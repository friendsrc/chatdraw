package com.example.chatdraw.Contacts;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FindFriendActivity extends AppCompatActivity {

    private static String TAG = "FindFriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        super.onStart();
        // Create a custom adapter for the friend list ListView
        final NewFriendAdapter newFriendAdapter = new NewFriendAdapter(this);


        // set onClickListener on the listView
        // if clicked, go back to FriendListActivity
        // put extra containing the name of the clicked profile
        ListView listView = findViewById(R.id.find_friend_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create Intent to send selected friend's name back to FriendListActivity
                Intent intent = new Intent();
                NewFriendItem newFriendItem = (NewFriendItem) newFriendAdapter.getItem(position);
                intent.putExtra("name", newFriendItem.getName());
                intent.putExtra("username", newFriendItem.getUsername());
                // TODO send friend's imageID

                // set the result as successful
                setResult(Activity.RESULT_OK, intent);

                // destroy this activity
                finish();
            }
        });

        // set the action bar title
        getSupportActionBar().setTitle("Find Friends");

        // add a back button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // add listener on the edit text
        final EditText editText = findViewById(R.id.find_friend_edittext);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (!str.trim().equals("")) {
                    findUserInDatabase(newFriendAdapter, str);  // find user and update listview
                }
            }
        });
    }

    public void findUserInDatabase(final NewFriendAdapter newFriendAdapter, final String text) {
        // create a listener to get the data from firebase
        // the listener checks if the User's username contains the inputted text
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (count >= 20) break;
                    User user = ds.getValue(User.class);
                    String username = user.getUsername();
                    if (username.contains(text)) {
                        count++;
                        updateListView(newFriendAdapter, user.getName(), user.getUsername(), R.drawable.blank_account);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // clean the listview previous data
        newFriendAdapter.clearData();

        // get the User whose username is equal to the inputted text
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabase.orderByChild("username").equalTo("text").addValueEventListener(valueEventListener);

        // get Users whose username contains the inputted text
        usersDatabase.orderByChild("username").addValueEventListener(valueEventListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    public void updateListView(NewFriendAdapter newFriendAdapter, String name, String username, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.find_friend_listview);

        // Instantiate a new NewFriendItem and add it to the custom adapter
        NewFriendItem newFriendItem = new NewFriendItem(name, username, imageID);
        newFriendAdapter.addAdapterItem(newFriendItem);

        // set the adapter to the ListView
        listView.setAdapter(newFriendAdapter);
    }
}
