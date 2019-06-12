package com.example.chatdraw.FriendListActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.chatdraw.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Create a custom adapter for the friend list ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);

        // Set the "add" button to go to the FindFriendActivity
        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivity(intent);
            }
        });

        // get the Intent and add the newly added friend
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            // Save name into the phone memory
            if (name != null && !name.equals("")) {
                updateListView(friendListAdapter, name, "No message sent yet...", R.drawable.common_google_signin_btn_icon_dark);
//                try {
//                    OutputStream outputStream = this.openFileOutput("messages.txt", MODE_APPEND);
//                    PrintStream output = new PrintStream(outputStream);
//                    output.println(name + "\t" + "No message sent yet");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
            }
        }

//        checkSavedMessages(friendListAdapter);

        // Testing the custom adapter
        for (int i = 1; i < 21; i++) {
            updateListView(friendListAdapter, "Person " + i,
                    "This is my chat preview. Have a good day!", R.drawable.friends_icon);
        }

    }

    public void updateListView(FriendListAdapter friendListAdapter, String name, String chatPreview, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.friend_list_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, chatPreview, imageID);
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
