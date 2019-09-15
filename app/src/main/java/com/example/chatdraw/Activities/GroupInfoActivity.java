package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.Adapters.RecyclerViewAdapter;
import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity implements RecyclerViewClickListener{

    private static final String TAG = "GroupInfoActivity";
    public static int INFO_EDIT_REQUEST_CODE = 10191;

    private String groupUID;
    private String groupName;
    private String groupImageUrl;
    private ArrayList<String> groupMembers;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<FriendListItem> friendList;

    // Adapters for RecyclerView
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        // set the RecyclerView
        recyclerView = findViewById(R.id.add_group_member_recycler_view);

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

        Intent intent = getIntent();
        groupUID = intent.getStringExtra("id");
        getMembers();

        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupUID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // get group data
                        groupName = documentSnapshot.get("groupName").toString();
                        if (documentSnapshot.get("groupImageUrl") != null) {
                            groupImageUrl = documentSnapshot.get("groupImageUrl").toString();
                        }
                        groupMembers = (ArrayList<String>) documentSnapshot.get("members");

                        // set the toolbar
                        Toolbar myToolbar = findViewById(R.id.my_toolbar);
                        setSupportActionBar(myToolbar);

                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setTitle("Group Info");
                        }
                    }
                });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_group_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_info_add_member:
                Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_info_edit:
                Intent intent = new Intent(GroupInfoActivity.this, GroupInfoEditActivity.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupUID", groupUID);
                intent.putExtra("groupImageUrl", groupImageUrl);
                startActivityForResult(intent, INFO_EDIT_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INFO_EDIT_REQUEST_CODE && resultCode == 1000) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getMembers() {
        Log.d(TAG, "Getting contacts from cache");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.disableNetwork();
        FirebaseDatabase.getInstance().goOffline();
        db.collection("Groups")
                .document(groupUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("members");
                            if (arr != null) {
                                for (String s : arr) {
                                    addMemberWithID(s);
                                }
                            }
                            db.enableNetwork();
                        } else {
                            getContactsFromFirestore();
                        }
                    }
                });
    }

    public void getContactsFromFirestore() {
        Log.d(TAG, "Getting contacts from firestore");
        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.enableNetwork();
        FirebaseDatabase.getInstance().goOnline();
        db.collection("Groups")
                .document(groupUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("members");
                            if (arr != null ) {
                                for (String s: arr) {
                                    addMemberWithID(s);
                                }
                            }
                        }
                    }
                });
    }

    private void addMemberWithID(final String uID) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

    }
}
