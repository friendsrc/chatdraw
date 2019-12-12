package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

    private Uri cameraImageUri;

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
        groupPicture.setOnClickListener(v -> selectImage());

        // if 'create group' button is clicked, send information to firestore
        final EditText editText = findViewById(R.id.group_create_edittext);
        Button button = findViewById(R.id.group_create_button);
        button.setOnClickListener(v -> {

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

                firestore.collection("Users")
                        .document(userUID)
                        .update("groups", FieldValue.arrayUnion(groupID));

                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users")
                        .document(userUID)
                        .update("groups", FieldValue.arrayUnion(groupID))
                        .addOnSuccessListener(aVoid -> Toast.makeText(GroupCreateActivity.this,
                                "Contact added successfully.",
                                Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("contacts", new ArrayList<String>());
                            map.put("groups", new ArrayList<String>());
                            db.collection("Users").document(userUID).set(map);
                            db.collection("Users")
                                    .document(userUID)
                                    .update("groups", FieldValue.arrayUnion(groupID))
                                    .addOnSuccessListener(aVoid -> Toast.makeText(GroupCreateActivity.this,
                                            "Contact added successfully.",
                                            Toast.LENGTH_SHORT).show());
                        });

                // add group name to group document
                Map<String, Object> docData = new HashMap<>();
                docData.put("groupName", groupName);
                docData.put("groupID", groupID);
                docData.put("groupImageUrl", url);
                reference.set(docData);

                // add group data to Realtime Database
                FirebaseDatabase.getInstance()
                        .getReference("Groups")
                        .child(groupID)
                        .setValue(docData);

                // add each member's id to group document and add group id to each
                // member's document
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups/" + groupID);
                for (String s: members) {
                    // add member id to group
                    reference.update("members", FieldValue.arrayUnion(s));

                    // add group id to member
                    final String memberID = s;
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(s)
                            .update("groups", FieldValue.arrayUnion(groupID))
                            .addOnFailureListener(e -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("contacts", new ArrayList<String>());
                                map.put("groups", new ArrayList<String>());
                                db.collection("Users").document(memberID).set(map);
                                db.collection("Users")
                                        .document(memberID)
                                        .update("groups", FieldValue.arrayUnion(groupID))
                                        .addOnSuccessListener(aVoid -> Toast.makeText(GroupCreateActivity.this,
                                                "Contact added successfully.",
                                                Toast.LENGTH_SHORT).show());
                            });

                    // create a placeholder chat item
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

    public static Bitmap rotateImage(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void selectImage(){
        final CharSequence[] items={"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupCreateActivity.this);
        builder.setTitle("Get image from");

        builder.setItems(items, (dialogInterface, i) -> {
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
                    takePicture();
                }
            } else if (items[i].equals("Gallery")) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_FILE);
            }
        });
        builder.show();
    }

    private void takePicture() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        cameraImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if (resultCode == Activity.RESULT_OK){
            cameraLogo.setVisibility(View.INVISIBLE);
            if(requestCode == REQUEST_CAMERA){
//                Bundle bundle = data.getExtras();
//                bmp = (Bitmap) bundle.get("data");
                try {
                    bmp = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), cameraImageUri);
                    ExifInterface ei = new ExifInterface(
                            this.getContentResolver().openInputStream(cameraImageUri));
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bmp = rotateImage(bmp, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bmp = rotateImage(bmp, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bmp = rotateImage(bmp, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                uploadTask.addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    mProgressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
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
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    mProgressDialog.setMessage("Uploading Image...");
                    mProgressDialog.show();
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
                uploadTask.addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    mProgressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
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
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    mProgressDialog.setMessage("Uploading Image...");
                    mProgressDialog.show();
                });

                bmp = null;
            } else {
                Toast.makeText(this, "No file selected or camera picture not configured yet", Toast.LENGTH_LONG).show();
            }

        }
    }
}
