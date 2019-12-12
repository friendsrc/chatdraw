package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Adapters.RecyclerViewAdapter;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class NewGroupActivity extends AppCompatActivity implements RecyclerViewClickListener {

    public static final int GROUP_CREATE_REQUEST_CODE = 1001;

    public static final String TAG = "NewGroupActivity";

    public String userUID;
    private HashMap<Integer, FriendListItem> chosenContacts;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<FriendListItem> myDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        recyclerView = findViewById(R.id.new_group_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        myDataset = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(myDataset, NewGroupActivity.this, this);
        recyclerView.setAdapter(mAdapter);

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("New Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // create a hashmap to store chosen contacts
        chosenContacts = new HashMap<>();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(NewGroupActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // get contacts from Firebase
        getContacts();


        ImageView imageView = findViewById(R.id.new_group_nextbutton_imageview);
        imageView.setOnClickListener(v -> {
            if (chosenContacts.size() > 0) {
                Intent intent = new Intent(NewGroupActivity.this, GroupCreateActivity.class);
                String[] memberList = new String[chosenContacts.size()];
                int i = 0;
                for (FriendListItem f: chosenContacts.values()) {
                    memberList[i] = f.getUID();
                    i++;
                }
                intent.putExtra("memberList", memberList);
                startActivityForResult(intent, GROUP_CREATE_REQUEST_CODE);
            } else {
                Toast.makeText(NewGroupActivity.this,
                        "Select at least one group member", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void getContactsFromFirestore() {
        Log.d(TAG, "Getting contacts from firestore");
        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.enableNetwork();
        FirebaseDatabase.getInstance().goOnline();
        db.collection("Users")
                .document(userUID)
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
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.disableNetwork();
        FirebaseDatabase.getInstance().goOffline();
        db.collection("Users")
                .document(userUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                        if (arr != null) {
                            for (String s : arr) {
                                addUserWithID(s);
                            }
                        }
                        db.enableNetwork();
                    } else {
                        getContactsFromFirestore();
                    }
                });
    }

    private void addUserWithID(final String uID) {
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(uID).child("name").getValue();
                String status = (String) dataSnapshot.child(uID).child("status").getValue();
                String imageURL = (String) dataSnapshot.child(uID).child("imageUrl").getValue();

                if (status == null) status = "";

                FriendListItem newFriend = new FriendListItem(name, status, uID, imageURL);
                mAdapter.addItem(newFriend);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        FriendListItem friendListItem = mAdapter.getItem(position);
        if (chosenContacts.containsKey(position)) {
            chosenContacts.remove(position);
            v.setBackgroundColor(Color.TRANSPARENT);
        } else {
            chosenContacts.put(position, friendListItem);
            v.setBackgroundColor(getResources().getColor(R.color.bluegray100));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GROUP_CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

}
