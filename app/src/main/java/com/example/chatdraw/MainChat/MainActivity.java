package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatdraw.AccountActivity.ProfileEditActivity;
import com.example.chatdraw.AccountActivity.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
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

import com.example.chatdraw.AccountActivity.SettingsActivity;
import com.example.chatdraw.Chat.ChatActivity;
import com.example.chatdraw.Contacts.FindFriendActivity;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FriendListAdapter mFriendListAdapter;
    private static final int FIND_FRIEND_REQUEST_CODE = 101;
    private static final int NEW_MESSAGE_REQUEST_CODE = 808;
    private static final int FIND_SETTINGS_REQUEST_CODE = 909;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;

        // Testing the custom adapter
        for (int i = 1; i < 3; i++) {
            updateListView(friendListAdapter, "Person " + i,
                    "Hi, I write this text.", R.drawable.blank_account);
        }

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
            Log.d("HEY", "uID is " + uID);
            FirebaseFirestore.getInstance().collection("Users").document(uID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            String name = (String) snapshot.get("name");
                            updateListView(mFriendListAdapter, name, "No messages yet.", R.drawable.blank_account);

                        }
                    });
            // TODO get messages > this user > username
        }
    }

    public void updateListView(FriendListAdapter friendListAdapter, String name, String messagePreview, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.main_chat_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, messagePreview, imageID);
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

}