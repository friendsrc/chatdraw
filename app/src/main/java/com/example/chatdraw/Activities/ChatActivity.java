package com.example.chatdraw.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatdraw.Callers.BaseActivity;
import com.example.chatdraw.Callers.CallScreenActivity;
import com.example.chatdraw.Callers.SinchService;
import com.example.chatdraw.Drawing.DrawActivity;
import com.example.chatdraw.GroupCallers.GroupCallActivity;
import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Adapters.ChatRecyclerViewAdapter;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.annotation.Nullable;

public class ChatActivity extends BaseActivity implements RecyclerViewClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_DOCUMENT = 2;
    public static final int REQUEST_MICROPHONE = 3;
    public static final int REQUEST_INFOEDIT = 4;
    private static String TAG = "ChatActivity";
    private boolean isServiceReady = false;

    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";

    // this user's information
    private String userUID;
    final String[] userName = new String[1];
    final String[] userUsername = new String[1];
    final String[] userImageUrl = new String[1];

    // to check if its a one-on-one or group chat
    private boolean isGroup = false;

    // friend's information (used if isGroup == false)
    private String friendsUID;
    final String[] friendName = new String[1];
    final String[] friendUsername = new String[1];
    final String[] friendImageUrl = new String[1];

    // group's information (if isGroup == true)
    private String groupID;
    private String groupName;
    private String groupImageUrl;
    private LinkedList<String> membersID;

    // Uri are actually URLs that are meant for local storage
    private Uri selectedImageUri;
    private Uri pdfUri;
    private String pdfName;

    private Bitmap bmp;
    private ProgressDialog mProgressDialog;
    private boolean isActionSelected = false;
    private String userID;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage googleStorageRef;
    private FirebaseDatabase googleDatabaseRef;

    // RecyclerView
    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mAdapter;
    private LinkedList<ChatItem> myDataset;

    // SwipeRefreshLayout
    SwipeRefreshLayout mSwipeRefreshLayout;

    // Photo dialog pop-up
    Dialog mPhotoDialog;

    // for data pagination
    DocumentSnapshot lastSnapshot;
    int docsPerRetrieval = 500;
    int docsOnScreen = docsPerRetrieval;

    boolean isFromService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mPhotoDialog = new Dialog(this);

        mRecyclerView = findViewById(R.id.chat_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Progress Dialog for uploading
        mProgressDialog = new ProgressDialog(ChatActivity.this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        googleStorageRef = FirebaseStorage.getInstance();
        googleDatabaseRef = FirebaseDatabase.getInstance();

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        myDataset = new LinkedList<>();
        mAdapter = new ChatRecyclerViewAdapter(myDataset, ChatActivity.this, this);
        mRecyclerView.setAdapter(mAdapter);

        // set 'pull-to-fetch-older-messages'
        mSwipeRefreshLayout = findViewById(R.id.chat_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // get friend's UID
        Intent intent = getIntent();
        friendsUID = intent.getStringExtra("uID");


        // check if this activity is from ChatService
        isFromService = intent.getBooleanExtra("isFromService", false);

        // get user's UID
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChatActivity.this);
        if (acct != null) {
            this.userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // get user's display name and profile picture
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        userName[0] = dataSnapshot.child("name").getValue(String.class);
                        userUsername[0] = dataSnapshot.child("username").getValue(String.class);
                        userImageUrl[0] = dataSnapshot.child("imageUrl").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        // set the action bar title
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(intent.getStringExtra("name"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // solve confict here
        View v = findViewById(R.id.my_toolbar);
        v.setOnClickListener(v12 -> {
            if (isGroup) {
                Intent intent1 = new Intent(ChatActivity.this, GroupInfoActivity.class);
                intent1.putExtra("id", groupID);
                startActivityForResult(intent1, REQUEST_INFOEDIT);
            } else {
                // TODO
            }
        });

        // set onCLickListener on the 'More option' button
        ImageView fileImageView = findViewById(R.id.chat_attach_imageView);
        fileImageView.setOnClickListener(view -> SelectImage());

        // set onClickListener on the 'Send Message' button
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(v1 -> {
            // get  the inputted  message
            EditText editText = findViewById(R.id.chat_edittext);
            String message = editText.getText().toString();
            // create a new ChatItem
            if (!message.trim().equals("")) {
                ChatItem newChatItem = addMessageToAdapter(message);
                sendMessage(newChatItem); // send the ChatItem to Firebase
                editText.setText(""); // erase the content of the EditText
            }
        });

        if (friendsUID.startsWith("GROUP_")) {
            isGroup = true;
            groupID = friendsUID;
            FirebaseFirestore.getInstance().collection("Groups")
                    .document(groupID)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot snapshot = task.getResult();
                        ArrayList<String> arr = (ArrayList<String>) snapshot.get("members");
                        membersID = new LinkedList<>();
                        membersID.addAll(arr);
                        groupName = snapshot.getString("groupName");
                        groupImageUrl = snapshot.getString("groupImageUrl");
                        getMessages();
                    });
        } else {
            isGroup = false;
            // get friends's display name and profile picture

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(friendsUID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            friendName[0] = dataSnapshot.child("name").getValue(String.class);
                            friendUsername[0] = dataSnapshot.child("username").getValue(String.class);
                            friendImageUrl[0] = dataSnapshot.child("imageUrl").getValue(String.class);
                            getMessages();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGroup) {

        }
    }

    private void SelectImage(){
        final CharSequence[] items={"Camera", "Image", "File Explorer", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Send file from");

        builder.setItems(items, (dialogInterface, i) -> {
            if (items[i].equals("Camera")) {
                // ask for Camera permission
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(
                            ChatActivity.this, new String[] {Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
            } else if (items[i].equals("Image")) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_FILE);
            } else if (items[i].equals("File Explorer")) {
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_DOCUMENT);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, REQUEST_DOCUMENT);
                }
            } else if (items[i].equals("Cancel")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        } else if (requestCode == REQUEST_DOCUMENT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, REQUEST_DOCUMENT);
        } else if (requestCode == REQUEST_MICROPHONE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Permission is not granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_INFOEDIT) {
            FirebaseFirestore.getInstance()
                    .collection("Groups")
                    .document(groupID)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        Toolbar toolbar = findViewById(R.id.my_toolbar);
                        toolbar.setTitle(documentSnapshot.get("groupName").toString());
                    });
        }

        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                bmp = (Bitmap) bundle.get("data");
                isActionSelected = true;
            } else if (requestCode == SELECT_FILE){
                selectedImageUri = data.getData();
                isActionSelected = true;
            } else if (requestCode == REQUEST_DOCUMENT) {
                pdfUri = data.getData();
                isActionSelected = true;

                // get Pdf name
                String uriString = pdfUri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getContentResolver().query(pdfUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            pdfName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    pdfName = myFile.getName();
                }
            } else {
                isActionSelected = false;
            }

            if (isActionSelected) {
                //get the signed in user
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChatActivity.this);

                if (acct != null) {
                    userID = acct.getId();
                } else {
                    //get the signed in user
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        userID = user.getUid();
                    } else {
                        return;
                    }
                }

                if (bmp != null) {
                    mProgressDialog.setTitle("Uploading Image...");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.show();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataforbmp = baos.toByteArray();

                    StorageReference fileReference = FirebaseStorage.getInstance().getReference("Users");
                    Intent intent = getIntent();
                    friendsUID = intent.getStringExtra("uID");

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    final StorageReference imageRef = fileReference.child(userID)
                            .child("sentImage")
                            .child(friendsUID)
                            .child(timestamp.getTime() + ".jpg");

                    UploadTask uploadTask = imageRef.putBytes(dataforbmp);
                    uploadTask.addOnFailureListener(e -> {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(taskSnapshot -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
//                                     Upload upload = new Upload(name, url);
                            // update Firestore Chat
                            ChatItem newChatItem = addMessageToAdapter(userUID + "\tIMAGE\t" + url);
                            sendMessage(newChatItem);
                        });
                    }).addOnProgressListener(taskSnapshot -> {
                        int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mProgressDialog.setProgress(currentProgress);
                    }).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show());

                    bmp = null;
                } else if (selectedImageUri != null) {
                    mProgressDialog.setTitle("Uploading Image...");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.show();

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    final StorageReference fileReference = FirebaseStorage.getInstance().getReference("Users")
                            .child(userID)
                            .child("profilepic")
                            .child(timestamp.getTime() + ".jpg");

                    InputStream imageStream = null;

                    try {
                        imageStream = getContentResolver().openInputStream(selectedImageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                    // this is an example if you want to set the bmp to your chat for example
                    // circleImageView.setImageBitmap(bmp);

                    byte[] byteArray = stream.toByteArray();

                    UploadTask uploadTask = fileReference.putBytes(byteArray);
                    uploadTask.addOnFailureListener(e -> {
                        // Handle unsuccessful uploads
                        mProgressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(taskSnapshot -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();
//                                    Upload upload = new Upload(name, url);

                            // update realtime
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(userID).child("imageUrl").setValue(url);

                            // update firestore
//                                Upload profileUpload = new Upload(url);
                            ChatItem newChatItem = addMessageToAdapter(userUID + "\tIMAGE\t" + url);
                            sendMessage(newChatItem);
                        });
                    }).addOnProgressListener(taskSnapshot -> {
                        int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        mProgressDialog.setProgress(currentProgress);
                    }).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show());

                    selectedImageUri = null;
                } else if (pdfUri != null) {
                    mProgressDialog.setTitle("Uploading Files...");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.show();

                    String fileName = System.currentTimeMillis() + "";
                    final StorageReference storageReference = googleStorageRef.getReference().child(userID).child("Uploads").child(fileName);
                    storageReference.putFile(pdfUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                mProgressDialog.dismiss();
                                // url is the link that will redirect you to the FirebaseStorage
                                storageReference.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            // connect ke Firestore
                                            ChatItem newChatItem = addMessageToAdapter(userUID + "\tPDF\t" + pdfName + "\t" + uri);
                                            sendMessage(newChatItem);
                                        });


                            })
                            .addOnFailureListener(e -> {
                                mProgressDialog.dismiss();
                                Toast.makeText(ChatActivity.this, "Failed to upload files", Toast.LENGTH_SHORT).show();
                            }).addOnProgressListener(taskSnapshot -> {
                                int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mProgressDialog.setProgress(currentProgress);
                            });
                } else {
                    Toast.makeText(this, "No file selected or camera picture not configured yet", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No file selected or camera picture not configured yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        switch (item.getItemId()) {
            case R.id.draw:
                // Go to draw activity
                Intent intent = new Intent(ChatActivity.this, DrawActivity.class);
                intent.putExtra("userUID", userUID);
                intent.putExtra("friendsUID", friendsUID);
                startActivity(intent);

                return true;
            case R.id.call:
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    // we are connected to a network

                    if (isGroup) {
                        if (isServiceReady) {
                            groupCallButtonClicked();
                        } else {
                            Toast.makeText(this, "Calling service not ready", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (isServiceReady) {
                            callButtonClicked();
                        } else {
                            Toast.makeText(this, "Calling service not ready", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "No internet connection detected", Toast.LENGTH_SHORT).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onServiceConnected() {
        isServiceReady = true;
    }

    private void groupCallButtonClicked() {
        String[] tempGroupArr = friendsUID.split("_");
        String sinchGroupID = tempGroupArr[0] + "_" + tempGroupArr[2];
        Log.v("groupSinch", sinchGroupID);
        Log.v("memberSinch", "" + membersID);

        if (userImageUrl[0] == null) {
            userImageUrl[0] = "https://firebasestorage.googleapis.com/v0/b/chatdraw-ff7eb.appspot.com/o/Users%2F106689861101623002819%2Fprofilepic%2F1567929062984.jpg?alt=media&token=a800b643-1d02-4d38-964f-46c84b0b3b02";
        }

        Log.v("MYGOD", "" + userName[0]);
        Log.v("MYGOD", "" + userImageUrl[0]);

        if (getSinchServiceInterface().getGroupIsOnGoingCall()) {
            if (getSinchServiceInterface().getGroupUserName().equals(sinchGroupID)) {
                Intent intent = new Intent(ChatActivity.this, GroupCallActivity.class);
                intent.putExtra("participant", membersID.size());
                intent.putExtra("imageUrl", userImageUrl[0]);
                intent.putExtra("userName", userName[0]);
                intent.putExtra("userID", userUID);
                intent.putExtra("groupID", sinchGroupID);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cannot call others while talking with others", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (getSinchServiceInterface().getIsOnGoingCall()) {
                Toast.makeText(this, "You are currently calling other person: "
                        + getSinchServiceInterface().getFriendName(), Toast.LENGTH_SHORT).show();
            } else if (getSinchServiceInterface().getTryConnectUser() != null) {
                Toast.makeText(this, "Cannot call others while trying to connect with others", Toast.LENGTH_SHORT).show();
            } else {
                if (sinchGroupID.isEmpty()) {
                    Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Intent intent = new Intent(ChatActivity.this, GroupCallActivity.class);
                    intent.putExtra("participant", membersID.size());
                    intent.putExtra("userID", userUID);
                    intent.putExtra("imageUrl", userImageUrl[0]);
                    intent.putExtra("userName", userName[0]);
                    intent.putExtra("groupID", sinchGroupID);
                    intent.putExtra("groupName", groupName);
                    startActivity(intent);
                } catch (MissingPermissionException e) {
                    ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, REQUEST_MICROPHONE);
                }
            }
        }
    }

    private void callButtonClicked() {
        if (getSinchServiceInterface().getIsOnGoingCall()) {
            if (getSinchServiceInterface().getFriendUserName().equals(friendsUID)) {
                Toast.makeText(this, "Is on going call", Toast.LENGTH_SHORT).show();

                String tempCallID = getSinchServiceInterface().getCurrentUserCallID();

                Intent callScreen = new Intent(this, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, tempCallID);
                callScreen.putExtra("userID", userUID);
                callScreen.putExtra("FriendID", friendsUID);
                callScreen.putExtra("FriendName", friendName[0]);

                startActivity(callScreen);
            } else {
                Toast.makeText(this, "Cannot call others while talking with others", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (getSinchServiceInterface().getGroupIsOnGoingCall()) {
                Toast.makeText(this, "You have on-going call in another group: "
                        + getSinchServiceInterface().getGroupName(), Toast.LENGTH_SHORT).show();
            } else if (getSinchServiceInterface().getTryConnectUser() != null) {
                if (!getSinchServiceInterface().getTryConnectUser().equals(friendsUID)) {
                    Toast.makeText(this, "Cannot call others while talking with others", Toast.LENGTH_SHORT).show();
                } else {
                    Intent callScreen = new Intent(this, CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, getSinchServiceInterface().getTryConnectCallID());
                    callScreen.putExtra("userID", userUID);
                    callScreen.putExtra("FriendID", friendsUID);
                    callScreen.putExtra("FriendName", friendName[0]);
                    startActivity(callScreen);
                }
            } else {
                String userNameTemp = friendsUID;

                if (userNameTemp.isEmpty()) {
                    Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    Call call = getSinchServiceInterface().callUser(userNameTemp);
                    if (call == null) {
                        // Service failed for some reason, show a Toast and abort
                        Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                                + "placing a call.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String callId = call.getCallId();
                    Intent callScreen = new Intent(this, CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("userID", userUID);
                    callScreen.putExtra("FriendID", friendsUID);
                    callScreen.putExtra("FriendName", friendName[0]);
                    getSinchServiceInterface().setTryConnectUser(friendsUID);
                    getSinchServiceInterface().setTryConnectCallID(callId);
                    startActivity(callScreen);
                } catch (MissingPermissionException e) {
                    ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, REQUEST_MICROPHONE);
                }
            }
        }
    }

    // send the ChatItem to Firebase
    private void sendMessage(ChatItem chatItem) {
        docsOnScreen++;
        Log.d(TAG, "sending Message");
        if (!chatItem.getMessageBody().equals("")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (!isGroup) {
                // Send to this user's message collection
                db.collection("Messages")
                        .document(userUID)
                        .collection("Friends")
                        .document(friendsUID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Send to the receiver's message collection
                db.collection("Messages")
                        .document(friendsUID)
                        .collection("Friends")
                        .document(userUID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Check if the message is not a text message
                if (chatItem.getMessageBody().startsWith(userUID)) {
                    String[] arr = chatItem.getMessageBody().split("\t");
                    if (arr[1].equals("IMAGE")) {
                        chatItem.setMessageBody("[Image]");
                    } else if (arr[1].equals("PDF")) {
                        chatItem.setMessageBody("[Pdf]");
                    } else {
                        chatItem.setMessageBody("[Unknown file type]");
                    }
                }

                // Limit the length of chat preview
                if (chatItem.getMessageBody().length() > 43) {
                    chatItem.setMessageBody(chatItem.getMessageBody().substring(0, 40) + "...");
                }


                // Send to user's message preview collection
                db.collection("Previews").document(userUID)
                        .collection("ChatPreviews").document(friendsUID)
                        .set(chatItem);

                // Send to the receiver's message preview collection
                db.collection("Previews").document(friendsUID)
                        .collection("ChatPreviews").document(userUID)
                        .set(chatItem);

            } else {
                // Send to group's message collection
                chatItem.setReceiverName(groupName);
                db.collection("GroupMessages")
                        .document(groupID)
                        .collection("ChatHistory")
                        .add(chatItem);
            }
        }
    }

    public void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isGroup) {
            db.collection("Messages")
                    .document(userUID)
                    .collection("Friends")
                    .document(friendsUID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(docsOnScreen)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        // remove previous data
                        mAdapter.clearData();
                        for (DocumentSnapshot q: queryDocumentSnapshots) {
                            lastSnapshot = q;
                            ChatItem chatItem = q.toObject(ChatItem.class);
                            String[] arr = chatItem.getMessageBody().split("\t");

                            if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                String updatedImageURL = friendImageUrl[0];
                                chatItem.setSenderImageUrl(updatedImageURL);
                            }
                            mAdapter.addData(chatItem);
                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });
        } else { // if its a group
            db.collection("GroupMessages")
                    .document(groupID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(docsOnScreen)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        mAdapter.clearData();
                        for (DocumentSnapshot q: queryDocumentSnapshots) {
                            lastSnapshot = q;
                            ChatItem chatItem = q.toObject(ChatItem.class);

                            if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                String updatedImageURL = friendImageUrl[0];
                                chatItem.setSenderImageUrl(updatedImageURL);
                            }
                            mAdapter.addData(chatItem);
                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, destroy this activity
        if (isFromService) {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
        return true;
    }

    public ChatItem addMessageToAdapter(String messageBody) {
        ChatItem chatItem;
        if (isGroup) {
            chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0],
                    userImageUrl[0], groupID, groupName, null, groupImageUrl);
        } else {
            chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0],
                    userImageUrl[0], friendsUID, friendName[0], friendUsername[0], friendImageUrl[0]);
        }

        // add the new ChatItem to the ChatAdapter
        mAdapter.addData(chatItem);
        return chatItem;
    }

    @Override
    public void onRefresh() {
        getOlderMessages();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        ChatItem chatItem = mAdapter.getItem(position);

        if (v.findViewById(R.id.text_message_cardview) != null) {
            goToImagePreview(chatItem);
        } else if (v.findViewById(R.id.pdf_icon_imageview) != null) {
            String url = chatItem.getMessageBody().split("\t")[3];
            viewPdf(url);
        }

    }

    public void goToImagePreview(ChatItem chatItem) {
        Intent intent = new Intent(ChatActivity.this, ImagePreviewActivity.class);
        String[] arr = chatItem.getMessageBody().split("\t");
        intent.putExtra("imageUrl", arr[2]);
        String senderName;
        if (chatItem.getSenderID().equals(userUID)) {
            senderName = "You";
        } else {
            senderName = chatItem.getSenderName();
        }
        intent.putExtra("senderName", senderName);
        startActivity(intent);
    }

    public void viewPdf(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public void getOlderMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isGroup) {
            db.collection("Messages")
                    .document(userUID)
                    .collection("Friends")
                    .document(friendsUID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastSnapshot)
                    .limit(docsPerRetrieval)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(ChatActivity.this,
                                    "No older messages.", Toast.LENGTH_SHORT).show();
                        }
                        for (DocumentSnapshot q: queryDocumentSnapshots) {
                            lastSnapshot = q;
                            ChatItem chatItem = q.toObject(ChatItem.class);

                            if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                String updatedImageURL = friendImageUrl[0];
                                chatItem.setSenderImageUrl(updatedImageURL);
                            }
                            mAdapter.addData(chatItem);
                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });
        } else { // if its not a group
            db.collection("GroupMessages")
                    .document(groupID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastSnapshot)
                    .limit(docsPerRetrieval)
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(ChatActivity.this,
                                    "No older messages.", Toast.LENGTH_SHORT).show();
                        }
                        for (DocumentSnapshot q: queryDocumentSnapshots) {
                            lastSnapshot = q;
                            ChatItem chatItem = q.toObject(ChatItem.class);

                            if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                String updatedImageURL = friendImageUrl[0];
                                chatItem.setSenderImageUrl(updatedImageURL);
                            }
                            mAdapter.addData(chatItem);
                            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });
        }
    }

    public void showPhotoPopup(ChatItem chatItem) {
        mPhotoDialog.setContentView(R.layout.photopopup);
        mPhotoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPhotoDialog.setCancelable(true);

        ImageView photo = mPhotoDialog.findViewById(R.id.photo_popup_image);
        String[] arr = chatItem.getMessageBody().split("\t");
        String photoUrl = arr[2];
        Picasso.get()
                .load(photoUrl)
                .fit()
                .into(photo);


        ImageView closeButton = mPhotoDialog.findViewById(R.id.photo_popup_close_button);
        closeButton.setOnClickListener(v -> mPhotoDialog.dismiss());

        ImageView saveButton = mPhotoDialog.findViewById(R.id.photo_popup_save_button);
        saveButton.setOnClickListener(v -> Toast.makeText(ChatActivity.this, "Not yet configured", Toast.LENGTH_SHORT).show());

        mPhotoDialog.show();
        mPhotoDialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}

/*
For future use: If you want to mute another people

    public void checkForConferenceDetails() {
        String myURL = "https://callingapi.sinch.com/v1/conferences/id/" + groupID;

        RequestQueue requestQueue = Volley.newRequestQueue(ChatActivity.this);
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                myURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            num_participant = response.getJSONArray("participants").length();
                        } catch (JSONException e) {
                            Toast.makeText(ChatActivity.this, "Unknown error occurred [802]", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            if (error.networkResponse.statusCode == 404) {
                                num_participant = 0;
                                Toast.makeText(ChatActivity.this, "No call before", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ChatActivity.this, "Unknown error occurred [804]", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ){
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String namePassword = APP_KEY + ":" + APP_SECRET;
                String auth = Base64.encodeToString(namePassword.getBytes(), Base64.NO_WRAP);

                String authorization = "Basic" + " " + auth;

                params.put("Authorization", authorization);
                return params;
            }
        };

        requestQueue.add(objectRequest);
    }
*/