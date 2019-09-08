package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.acl.Group;
import java.util.ArrayList;

public class GroupInfoEditActivity extends AppCompatActivity {
    
    private static final int REQUEST_CAMERA = 807;
    private static final int SELECT_FILE = 809;

    private String groupUID;
    private String groupName;
    private String groupImageUrl;

    private boolean isNameChanged = false ;
    private boolean isPhotoChanged = false;

    private Bitmap bmp;
    private ImageView imageView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info_edit);

        Intent intent = getIntent();
        groupUID = intent.getStringExtra("groupUID");
        groupName = intent.getStringExtra("groupName");
        groupImageUrl = intent.getStringExtra("groupImageUrl");

        // set the toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit");
        }
        
        imageView = findViewById(R.id.group_info_edit_imageview);
        if (groupImageUrl != null) {
            Picasso.get()
                    .load(groupImageUrl)
                    .fit()
                    .into(imageView);
        }
        
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        EditText editText = findViewById(R.id.group_info_edit_edittext);
        editText.setText(groupName);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               isNameChanged = true;
               groupName = editText.getText().toString();
            }
        });

        Button saveButton = findViewById(R.id.group_info_edit_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPhotoChanged) {
                    FirebaseFirestore.getInstance()
                            .collection("Groups")
                            .document(groupUID)
                            .update("groupImageUrl", url);
                }
                if (isNameChanged) {
                    FirebaseFirestore.getInstance()
                            .collection("Groups")
                            .document(groupUID)
                            .update("groupName", groupName);

                }
                sendMessage();
                Toast.makeText(GroupInfoEditActivity.this, "Changes saved.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    private void selectImage(){
        final CharSequence[] items={"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoEditActivity.this);
        builder.setTitle("Get image from");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    // ask for Camera permission
                    if (ContextCompat.checkSelfPermission(GroupInfoEditActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                                GroupInfoEditActivity.this, new String[] {Manifest.permission.CAMERA},
                                REQUEST_CAMERA);
                    }

                    if (ContextCompat.checkSelfPermission(GroupInfoEditActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(GroupInfoEditActivity.this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        isPhotoChanged = true;
        if (resultCode == Activity.RESULT_OK){
            Uri selectedImageUri = null;
            if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                bmp = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bmp);
            } else if (requestCode == SELECT_FILE){
                selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);
            }

            final String name = "profilePicture";
            final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Groups");
            final ProgressDialog mProgressDialog = new ProgressDialog(GroupInfoEditActivity.this);
            final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Groups");



            if (selectedImageUri != null) {
                final StorageReference fileReference = mStorageRef.child(groupUID).child("newGroupPic")
                        .child("image.jpg");

                InputStream imageStream = null;

                try {
                    imageStream = getContentResolver().openInputStream(selectedImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bmp = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                imageView.setImageBitmap(bmp);
                byte[] byteArray = stream.toByteArray();

                UploadTask uploadTask = fileReference.putBytes(byteArray);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupInfoEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupInfoEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Toast.makeText(GroupInfoEditActivity.this, url, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.setMessage("Uploading Image...");
                        mProgressDialog.show();
                    }
                });

                selectedImageUri = null;
            } else if (bmp != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] dataforbmp = baos.toByteArray();

                StorageReference fileReference = FirebaseStorage.getInstance().getReference("Users");
                final StorageReference imageRef = fileReference.child(groupUID).child("newGroupPic")
                        .child("image.jpg");

                UploadTask uploadTask = imageRef.putBytes(dataforbmp);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupInfoEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupInfoEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Toast.makeText(GroupInfoEditActivity.this, url, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.setMessage("Uploading Image...");
                        mProgressDialog.show();
                    }
                });

                bmp = null;
            } else {
                Toast.makeText(this, "No file selected or camera picture not configured yet", Toast.LENGTH_LONG).show();
            }

        }


    }

    private void sendMessage() {
        // get user's UID
        String userUID;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(GroupInfoEditActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                        ChatItem chatItem  = new ChatItem(name + " changed the group info", userUID, name, username, imageUrl,
                                groupUID, groupName, "" ,groupImageUrl);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("GroupMessages")
                                .document(groupUID)
                                .collection("ChatHistory")
                                .add(chatItem);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
