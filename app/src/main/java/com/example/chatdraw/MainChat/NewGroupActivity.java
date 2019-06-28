package com.example.chatdraw.MainChat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.HashMap;

public class NewGroupActivity extends AppCompatActivity {

    public static final String TAG = "NewGroupActivity";
    private FriendListAdapter mFriendListAdapter;
    private ListView mListView;
    private HashMap<String, FriendListItem> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        // set the action bar
        getSupportActionBar().setTitle("New Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set adapter on ListView
        mFriendListAdapter = new FriendListAdapter(this);
        mListView = findViewById(R.id.new_group_listview);
        contacts = new HashMap<>();

        // get contacts from Firebase
        getContacts();

        // get layout
        final LinearLayout layout = findViewById(R.id.new_group_layout);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: create proper layout
                FriendListItem friendListItem = (FriendListItem) mFriendListAdapter.getItem(position);
                View viewItem = getLayoutInflater().inflate(R.layout.new_group_contact_item, layout);
                TextView textView = viewItem.findViewById(R.id.new_group_contact_textview);
                textView.setText(friendListItem.getName());
            }
        });

    }

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void getContacts() {
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
                                addUserWithID(s);
                            }
                        }
                    }
                });
    }

    private void addUserWithID(final String uID) {
        FirebaseFirestore.getInstance().collection("Users").document(uID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        String name = (String) doc.get("name");
                        String status = (String) doc.get("status");
                        String imageURL = (String) doc.get("imageUrl");

                        // check if the user doesn't have name/status/imageURL
                        if (status == null) status = "[status]";

                        // add the contact to ListView
                        FriendListItem friendListItem
                                = updateListView(name, status, uID, imageURL);

                        // get the current user's uID
                        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseFirestore.getInstance().collection("Users")
                                .document(currentUserID)
                                .update("contacts", FieldValue.arrayUnion(uID));
                    }
                });
    }

    public FriendListItem updateListView(String name, String status, String uid, String imageUrl) {
        // find the friend list ListView
        if (mListView == null) mListView = findViewById(R.id.new_group_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, status, uid ,imageUrl);
        mFriendListAdapter.addAdapterItem(newFriend);

        // set the adapter to the ListView
        mListView.setAdapter(mFriendListAdapter);
        return newFriend;
    }
}
