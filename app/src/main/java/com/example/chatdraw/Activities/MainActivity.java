package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatdraw.AccountActivity.ProfileEditActivity;
import com.example.chatdraw.Items.ChatItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
//        navigationView.setCheckedItem(R.id.nav_contacts);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        final View hView = navigationView.getHeaderView(0);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

                String imgurl;

                if (acct != null) {
                    imgurl = (String) dataSnapshot.child(acct.getId()).child("imageUrl").getValue();
                } else {
                    imgurl = (String) dataSnapshot.child(user.getUid()).child("imageUrl").getValue();
                }

                ImageButton imgbut = (ImageButton) hView.findViewById(R.id.profile_edit_button);

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

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                String tempName = "";
                String tempUsername = "";

                if (acct != null) {
                    String uID = acct.getId();

                    if (uID != null) {
                        tempName = (String) dataSnapshot.child(uID).child("name").getValue();
                        tempUsername = (String) dataSnapshot.child(uID).child("username").getValue();
                    } else {
                        Toast.makeText(MainActivity.this, "Error on user validation", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tempName = (String) dataSnapshot.child(user.getUid()).child("name").getValue();
                    tempUsername = (String) dataSnapshot.child(user.getUid()).child("username").getValue();
                }

                TextView tv = (TextView) hView.findViewById(R.id.profile_field);
                tv.setText(tempName);

                TextView tv1 = (TextView) hView.findViewById(R.id.username_field);
                tv1.setText(tempUsername);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        newChatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
            startActivityForResult(intent, NEW_MESSAGE_REQUEST_CODE);
            }
        });

        ImageButton imgbutt = (ImageButton) hView.findViewById(R.id.profile_edit_button);
        imgbutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // get Messages Previews from Firestore
        getMessageList(mFriendListAdapter);

        // set on click listener to the ListView
        ListView listView = findViewById(R.id.main_chat_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                FriendListItem friendListItem = (FriendListItem) mFriendListAdapter.getItem(position);
                intent.putExtra("name", friendListItem.getName());
                intent.putExtra("uID", friendListItem.getUID());
                startActivity(intent);
            }
        });
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_calls:
                Toast.makeText(this,"Not yet configured", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_contacts:
                Intent intent  = new Intent(this, FriendListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_invites:
                Toast.makeText(this,"Not yet configured", Toast.LENGTH_SHORT).show();
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
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            String uID = snapshot.getId();
                            String name = snapshot.getString("name");
                            String imageUrl = snapshot.getString("imageUrl");
                            updateListView(mFriendListAdapter, uID, name, "No messages yet.", imageUrl);
                        }
                    });
        } else if (requestCode == NEW_MESSAGE_REQUEST_CODE && resultCode == 55) {
//            String groupID = data.getStringExtra("groupID");
//            String groupName = data.getStringExtra("groupName");
//            String groupImageUrl = data.getStringExtra("groupImageUrl");
        }
    }

    public void updateListView(FriendListAdapter friendListAdapter, String uID, String name, String messagePreview, String imageUrl) {
        Log.d("HEY", "updating with id = " + uID);
        // find the friend list ListView
        ListView listView = findViewById(R.id.main_chat_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, messagePreview, uID, imageUrl);
        friendListAdapter.addAdapterItem(newFriend);

        // set the adapter to the ListView
        listView.setAdapter(friendListAdapter);
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

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            if (userUID == null) {
                userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                Toast.makeText(MainActivity.this, "User not validated", Toast.LENGTH_SHORT).show();
            }
        }

        // get list of message previews from Firestore and update ListView
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Previews")
                .document(userUID)
                .collection("ChatPreviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
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
                    }
                });
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
