package com.example.chatdraw.Contacts;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatdraw.MainChat.MainActivity;
import com.example.chatdraw.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class FriendListActivity extends AppCompatActivity {

    private FriendListAdapter mFriendListAdapter;
    private static final int FIND_FRIEND_REQUEST_CODE = 101;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Create a custom adapter for the friend list ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;

        // Set the "add" button to go to the FindFriendActivity
        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
            }
        });


        // Testing the custom adapter
        for (int i = 1; i < 3; i++) {
            updateListView(friendListAdapter, "Person " + i,
                    "[status]", R.drawable.friends_icon);
        }

        // set the Action Bar title
        getSupportActionBar().setTitle("Contacts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            updateListView(mFriendListAdapter, name, "[status]", R.drawable.common_google_signin_btn_icon_dark);
//                try {
//                    OutputStream outputStream = this.openFileOutput("messages.txt", MODE_APPEND);
//                    PrintStream output = new PrintStream(outputStream);
//                    output.println(name + "\t" + "No message sent yet");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
        }
    }

    public void updateListView(FriendListAdapter friendListAdapter, String name, String status, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.friend_list_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, status, imageID);
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
                        savedChat[1], R.drawable.friends_icon);            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //To Do check firebase data
    }
}
