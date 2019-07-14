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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

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
        mAdapter = new RecyclerViewAdapter(friendList, this, new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                // do nothing
            }
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
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
            }
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

        // get Groups list from Firebase
        getGroups();

        // set toggle switch
        ToggleSwitch toggleSwitch = findViewById(R.id.friend_list_toggleswitch);
        final RecyclerView recyclerView = findViewById(R.id.friend_list_recycler_view);
        toggleSwitch.setCheckedPosition(0);
        toggleSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                if (i == 1) {
                    recyclerView.setAdapter(mGroupAdapter);
                } else {
                    recyclerView.setAdapter(mAdapter);
                }
            }
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
            addUserWithID(uID);
            getContacts();
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

                friendList.add(newFriend);
                mAdapter.addItem(newFriend);
                mAdapter.notifyDataSetChanged();

                if (currentUserID == null) {
                    return;
                }

                try {
                    FileInputStream fis = getApplicationContext().openFileInput("FRIEND" + currentUserID);
                    ObjectInputStream oi = new ObjectInputStream(fis);
                    ArrayList<FriendListItem> savedFriendList = (ArrayList<FriendListItem>) oi.readObject();
                    friendList.addAll(savedFriendList);

                    FileOutputStream fos = getApplicationContext().openFileOutput("FRIEND" + currentUserID, MODE_PRIVATE);
                    ObjectOutputStream of = new ObjectOutputStream(fos);
                    of.writeObject(friendList);
                    of.close();
                    fos.close();
                }
                catch (Exception e) {
                    Log.e("InternalStorage", e.getMessage());
                }

                // Returns a special value that can be used with set() or update() that
                // tells the server to union the given elements with any array value
                // that already exists on the server.
                FirebaseFirestore.getInstance().collection("Users")
                        .document(currentUserID)
                        .update("contacts", FieldValue.arrayUnion(uID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getContacts() {
        try {
            FileInputStream fis = getApplicationContext().openFileInput("FRIEND" + currentUserID);
            ObjectInputStream oi = new ObjectInputStream(fis);
            ArrayList<FriendListItem> friendList = (ArrayList<FriendListItem>) oi.readObject();
            mAdapter.clearData();
            mAdapter.addAll(friendList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("InternalStorage", e.getMessage());
            // if file doesn't exist, get data from Firestore
            FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getGroups() {
        if (currentUserID == null) {
            return;
        }

        // specify an adapter
        ArrayList<NewFriendItem> myDataset = new ArrayList<>();
        final GroupListRecyclerViewAdapter adapter
                = new GroupListRecyclerViewAdapter(myDataset, FriendListActivity.this, this);
        mGroupAdapter = adapter;

        FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
                        if (arr != null && !arr.isEmpty()) {
                            for (String s: arr) {
                                final String groupID = s;
                                FirebaseFirestore.getInstance().collection("Groups").document(groupID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot doc = task.getResult();
                                                if (doc == null) {
                                                    return;
                                                }

                                                String name = (String) doc.get("groupName");
                                                String imageURL = (String) doc.get("groupImageUrl");

                                                NewFriendItem newFriendItem = new NewFriendItem(name, groupID, imageURL);
                                                adapter.addData(newFriendItem);
                                            }
                                        });

                            }
                        }
                    }

                });


    }

    @Override
    public void recyclerViewListClicked(View v, int position){

    }
}
