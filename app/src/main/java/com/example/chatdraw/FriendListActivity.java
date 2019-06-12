package com.example.chatdraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Create a custom adapter for the friend list ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);

        // Testing the custom adapter
        for (int i = 1; i < 10; i++) {
            updateListView(friendListAdapter, "Person " + i,
                    "This is my chat preview. Have a good day!", R.drawable.friends_icon);
            updateListView(friendListAdapter,"Person " + i + " the second ",
                    "This is my chat preview...", R.drawable.common_google_signin_btn_icon_dark);
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
}
