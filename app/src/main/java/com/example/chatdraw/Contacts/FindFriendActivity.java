package com.example.chatdraw.Contacts;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
                // get clicked item
                NewFriendItem newFriendItem = (NewFriendItem) newFriendAdapter.getItem(position);

                // create Intent to send selected friend's name back to FriendListActivity
                final Intent intent = new Intent();

                // get new friend's uID, add to intent, and then destroy this activity
                FirebaseFirestore.getInstance().collection("Users")
                        .whereEqualTo("username", newFriendItem.getUsername())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        final String uID = document.getId();
                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        ArrayList<String> contacts = (ArrayList<String>) task.getResult().get("contacts");
                                                        // if the chosen contact already exist in this user's contacts list, make a toast
                                                        if (contacts != null && contacts.contains(uID)) {
                                                            Toast.makeText(FindFriendActivity.this, "Already in Contacts", Toast.LENGTH_SHORT).show();
                                                        } else  if (uID.equals(currentUserID)) {
                                                            Toast.makeText(FindFriendActivity.this, "Can't add your own account", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            intent.putExtra("uID", uID);

                                                            // set the result as successful
                                                            setResult(Activity.RESULT_OK, intent);

                                                            // destroy this activity
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });


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
                newFriendAdapter.clearData();
                String str = s.toString();
                if (!str.trim().equals("")) {
                    findUserInDatabase(newFriendAdapter, str.trim());  // find user and update listview
                }
            }
        });
    }

    public void findUserInDatabase(final NewFriendAdapter newFriendAdapter, String inputText) {
//        // create a listener to get the data from Realtime Database
//        // the listener checks if the User's username contains the inputted text
//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                int count = 0;
//                for (DataSnapshot ds: dataSnapshot.getChildren()) {
//                    if (count >= 20) break;
//                    User user = ds.getValue(User.class);
//                    String username = user.getUsername();
//                    if (username.contains(text)) {
//                        count++;
//                        updateListView(newFriendAdapter, user.getName(), user.getUsername(), R.drawable.blank_account);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        };
//
//        // clean the listview previous data
//        newFriendAdapter.clearData();
//
//        // get the User whose username is equal to the inputted text
//        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
//        usersDatabase.orderByChild("username").equalTo("text").addValueEventListener(valueEventListener);
//
//        // get Users whose username contains the inputted text
//        usersDatabase.orderByChild("username").addValueEventListener(valueEventListener);


        // Check if the input start with @
        final String text;
        if (inputText.charAt(0) != '@') {
            text = "@" + inputText;
        }  else {
            text = inputText;
        }

        if (text.length() < 2) return;

        // get users whose usernames contain the inputted text
        char firstChar = text.charAt(1);
        char nextChar = ++firstChar;
        Log.d("HEY", "NEXT CHAER = " + nextChar);
        FirebaseFirestore.getInstance().collection("Users")
                .orderBy("username")
                .startAt(text)
                .endBefore("@" + nextChar)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            newFriendAdapter.clearData();
                            List<User> users = task.getResult().toObjects(User.class);
                            int count = 0;
                            for (User u : users) {
                                updateListView(newFriendAdapter, u.getName(), u.getUsername(), R.drawable.blank_account);
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

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
