package com.example.chatdraw.FriendListActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chatdraw.R;

import org.w3c.dom.Text;

public class FindFriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        super.onStart();
        // Create a custom adapter for the friend list ListView
        final NewFriendAdapter newFriendAdapter = new NewFriendAdapter(this);

        // Testing the custom adapter
        for (int i = 1; i < 10; i++) {
            updateListView(newFriendAdapter, "Person " + i, R.drawable.friends_icon);
        }
    }

    public void updateListView(NewFriendAdapter newFriendAdapter, String name, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.find_friend_listview);
        TextView textView = findViewById(R.id.find_friend_textview);

        // Instantiate a new NewFriendItem and add it to the custom adapter
        NewFriendItem newFriendItem = new NewFriendItem(name, imageID);
        newFriendAdapter.addAdapterItem(newFriendItem);

        // set the adapter to the ListView
        listView.setAdapter(newFriendAdapter);
    }
}
