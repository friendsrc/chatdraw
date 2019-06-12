package com.example.chatdraw.FriendListActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
            updateListView(newFriendAdapter, "Person " + i + " the second", R.drawable.common_google_signin_btn_icon_dark_focused);
        }

        // set onClickListener on the listView
        // if clicked, go back to FriendListActivity
        // put extra containing the name of the clicked profile
        ListView listView = findViewById(R.id.find_friend_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FindFriendActivity.this, FriendListActivity.class);
                NewFriendItem newFriendItem = (NewFriendItem) newFriendAdapter.getItem(position);
                intent.putExtra("name", newFriendItem.getName());
                startActivity(intent);
                finish();
            }
        });
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
