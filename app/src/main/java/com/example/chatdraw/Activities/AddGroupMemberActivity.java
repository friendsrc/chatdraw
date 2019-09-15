package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.chatdraw.Adapters.RecyclerViewAdapter;
import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddGroupMemberActivity extends AppCompatActivity implements RecyclerViewClickListener{

    private static final String TAG = "AddGroupMemberActivity";

    private String currentUserID;
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
        setContentView(R.layout.activity_add_group_member);
        groupUID = getIntent().getStringExtra("groupUID");
        groupName = getIntent().getStringExtra("groupName");
        groupImageUrl = getIntent().getStringExtra("groupImageUrl");
        groupMembers = getIntent().getStringArrayListExtra("groupMembers");

        // get the current user's uID
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(AddGroupMemberActivity.this);
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

        // set the RecyclerView
        recyclerView = findViewById(R.id.new_member_contacts_recycler_view);

        // use this setting to improve performance if changes in content do not change
        // the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        friendList = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(friendList, this, this);
        recyclerView.setAdapter(mAdapter);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add member");
        }

        getContactsNotInGroup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        FriendListItem friendListItem = mAdapter.getItem(position);
        String newMemberUID = friendListItem.getUID();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups")
                .document(groupUID)
                .update("members", FieldValue.arrayUnion(newMemberUID));
        groupMembers.add(newMemberUID);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(newMemberUID)
                .update("groups", FieldValue.arrayUnion(groupUID))
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("contacts", new ArrayList<String>());
                        map.put("groups", new ArrayList<String>());
                        db.collection("Users").document(newMemberUID).set(map);
                        db.collection("Users")
                                .document(newMemberUID)
                                .update("groups", FieldValue.arrayUnion(groupUID))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AddGroupMemberActivity.this,
                                                "Contact added successfully.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

        // create a placeholder chat item
        ChatItem chatItem = new ChatItem("", groupUID, groupName,
                null, groupImageUrl, newMemberUID,
                null, null, null);

        // Send to user's message preview collection
        FirebaseFirestore.getInstance()
                .collection("Previews")
                .document(newMemberUID)
                .collection("ChatPreviews")
                .document(groupUID)
                .set(chatItem);

        Intent intent = new Intent();
        intent.putExtra("newMemberUID", newMemberUID);
        setResult(2143, intent);
        finish();
    }

    public void getContactsNotInGroup() {
        Log.d(TAG, "Getting group members from cache");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.disableNetwork();
        FirebaseDatabase.getInstance().goOffline();
        db.collection("Users")
                .document(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                            if (arr != null) {
                                for (String s : arr) {
                                    if (!groupMembers.contains(s)) {
                                        addContactWithID(s);
                                    }
                                }
                            }
                            db.enableNetwork();
                        } else {
                            getContactsNotInGroupFromFirestore();
                        }
                    }
                });
    }

    public void getContactsNotInGroupFromFirestore() {
        Log.d(TAG, "Getting group members from firestore");
        final FirebaseFirestore db  = FirebaseFirestore.getInstance();
        db.enableNetwork();
        FirebaseDatabase.getInstance().goOnline();
        db.collection("Users")
                .document(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
                            if (arr != null ) {
                                for (String s: arr) {
                                    addContactWithID(s);
                                }
                            }
                        }
                    }
                });
    }

    private void addContactWithID(final String uID) {
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
}
