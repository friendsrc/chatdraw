package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.chatdraw.Contacts.FriendListAdapter;
import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;

public class NewMessageActivity extends AppCompatActivity {

    private FriendListAdapter mFriendListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        // Reuse the FriendListAdapter from FriendListActivity
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;

        // Testing the custom adapter
        for (int i = 1; i < 10; i++) {
            updateListView(friendListAdapter, "Person " + i + " the second", "[status]" ,R.drawable.blank_account);
        }

        // set onClickListener on the listView
        // if clicked, go back to MainActivity
        // put extra containing the name of the clicked profile
        ListView listView = findViewById(R.id.new_message_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create Intent to send selected friend's name back to MainActivity
                Intent intent = new Intent();
                FriendListItem friendListItem = (FriendListItem) friendListAdapter.getItem(position);
                intent.putExtra("name", friendListItem.getName());

                // set the result as successful
                setResult(Activity.RESULT_OK, intent);

                // destroy this activity
                finish();
            }
        });

        LinearLayout linearLayout = findViewById(R.id.new_group_chat_linearlayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMessageActivity.this, NewGroupActivity.class);
                startActivity(intent);
            }
        });

        // set the action bar title
        getSupportActionBar().setTitle("New Message");

        // add a back button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    public void updateListView(FriendListAdapter friendListAdapter, String name, String status, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.new_message_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, status, imageID);
        friendListAdapter.addAdapterItem(newFriend);

        // set the adapter to the ListView
        listView.setAdapter(friendListAdapter);
    }
}
