package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.Contacts.FriendListAdapter;
import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewMessageActivity extends AppCompatActivity {

    public static final String TAG = "NewMessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        // Reuse the FriendListAdapter from FriendListActivity
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);

        // get Contacts list
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String id = currentFirebaseUser.getUid();
        FirebaseFirestore.getInstance().collection("Users").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                        if (arr != null && !arr.isEmpty()) {
                            for (String s: arr) {
                                addUserWithUsername(s, friendListAdapter);
                            }
                        }
                    }
                });

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
                intent.putExtra("username", friendListItem.getUsername());

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

    private void addUserWithUsername(final String username, final FriendListAdapter friendListAdapter) {
        Log.d(TAG, "adding with username = " + username);
        FirebaseFirestore.getInstance().collection("Users").whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d(TAG, task.toString());
                        // get name and status from firestore
                        List<User> users = task.getResult().toObjects(User.class);
                        User newFriend = users.get(0);
                        String name = newFriend.getName();

                        // TODO get profile picture and status from firestore
                        updateListView(friendListAdapter, username,
                                name, "[status]", R.drawable.blank_account);
                    }
                });
    }

    public FriendListItem updateListView(FriendListAdapter friendListAdapter, String username, String name, String status, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.new_message_listview);
        Log.d(TAG, "updating list view with " + name);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(username, name, status, imageID);
        friendListAdapter.addAdapterItem(newFriend);

        // set the adapter to the ListView
        listView.setAdapter(friendListAdapter);

        return newFriend;
    }
}
