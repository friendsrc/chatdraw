package com.example.chatdraw.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.example.chatdraw.Drawing.DrawActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity implements RecyclerViewClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int SELECT_FILE = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_DOCUMENT = 2;
    private static String TAG = "ChatActivity";

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

        Log.d("HEY", friendsUID);

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
        FirebaseFirestore.getInstance().collection("Users")
                .document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        userName[0] = task.getResult().getString("name");
                        userUsername[0] = task.getResult().getString("username");
                        userImageUrl[0] = task.getResult().getString("imageUrl");
                    }
                });


        // set the action bar title
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(intent.getStringExtra("name"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set onCLickListener on the 'More option' button
        ImageView fileImageView = findViewById(R.id.chat_attach_imageView);
        fileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        // set onClickListener on the 'Send Message' button
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get  the inputted  message
                EditText editText = findViewById(R.id.chat_edittext);
                String message = editText.getText().toString();
                // create a new ChatItem
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
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            ArrayList<String> arr = (ArrayList<String>) snapshot.get("members");
                            membersID = new LinkedList<>();
                            membersID.addAll(arr);
                            groupName = snapshot.getString("groupName");
                            groupImageUrl = snapshot.getString("groupImageUrl");
                            getMessages();
                        }
                    });
        } else {
            isGroup = false;
            // get friends's display name and profile picture
            FirebaseFirestore.getInstance().collection("Users")
                    .document(friendsUID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            friendName[0] = task.getResult().getString("name");
                            friendUsername[0] = task.getResult().getString("username");
                            friendImageUrl[0] = task.getResult().getString("imageUrl");
                            getMessages();
                        }
                    });
        }
    }

    private void SelectImage(){
        final CharSequence[] items={"Camera", "Image", "File Explorer", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Send file from");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
        } else {
            Toast.makeText(this, "Permission is not granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

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
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful uploads
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
//                                     Upload upload = new Upload(name, url);
                                    // update Firestore Chat
                                    ChatItem newChatItem = addMessageToAdapter(userUID + "\tIMAGE\t" + url);
                                    sendMessage(newChatItem);
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressDialog.setProgress(currentProgress);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                        }
                    });

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
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful uploads
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
//                                    Upload upload = new Upload(name, url);

                                    // update realtime
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(userID).child("imageUrl").setValue(url);

                                    // update firestore
//                                Upload profileUpload = new Upload(url);
                                    ChatItem newChatItem = addMessageToAdapter(userUID + "\tIMAGE\t" + url);
                                    sendMessage(newChatItem);
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressDialog.setProgress(currentProgress);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                        }
                    });

                    selectedImageUri = null;
                } else if (pdfUri != null) {
                    mProgressDialog.setTitle("Uploading Files...");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setProgress(0);
                    mProgressDialog.show();

                    String fileName = System.currentTimeMillis() + "";
                    final StorageReference storageReference = googleStorageRef.getReference().child(userID).child("Uploads").child(fileName);
                    storageReference.putFile(pdfUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mProgressDialog.dismiss();
                                    // url is the link that will redirect you to the FirebaseStorage
                                    storageReference.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // connect ke Firestore
                                                    ChatItem newChatItem = addMessageToAdapter(userUID + "\tPDF\t" + pdfName +"\t" + uri);
                                                    sendMessage(newChatItem);
                                                }
                                            });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ChatActivity.this, "Failed to upload files", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressDialog.setProgress(currentProgress);
                        }
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
        switch (item.getItemId()) {
            case R.id.draw:
                // Go to draw activity
                Intent intent = new Intent(ChatActivity.this, DrawActivity.class);
                intent.putExtra("userUID", userUID);
                intent.putExtra("friendsUID", friendsUID);
                startActivity(intent);
//                Toast.makeText(this, "Draw", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.call:
                // make a call
                Toast.makeText(this, "Call", Toast.LENGTH_SHORT).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
                        }
                    });
        } else { // if its a group
            db.collection("GroupMessages")
                    .document(groupID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(docsOnScreen)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
        } else if (v.findViewById(R.id.pdf_icon_imageview) != null) {
            String url = chatItem.getMessageBody().split("\t")[3];

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);

//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.parse(url), "application/pdf");
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            Intent newIntent = Intent.createChooser(intent, "Open File");
//            try {
//                startActivity(newIntent);
//            } catch (ActivityNotFoundException e) {
//                // Instruct the user to install a PDF reader here, or something
//            }

        }

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
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
                        }
                    });
        } else { // if its not a group
            db.collection("GroupMessages")
                    .document(groupID)
                    .collection("ChatHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastSnapshot)
                    .limit(docsPerRetrieval)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
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
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoDialog.dismiss();
            }
        });

        ImageView saveButton = mPhotoDialog.findViewById(R.id.photo_popup_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Not yet configured", Toast.LENGTH_SHORT).show();
            }
        });

        mPhotoDialog.show();
        mPhotoDialog.getWindow().setGravity(Gravity.CENTER);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
