package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatdraw.Adapters.NewFriendAdapter;
import com.example.chatdraw.R;
import com.example.chatdraw.Items.NewFriendItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity {

    private static String TAG = "FindFriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        super.onStart();
        // Create a custom adapter for the friend list ListView
        final NewFriendAdapter newFriendAdapter = new NewFriendAdapter(this);

        // create an onItemClickListener for the ListView
        AdapterView.OnItemClickListener onItemClickListener
                = new AdapterView.OnItemClickListener() {
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
                                    String id;
                                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FindFriendActivity.this);
                                    if (acct != null) {
                                        id = acct.getId();
                                    } else {
                                        id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    }
                                    final String currentUserID = id;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        final String uID = document.getId();
                                        FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(currentUserID)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        ArrayList<String> contacts
                                                                = (ArrayList<String>)
                                                                task.getResult().get("contacts");
                                                        // if the chosen contact already exist in
                                                        // this user's contacts list, make a toast
                                                        if (contacts != null && contacts.contains(uID)) {
                                                            Toast.makeText(
                                                                    FindFriendActivity.this,
                                                                    "Already in Contacts",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        } else  if (uID.equals(currentUserID)) {
                                                            Toast.makeText(
                                                                    FindFriendActivity.this,
                                                                    "Can't add your own account",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
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
        };

        // set onClickListener on the listView
        // if clicked, go back to FriendListActivity
        // put extra containing the name of the clicked profile
        ListView listView = findViewById(R.id.find_friend_listview);
        listView.setOnItemClickListener(onItemClickListener);

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Find Friends");
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
                    // find user and update listview
                    findUserInDatabase(newFriendAdapter, str.trim());
                }
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void findUserInDatabase(final NewFriendAdapter newFriendAdapter, String inputText) {
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
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (count > 20) break;
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                count++;
                                updateListView(
                                        newFriendAdapter,
                                        document.getString("name"),
                                        document.getString("username"),
                                        document.getString("imageUrl"));
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

    public void updateListView(NewFriendAdapter newFriendAdapter, String name,
                               String username, String imageUrl) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.find_friend_listview);

        // Instantiate a new NewFriendItem and add it to the custom adapter
        NewFriendItem newFriendItem = new NewFriendItem(name, username, imageUrl);
        newFriendAdapter.addAdapterItem(newFriendItem);

        // set the adapter to the ListView
        listView.setAdapter(newFriendAdapter);
    }
}
