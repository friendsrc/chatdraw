package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail();
            View hView = navigationView.getHeaderView(0);
            TextView tv = (TextView) hView.findViewById(R.id.email_field);
            tv.setText(userEmail);
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
                Intent intent_invite  = new Intent(this, FindFriendActivity.class);
                startActivityForResult(intent_invite, FIND_FRIEND_REQUEST_CODE);
                break;
            case R.id.nav_settings:
                Intent intent_settings = new Intent(this, SettingsActivity.class);
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
//            String name = data.getStringExtra("name");
//            updateListView(mFriendListAdapter, name, "[status]", R.drawable.common_google_signin_btn_icon_dark);
//                try {
//                    OutputStream outputStream = this.openFileOutput("messages.txt", MODE_APPEND);
//                    PrintStream output = new PrintStream(outputStream);
//                    output.println(name + "\t" + "No message sent yet");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
        } else if (requestCode == FIND_SETTINGS_REQUEST_CODE) {

        } else if (requestCode == NEW_MESSAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            updateListView(mFriendListAdapter, name, "No messages yet.", R.drawable.blank_account);
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

    // Check if there exist saved information of the friends list in the phone (not yet working)
    public void checkSavedMessages(FriendListAdapter friendListAdapter) {
        try {
            // get saved file and create its Scanner
            InputStream inputStream = this.openFileInput("messages.txt");
            Scanner scan = new Scanner(inputStream);

            // update the listView
            String[] savedChat = scan.nextLine().split("\t");
            while (scan.hasNext()) {
                updateListView(friendListAdapter, savedChat[0],
                        savedChat[1], R.drawable.blank_account);            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //To Do check firebase data
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