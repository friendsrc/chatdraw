package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatdraw.AccountActivity.LoginActivity;
import com.example.chatdraw.AccountActivity.ProfileEditActivity;
import com.example.chatdraw.Callers.BaseActivity;
import com.example.chatdraw.Callers.SinchService;
import com.example.chatdraw.Credits.CreditActivity;
import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.Services.ChatService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.Adapters.FriendListAdapter;
import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.R;
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
import com.sinch.android.rtc.SinchError;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, SinchService.StartFailedListener {
    private static final String TAG = "MainActivity";

    private static final int FIND_FRIEND_REQUEST_CODE = 101;
    private static final int NEW_MESSAGE_REQUEST_CODE = 808;
    private static final int FIND_SETTINGS_REQUEST_CODE = 909;

    private DrawerLayout drawer;
    private DatabaseReference mDatabaseRef;
    private FriendListAdapter mFriendListAdapter;

    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create Adapter and set to ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;
        ListView listView = findViewById(R.id.main_chat_listview);
        listView.setAdapter(friendListAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        final View hView = navigationView.getHeaderView(0);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                userUID = user.getUid();
            } else {
                Toast.makeText(this, "User is not verified", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tempName = "";
                String tempUsername = "";
                String imgurl = "";

                tempName = (String) dataSnapshot.child(userUID).child("name").getValue();
                tempUsername = (String) dataSnapshot.child(userUID).child("username").getValue();
                imgurl = (String) dataSnapshot.child(userUID).child("imageUrl").getValue();

                TextView tv = hView.findViewById(R.id.profile_field);
                tv.setText(tempName);

                TextView tv1 = hView.findViewById(R.id.username_field);
                tv1.setText(tempUsername);

                ImageButton imgbut = hView.findViewById(R.id.profile_edit_button);

                if (imgurl == null) {
                    imgbut.setImageResource(R.drawable.account_circle_black_75dp);
                } else {
                    Picasso.get()
                            .load(imgurl)
                            .fit()
                            .into(imgbut);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Unknown error, please contact us", Toast.LENGTH_SHORT).show();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Chatdraw");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // set the 'add-message' ImageView
        // if clicked -> go to NewMessageActivity
        ImageView newChatImageView = findViewById(R.id.main_chat_add_message_imageview);
        newChatImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
            startActivityForResult(intent, NEW_MESSAGE_REQUEST_CODE);
        });

        ImageButton imgbutt = hView.findViewById(R.id.profile_edit_button);
        imgbutt.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });

        // remove service
        Intent stopIntent = new Intent(MainActivity.this, ChatService.class);
        stopService(stopIntent);

        // remove notifications
//        String ns = Context.NOTIFICATION_SERVICE;
//        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
//        nMgr.cancelAll();
    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
        loginClicked();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        Toast.makeText(this, "SERVICE IS READY", Toast.LENGTH_SHORT).show();
    }

    private void loginClicked() {
        String userName = userUID;

        if (userName.isEmpty()) {
            Toast.makeText(this, "[MAIN] Username empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (!userName.equals(getSinchServiceInterface().getUserName())) {
            getSinchServiceInterface().stopClient();
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseFirestore.getInstance().collection("Users")
                .document(userUID)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<String> groups = (ArrayList<String>) task.getResult().get("groups");
                    // add change listener for groups
                    if (groups != null && !groups.isEmpty()) {
                        for (String s : groups) {
                            addListener(s);
                        }
                    }
                    // add the message previews to RecyclerView
                    getMessageList(mFriendListAdapter);
                });

        // set on click listener to the ListView
        ListView listView = findViewById(R.id.main_chat_listview);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            FriendListItem friendListItem = (FriendListItem) mFriendListAdapter.getItem(position);
            intent.putExtra("name", friendListItem.getName());
            intent.putExtra("uID", friendListItem.getUID());
            startActivity(intent);
        });
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_contacts:
                Intent intent  = new Intent(this, FriendListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_credits:
                Intent intent_credit  = new Intent(this, CreditActivity.class);
                startActivity(intent_credit);
                break;
            case R.id.nav_group:
                Intent intent_group = new Intent(this, NewGroupActivity.class);
                startActivity(intent_group);
                break;
            case R.id.nav_invites:
                Intent intent_invite = new Intent(this, InviteFriendActivity.class);
                startActivity(intent_invite);
                break;
            case R.id.nav_settings:
                Intent intent_settings = new Intent(this, ProfileEditActivity.class);
                startActivityForResult(intent_settings, FIND_SETTINGS_REQUEST_CODE);
                break;
            default:
                Toast.makeText(this,"Not yet configured", Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

        } else if (requestCode == FIND_SETTINGS_REQUEST_CODE) {

        } else if (requestCode == NEW_MESSAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String uID = data.getStringExtra("uID");
            FirebaseFirestore.getInstance().collection("Users").document(uID)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot snapshot = task.getResult();
                        String uID1 = snapshot.getId();
                        String name = snapshot.getString("name");
                        String username = snapshot.getString("username");
                        String imageUrl = snapshot.getString("imageUrl");

                        ChatItem chatItem = new ChatItem("No messages yet.",
                                uID1, name, username, imageUrl,
                                userUID, null, null, null);

                        FirebaseFirestore.getInstance().collection("Previews")
                                .document(userUID).collection("ChatPreviews")
                                .document(uID1).set(chatItem);
                    });
        } else if (requestCode == NEW_MESSAGE_REQUEST_CODE && resultCode == 55) {
            // has intent with string extras: groupID, groupName, and groupImageUrl
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void getMessageList(final FriendListAdapter friendListAdapter) {
        // clear previous data from ListView Adapter
        mFriendListAdapter.clearData();

        // get list of message previews from Firestore and update ListView
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Previews")
                .document(userUID)
                .collection("ChatPreviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    mFriendListAdapter.clearData();
                    for (DocumentSnapshot q: queryDocumentSnapshots) {
                        // turn DocumentSnapshot into ChatItem
                        ChatItem chatItem = q.toObject(ChatItem.class);

                        // get the properties needed to make a new FriendListItem
                        String uId = chatItem.getSenderID();
                        String lastMessage = chatItem.getMessageBody();
                        String name, imageUrl;

                        // if the sender of this ChatItem is the current user, get the profile
                        // of the receiver instead
                        if (uId.equals(userUID) || chatItem.getReceiverID().startsWith("GROUP_")) {
                            uId =chatItem.getReceiverID();
                            name = chatItem.getReceiverName();
                            imageUrl = chatItem.getReceiverImageUrl();
                            // if the sender is not the user, use the sender's profile
                        } else {
                            name = chatItem.getSenderName();
                            imageUrl = chatItem.getSenderImageUrl();
                        }

                        // create a new FriendListItem and add to ListView
                        FriendListItem friendListItem = new FriendListItem(name, lastMessage, uId, imageUrl);
                        friendListAdapter.addAdapterItem(friendListItem);
                        friendListAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void addListener(final String groupID) {
        final ChatItem[] chatItem = new ChatItem[1];
        FirebaseFirestore.getInstance()
                .collection("GroupMessages")
                .document(groupID)
                .collection("ChatHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        chatItem[0] = queryDocumentSnapshots.getDocuments().get(0).toObject(ChatItem.class);
                        // Check if the message is not a text message
                        if (chatItem[0].getMessageBody().startsWith(chatItem[0].getSenderID())) {
                            String[] arr = chatItem[0].getMessageBody().split("\t");
                            if (arr[1].equals("IMAGE")) {
                                chatItem[0].setMessageBody("[Image]");
                            } else if (arr[1].equals("PDF")) {
                                chatItem[0].setMessageBody("[Pdf]");
                            } else if (arr[1].equals("INFO")) {
                                chatItem[0].setMessageBody(arr[2]);
                            } else {
                                chatItem[0].setMessageBody("[Unknown file type]");
                            }

                        }

                        if (chatItem[0].getMessageBody().length() > 43) {
                            chatItem[0].setMessageBody(chatItem[0]
                                    .getMessageBody().substring(0, 40) + "...");
                        }
                        FirebaseFirestore.getInstance()
                                .collection("Previews")
                                .document(userUID)
                                .collection("ChatPreviews")
                                .document(groupID)
                                .set(chatItem[0]);
                    }
                });
    }


    @Override
    protected void onStop() {
        Log.d("HEY", "onStop called");
        Intent i = new Intent(this, ChatService.class);
        i.putExtra("id", userUID);
        this.startService(i);
        super.onStop();
    }
}

// Changing image into bitmap from firebase storage
//
//            try {
//                StorageReference storageRef = FirebaseStorage.getInstance().getReference("Users")
//                        .child(user.getUid()).child("profilepic").child("image.jpg");
//
//                final ImageButton imgbut = (ImageButton) hView.findViewById(R.id.profile_edit_button);
//
//                storageRef.getBytes(1000 * 1000)
//                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                            @Override
//                            public void onSuccess(byte[] bytes) {
//                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                DisplayMetrics dm = new DisplayMetrics();
//                                getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//                                imgbut.setMinimumHeight(dm.heightPixels);
//                                imgbut.setMinimumWidth(dm.widthPixels);
//                                imgbut.setImageBitmap(bm);
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MainActivity.this, "no image detected", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (Exception e) {
//                Toast.makeText(MainActivity.this, "no file path detected", Toast.LENGTH_SHORT).show();
//            }


// Updating profile picture and username from firestore

//        // get user's UID
//        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        // get user's display name and profile picture
//        FirebaseFirestore.getInstance().collection("Users")
//                .document(userUID)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        userName[0] = task.getResult().getString("name");
//                        userUsername[0] = task.getResult().getString("username");
//                        userImageUrl[0] = task.getResult().getString("imageUrl");
//                    }
//                });
