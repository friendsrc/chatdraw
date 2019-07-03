package com.example.chatdraw.AccountActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;
// import androidx.preference.PreferenceFragmentCompat;

import com.example.chatdraw.ChatActivites.MainActivity;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity {
    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;

    private CircleImageView circleImageView;

    private Uri selectedImageUri;
    private Bitmap bmp;
    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private String userUID;
    private FirebaseAuth auth;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        circleImageView = (CircleImageView) findViewById(R.id.new_profile_picture_image_view);
        mProgressDialog = new ProgressDialog(ProfileEditActivity.this);
        auth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("Users");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ProfileEditActivity.this);
                String tempName = "";
                String tempUsername = "";
                String imgurl = "";

                if (acct != null) {
                    String uID = acct.getId();

                    if (uID != null) {
                        userUID = uID;
                        tempName = (String) dataSnapshot.child(uID).child("name").getValue();
                        tempUsername = (String) dataSnapshot.child(uID).child("username").getValue();
                        imgurl = (String) dataSnapshot.child(uID).child("imageUrl").getValue();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Error on user validation", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    userUID = user.getUid();
                    tempName = (String) dataSnapshot.child(userUID).child("name").getValue();
                    tempUsername = (String) dataSnapshot.child(userUID).child("username").getValue();
                    imgurl = (String) dataSnapshot.child(userUID).child("imageUrl").getValue();
                }


                CircleImageView imgview = (CircleImageView) findViewById(R.id.new_profile_picture_image_view);

                if (imgurl == null) {
                    imgview.setImageResource(R.drawable.account_circle_black_75dp);
                } else {
                    Picasso.get()
                            .load(imgurl)
                            .into(imgview);
                }

                TextView tv = (TextView) findViewById(R.id.profiles_field);
                tv.setText(tempName);

                TextView tv1 = (TextView) findViewById(R.id.usernames_field);
                tv1.setText(tempUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.photo_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Settings");
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.root_preferences);

            bindSummaryValue(findPreference("status"));
            bindSummaryValue(findPreference("signature"));
        }
    }

     private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "Not set"));

    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value
                if (index > -1) {
                    preference.setSummary(listPreference.getEntries()[index]);

                    Activity activity = (Activity) preference.getContext();
                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

                    if (acct != null) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(acct.getId()).child("status");
                        databaseReference.setValue(stringValue);
                    } else {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("status");
                        databaseReference.setValue(stringValue);
                    }

                } else {
                    preference.setSummary(null);
                }
            } else if (preference instanceof EditTextPreference) {
                if (stringValue.length() == 0) {
                    if (preference.getSummary().equals("changethis")) {
                        Log.v("PASS HERE", "CHANGETHIS");
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Activity activity = (Activity) preference.getContext();
                                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

                                String profilename = "";
                                String username = "";

                                if (acct != null) {
                                    String tempID = acct.getId();
                                    profilename = (String) dataSnapshot.child(tempID).child("name").getValue();
                                    username = (String) dataSnapshot.child(tempID).child("username").getValue();
                                } else {
                                    String tempID = user.getUid();
                                    profilename = (String) dataSnapshot.child(tempID).child("name").getValue();
                                    username = (String) dataSnapshot.child(tempID).child("username").getValue();
                                }

                                if (preference.getKey().equals("profilenames") && (profilename != null)) {
                                    preference.setSummary(profilename);
                                } else if (preference.getKey().equals("usernames") && username != null) {
                                    preference.setSummary(username);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    } else {
                        Log.v("HERE", "SET HERE");
                        preference.setSummary("Not set");
                    }
                } else {
                    if (preference.getKey().equals("profilenames")) {
                        preference.setSummary(stringValue);
                    } else if (preference.getKey().equals("usernames")) {
                        preference.setSummary(stringValue);
                    } else {
                        preference.setSummary(stringValue);
                    }
                }
            } else if (preference instanceof RingtonePreference){
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary("Silent");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(),
                            Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary
                        preference.setSummary("Choose notification ringtone");
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            }

            return true;
        }
    };

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
                bmp = (Bitmap) bundle.get("data");
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
                final StorageReference fileReference = mStorageRef.child(userID).child("profilepic")
                        .child("image.jpg");

                InputStream imageStream = null;

                try {
                    imageStream = getContentResolver().openInputStream(selectedImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                circleImageView.setImageBitmap(bmp);
                byte[] byteArray = stream.toByteArray();

                UploadTask uploadTask = fileReference.putBytes(byteArray);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                // Upload upload = new Upload(name, url);

                                // update realtime
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(userID).child("imageUrl").setValue(url);

                                // update firestore
//                                Upload profileUpload = new Upload(url);
//                                FirebaseFirestore.getInstance().collection("Users").document(userID).set(profileUpload, SetOptions.merge());
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
                final StorageReference imageRef = fileReference.child(userID).child("profilepic")
                        .child("image.jpg");

                UploadTask uploadTask = imageRef.putBytes(dataforbmp);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                // Upload upload = new Upload(name, url);

                                // update realtime
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(userID).child("imageUrl").setValue(url);

                                // update firestore
                                Upload profileUpload = new Upload(url);
                                FirebaseFirestore.getInstance().collection("Users").document(userID).set(profileUpload, SetOptions.merge());
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

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}


//mUploadTask = fileReference.putFile(selectedImageUri)
//        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                mProgressDialog.dismiss();
//                Toast.makeText(ProfileEditActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
//                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    String url = uri.toString();
//                    Upload upload = new Upload(name, url);
//
//                    // update realtime
//                    String uploadId = mDatabaseRef.push().getKey();
//                    mDatabaseRef.child(userID).child("imageUrl").setValue(url);
//
//                    // update firestore
//                    Upload profileUpload = new Upload(url);
//                    FirebaseFirestore.getInstance().collection("Users").document(userID).set(profileUpload, SetOptions.merge());
//
//                }
//                });
//            }
//        })
//        .addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//            mProgressDialog.dismiss();
//            Toast.makeText(ProfileEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        })
//        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//            mProgressDialog.setMessage("Uploading Image...");
//            mProgressDialog.show();
//            }
//        });
