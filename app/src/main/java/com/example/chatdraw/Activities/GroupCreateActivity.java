package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupCreateActivity extends AppCompatActivity {

    private static final String TAG = "GroupCreateAcitivity";

    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;

    private String userUID;
    private Bitmap bmp;
    private Uri selectedImageUri;
    private String url;

    private String groupID;
    ImageView groupPicture;
    ImageView cameraLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        // get the added group members' IDs
        final String[] memberUIDs = getIntent().getStringArrayExtra("memberList");

        final ArrayList<String> members = new ArrayList<>();
        Collections.addAll(members, memberUIDs);

        // get this user's ID and add to array
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(GroupCreateActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        members.add(userUID);

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cameraLogo = findViewById(R.id.camera_logo);
        groupPicture = findViewById(R.id.group_create_imageview);
        groupPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        // if 'create group' button is clicked, send information to firestore
        final EditText editText = findViewById(R.id.group_create_edittext);
        Button button = findViewById(R.id.group_create_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get group name from EditText
                String groupName = editText.getText().toString().trim();

                if (!groupName.equals("")) {
                    // create a group id
                    groupID = "GROUP_" + userUID + "_" + UUID.randomUUID();

                    // get firestore
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    DocumentReference reference = firestore
                            .collection("Groups")
                            .document(groupID);

                    // add group name to group document
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("groupName", groupName);
                    docData.put("groupID", groupID);
                    docData.put("groupImageUrl", url);
                    reference.set(docData);

                    // add each member's id to group document and add group id to each
                    // member's document
                    for (String s: members) {
                        // add member id to group
                        reference.update("members", FieldValue.arrayUnion(s));

                        // add group id to member
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(s)
                                .update("groups", FieldValue.arrayUnion(groupID));

                        // create a placeholder chat item, TODO: set imageUrl
                        ChatItem chatItem = new ChatItem("", groupID, groupName,
                                null, url, s,
                                null, null, null);

                        // Send to user's message preview collection
                        FirebaseFirestore.getInstance()
                                .collection("Previews")
                                .document(s)
                                .collection("ChatPreviews")
                                .document(groupID)
                                .set(chatItem);
                    }

                    // set the result as successful
                    Intent intent = new Intent();
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("groupName", groupName);
                    intent.putExtra("groupImageUrl", url);
                    setResult(Activity.RESULT_OK, intent);

                    // destroy this activity
                    finish();
                } else {
                    Toast.makeText(GroupCreateActivity.this,
                            "Group name shouldn't be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void SelectImage(){
        final CharSequence[] items={"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupCreateActivity.this);
        builder.setTitle("Get image from");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    // ask for Camera permission
                    if (ContextCompat.checkSelfPermission(GroupCreateActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(
                                GroupCreateActivity.this, new String[] {Manifest.permission.CAMERA},
                                REQUEST_CAMERA);
                    }

                    if (ContextCompat.checkSelfPermission(GroupCreateActivity.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(GroupCreateActivity.this, "Camera permission not granted", Toast.LENGTH_SHORT).show();
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



        if (resultCode == Activity.RESULT_OK){
            cameraLogo.setVisibility(View.INVISIBLE);
            if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                bmp = (Bitmap) bundle.get("data");
                groupPicture.setImageBitmap(bmp);
            } else if (requestCode == SELECT_FILE){
                selectedImageUri = data.getData();
                groupPicture.setImageURI(selectedImageUri);
            }

            final String name = "profilePicture";
            final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Groups");
            final ProgressDialog mProgressDialog = new ProgressDialog(GroupCreateActivity.this);
            final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Groups");



            if (selectedImageUri != null) {
                final StorageReference fileReference = mStorageRef.child(userUID).child("newGroupPic")
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
                groupPicture.setImageBitmap(bmp);
                byte[] byteArray = stream.toByteArray();

                UploadTask uploadTask = fileReference.putBytes(byteArray);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Toast.makeText(GroupCreateActivity.this, url, Toast.LENGTH_LONG).show();

                                // Upload upload = new Upload(name, url);

                                // update realtime
//                                String uploadId = mDatabaseRef.push().getKey();
//                                mDatabaseRef.child(userUID).child("imageUrl").setValue(url);

                                // update firestore
//                                Upload profileUpload = new Upload(url);
//                                Map<String, Object> map = new HashMap<>();
//                                map.put("GroupImageUrl", url);
//                                FirebaseFirestore.getInstance().collection("Groups").document(groupID).update(map);
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
                final StorageReference imageRef = fileReference.child(userUID).child("newGroupPic")
                        .child("image.jpg");

                UploadTask uploadTask = imageRef.putBytes(dataforbmp);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Toast.makeText(GroupCreateActivity.this, url, Toast.LENGTH_LONG).show();

                                // Upload upload = new Upload(name, url);

                                // update realtime
//                                String uploadId = mDatabaseRef.push().getKey();
//                                mDatabaseRef.child(userUID).child("imageUrl").setValue(url);
//
//                                // update firestore
//                                Upload profileUpload = new Upload(url);
//                                Map<String, Object> map = new HashMap<>();
//                                map.put("GroupImageUrl", url);
//                                FirebaseFirestore.getInstance().collection("Groups").document(groupID).update(map);
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
}
