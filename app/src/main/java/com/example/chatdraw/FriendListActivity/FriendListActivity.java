package com.example.chatdraw.FriendListActivity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.chatdraw.R;

public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Create a custom adapter for the friend list ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);

        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivity(intent);
            }
        });

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
