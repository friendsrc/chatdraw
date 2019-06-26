package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatdraw.AccountActivity.ProfileEditActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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

import com.example.chatdraw.Chat.ChatActivity;
import com.example.chatdraw.Contacts.FriendListActivity;
import com.example.chatdraw.Contacts.FriendListAdapter;
import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FriendListAdapter mFriendListAdapter;
    private static final String TAG = "MainActivity";
    private static final int FIND_FRIEND_REQUEST_CODE = 101;
    private static final int NEW_MESSAGE_REQUEST_CODE = 808;
    private static final int FIND_SETTINGS_REQUEST_CODE = 909;
    private DrawerLayout drawer;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;

        getMessageList(friendListAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.setCheckedItem(R.id.nav_contacts);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        final View hView = navigationView.getHeaderView(0);


        if (user != null) {
            Log.d("userid", user.getUid());

            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String imgurl = (String) dataSnapshot.child(user.getUid()).child("uploads").child("imageUrl").getValue();

                    ImageButton imgbut = (ImageButton) hView.findViewById(R.id.profile_edit_button);
                    Picasso.get()
                            .load(imgurl)
                            .fit()
                            .into(imgbut);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

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

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String profilename = (String) dataSnapshot.child(user.getUid()).child("name").getValue();
                    String username = (String) dataSnapshot.child(user.getUid()).child("username").getValue();

                    TextView tv = (TextView) hView.findViewById(R.id.profile_field);
                    tv.setText(profilename);

                    TextView tv1 = (TextView) hView.findViewById(R.id.username_field);
                    tv1.setText(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        getSupportActionBar().setTitle("ChatDraw");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageView newChatImageView = findViewById(R.id.main_chat_add_message_imageview);
        newChatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, NewMessageActivity.class);
            startActivityForResult(intent, NEW_MESSAGE_REQUEST_CODE);
            }
        });

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

        ImageButton imgbutt = (ImageButton) hView.findViewById(R.id.profile_edit_button);
        imgbutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
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
                            String name = (String) snapshot.get("name");
                            updateListView(mFriendListAdapter, uID, name, "No messages yet.", R.drawable.blank_account);

                        }
                    });
            // TODO get messages > this user > username
        }
    }

    public void updateListView(FriendListAdapter friendListAdapter, String uID, String name, String messagePreview, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.main_chat_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, messagePreview, imageID, uID);
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "is this path valid " + db.collection("Messages/29UJzPD9tJYLRkfYmKgJbgtDAXt2/Friends").get());
        Log.d(TAG, "getting message list for " + FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("Messages")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "get " + task.getResult().getDocuments());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String friendsID = document.getId();
                                Log.d(TAG, "adding document + " +friendsID);
                                updateListView(friendListAdapter, friendsID, friendsID, "No messages yet.", R.drawable.blank_account);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}