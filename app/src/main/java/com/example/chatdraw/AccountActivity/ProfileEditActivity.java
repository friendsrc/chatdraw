package com.example.chatdraw.AccountActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity {
    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;

    private CircleImageView circleImageView;

    private Uri selectedImageUri;
    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth auth;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        circleImageView = (CircleImageView) findViewById(R.id.new_profile_picture_image_view);
        mProgressDialog = new ProgressDialog(ProfileEditActivity.this);
        auth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.photo_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        Button settingspage = (Button) findViewById(R.id.settings_redirect);
        settingspage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_settings = new Intent(ProfileEditActivity.this, SettingsActivity.class);
                startActivity(intent_settings);
            }
        });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        if (user != null) {
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String profilename = (String) dataSnapshot.child(user.getUid()).child("name").getValue();
                    String username = (String) dataSnapshot.child(user.getUid()).child("username").getValue();

                    TextView tv = (TextView) findViewById(R.id.profiles_field);
                    tv.setText(profilename);

                    TextView tv1 = (TextView) findViewById(R.id.usernames_field);
                    tv1.setText(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        // add a back button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Settings");
    }

    private void SelectImage(){
        final CharSequence[] items={"Camera","Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
        builder.setTitle("Change profile image from");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    // ask for Camera permission
                    if (ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                                ProfileEditActivity.this, new String[] {Manifest.permission.CAMERA},
                                REQUEST_CAMERA);
                    }

                    if (ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(ProfileEditActivity.this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);
                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if (resultCode == Activity.RESULT_OK){

            if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                circleImageView.setImageBitmap(bmp);
            } else if (requestCode == SELECT_FILE){
                selectedImageUri = data.getData();
                circleImageView.setImageURI(selectedImageUri);
            }

            //get the signed in user
            FirebaseUser user = auth.getCurrentUser();
            final String userID = user.getUid();

            final String name = "profilePicture";

            if (selectedImageUri != null) {
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(selectedImageUri));

                mUploadTask = fileReference.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mProgressDialog.dismiss();
                                Toast.makeText(ProfileEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                                Upload upload = new Upload(name,
                                        taskSnapshot.getUploadSessionUri().toString());
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(userID).child("uploads").setValue(upload);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgressDialog.dismiss();
                                Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                mProgressDialog.setMessage("Uploading Image...");
                                mProgressDialog.show();
                            }
                        });
                selectedImageUri = null;
            } else {
                Toast.makeText(this, "No file selected or camera picture not configured yet", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
