package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatdraw.Adapters.GroupListRecyclerViewAdapter;
import com.example.chatdraw.Items.NewFriendItem;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.example.chatdraw.R;
import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.Adapters.RecyclerViewAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendListActivity extends AppCompatActivity implements RecyclerViewClickListener, Serializable {

    private static String TAG = "FriendListActivity";

    private static final int FIND_FRIEND_REQUEST_CODE = 101;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<FriendListItem> friendList;
    private String currentUserID;

    // Adapters for RecyclerView
    private RecyclerViewAdapter mAdapter;
    private GroupListRecyclerViewAdapter mGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // set the RecyclerView
        recyclerView = findViewById(R.id.friend_list_recycler_view);

        // use this setting to improve performance if changes in content do not change
        // the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        friendList = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(friendList, this, (v, position) -> {
            // do nothing
        });
        recyclerView.setAdapter(mAdapter);

        // get the current user's uID
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FriendListActivity.this);
        if (acct != null) {
            currentUserID = acct.getId();
        } else {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                currentUserID = fbUser.getUid();
            } else {
                return;
            }
        }

        // set the "add" button to go to the FindFriendActivity
        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(v -> {
            Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
            startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
        });

        // set the Action Bar title
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Contacts");
        }

        // get Contacts list
        getContacts();
        getGroups();

        // set toggle switch
        Button groupsButton = findViewById(R.id.friend_list_groups_button);
        Button friendsButton = findViewById(R.id.friend_list_friends_button);
        groupsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
        friendsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
        groupsButton.setOnClickListener(x -> {
            recyclerView.setAdapter(mGroupAdapter);
            groupsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
            friendsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
        });
        friendsButton.setOnClickListener(x -> {
            recyclerView.setAdapter(mAdapter);
            groupsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
            friendsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // destroy the activity
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data == null) {
                return;
            }

            final String uID = data.getStringExtra("uID");
            recyclerView.setAdapter(mAdapter);

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users")
                    .document(currentUserID)
                    .update("contacts", FieldValue.arrayUnion(uID))
                    .addOnSuccessListener(aVoid -> Toast.makeText(FriendListActivity.this,
                            "Contact added successfully.",
                            Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("contacts", new ArrayList<String>());
                        map.put("groups", new ArrayList<String>());
                        db.collection("Users").document(currentUserID).set(map);
                        db.collection("Users")
                                .document(currentUserID)
                                .update("contacts", FieldValue.arrayUnion(uID))
                                .addOnSuccessListener(aVoid -> Toast.makeText(FriendListActivity.this,
                                        "Contact added successfully.",
                                        Toast.LENGTH_SHORT).show());
                    });
            addUserWithID(uID);
        }
    }

    private void addUserWithID(final String uID) {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(uID).child("name").getValue();
                String status = (String) dataSnapshot.child(uID).child("status").getValue();
                String imageURL = (String) dataSnapshot.child(uID).child("imageUrl").getValue();

                // check if the user doesn't have name/status/imageURL
                if (status == null) status = "";

                FriendListItem newFriend = new FriendListItem(name, status, uID, imageURL);
                mAdapter.addItem(newFriend);

//                if (currentUserID == null) {
//                    return;
//                }
//
//                try {
//                    FileOutputStream fos = getApplicationContext().openFileOutput("FRIEND" + currentUserID, MODE_PRIVATE);
//                    ObjectOutputStream of = new ObjectOutputStream(fos);
//                    of.writeObject(friendList);
//                    of.close();
//                    fos.close();
//                }
//                catch (Exception e) {
//                    Log.e("InternalStorage", e.getMessage());
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getContactsFromFirestore() {
        Log.d(TAG, "Getting contacts from firestore");
        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.enableNetwork();
        FirebaseDatabase.getInstance().goOnline();
        db.collection("Users")
                .document(currentUserID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                        if (arr != null ) {
                            for (String s: arr) {
                                addUserWithID(s);
                            }
                        }
                    }
                });
    }

    public void getContacts() {
        Log.d(TAG, "Getting contacts from cache");
        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.disableNetwork();
        FirebaseDatabase.getInstance().goOffline();
        db.collection("Users")
                .document(currentUserID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                        if (arr != null ) {
                            for (String s: arr) {
                                addUserWithID(s);
                            }
                        }
                        db.enableNetwork();
                    } else {
                        getContactsFromFirestore();
                    }
                });
    }

    public void getGroups() {
        Log.d(TAG, "Getting groups from cache");

        // specify an adapter
        ArrayList<NewFriendItem> myDataset = new ArrayList<>();
        final GroupListRecyclerViewAdapter adapter
                = new GroupListRecyclerViewAdapter(myDataset, FriendListActivity.this, this);
        mGroupAdapter = adapter;

        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.disableNetwork();
        FirebaseDatabase.getInstance().goOffline();

        FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
                    if (arr != null && !arr.isEmpty()) {
                        for (String s: arr) {
                            final String groupID = s;
                            FirebaseFirestore.getInstance().collection("Groups").document(groupID)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot doc = task1.getResult();
                                            if (doc == null) {
                                                return;
                                            }

                                            String name = (String) doc.get("groupName");
                                            String imageURL = (String) doc.get("groupImageUrl");

                                            NewFriendItem newFriendItem = new NewFriendItem(name, groupID, imageURL);
                                            adapter.addData(newFriendItem);
                                        } else {
                                            getGroupsFromFirestore();
                                        }
                                    });

                        }
                    }
                });
    }

    public void getGroupsFromFirestore() {
        Log.d(TAG, "Getting groups from firestore");

        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.enableNetwork();
        FirebaseDatabase.getInstance().goOnline();

        FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
                .get()
                .addOnCompleteListener(task -> {
                    ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
                    if (arr != null && !arr.isEmpty()) {
                        for (String s: arr) {
                            final String groupID = s;
                            FirebaseFirestore.getInstance().collection("Groups").document(groupID)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot doc = task1.getResult();
                                            if (doc == null) {
                                                return;
                                            }

                                            String name = (String) doc.get("groupName");
                                            String imageURL = (String) doc.get("groupImageUrl");

                                            NewFriendItem newFriendItem = new NewFriendItem(name, groupID, imageURL);
                                            mGroupAdapter.addData(newFriendItem);
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void recyclerViewListClicked(View v, int position){

    }
}
