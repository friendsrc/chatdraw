package com.example.chatdraw.ContactActivites;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatdraw.Adapters.GroupListRecyclerViewAdapter;
import com.example.chatdraw.ChatActivites.ChatActivity;
import com.example.chatdraw.ChatActivites.NewMessageActivity;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendListActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private static String TAG = "FriendListActivity";

    private static final int FIND_FRIEND_REQUEST_CODE = 101;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<FriendListItem> myDataset;
    private boolean isFriendsToggledOff = false;
    private boolean isGroupToggledOff = false;
    private GroupListRecyclerViewAdapter mGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        recyclerView = findViewById(R.id.friend_list_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        myDataset = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(myDataset, this, new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                // do nothing
            }
        });
        recyclerView.setAdapter(mAdapter);

        // Set the "add" button to go to the FindFriendActivity
        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
            }
        });

        // set the Action Bar title
        getSupportActionBar().setTitle("Contacts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get Groups list from Firebase
        getGroups();

        // get Contacts list from Firebase
        getContacts();

        LinearLayout groupToggle = findViewById(R.id.group_toggle);
        groupToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recyclerView = findViewById(R.id.friend_list_group_recycler_view);
                ImageView arrow = findViewById(R.id.group_arrow);
                if (isGroupToggledOff) {
                    recyclerView.setAdapter(mGroupAdapter);
//                    arrow.setImageResource(R.drawable.arrow_drop_down);
                } else {
                    ArrayList<NewFriendItem> emptyDataset = new ArrayList<>();
                    final GroupListRecyclerViewAdapter adapter
                            = new GroupListRecyclerViewAdapter(emptyDataset);
                    recyclerView.setAdapter(adapter);
//                    arrow.setImageResource(R.drawable.arrow_drop_up);
                }
                isGroupToggledOff = !isGroupToggledOff;
            }
        });

        LinearLayout friendToggle = findViewById(R.id.friend_toggle);
        friendToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recyclerView = findViewById(R.id.friend_list_recycler_view);
                if (isFriendsToggledOff) {
                    recyclerView.setAdapter(mAdapter);
                } else {
                    ArrayList<FriendListItem> emptyDataset = new ArrayList<>();
                    final RecyclerViewAdapter adapter
                            = new RecyclerViewAdapter(emptyDataset);
                    recyclerView.setAdapter(adapter);
                }
                isFriendsToggledOff = !isFriendsToggledOff;
            }
        });
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
            final String uID = data.getStringExtra("uID");
            addUserWithID(uID);

        }
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

                        FriendListItem newFriend = new FriendListItem(name, status, uID ,imageURL);
                        myDataset.add(newFriend);
                        mAdapter.notifyDataSetChanged();

                        // get the current user's uID
                        String currentUserID;
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FriendListActivity.this);
                        if (acct != null) {
                            currentUserID = acct.getId();
                        } else {
                            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        }
                        FirebaseFirestore.getInstance().collection("Users")
                                .document(currentUserID)
                                .update("contacts", FieldValue.arrayUnion(uID));
                    }
                });
    }

    public void getContacts() {
        String id;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FriendListActivity.this);
        if (acct != null) {
            id = acct.getId();
        } else {
            id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

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

    public void getGroups() {
        // get this user's id
        final String id;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FriendListActivity.this);
        if (acct != null) {
            id = acct.getId();
        } else {
            id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        RecyclerView recyclerView = findViewById(R.id.friend_list_group_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        ArrayList<NewFriendItem> myDataset = new ArrayList<>();
        final GroupListRecyclerViewAdapter adapter
                = new GroupListRecyclerViewAdapter(myDataset, FriendListActivity.this, this);
        mGroupAdapter = adapter;
        recyclerView.setAdapter(adapter);


        FirebaseFirestore.getInstance().collection("Users").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
                        if (arr != null && !arr.isEmpty()) {
                            for (String s: arr) {
                                Log.d("HEY", "adding group " + s);
                                FirebaseFirestore.getInstance().collection("Groups").document(id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot doc = task.getResult();
                                                String name = (String) doc.get("groupName");
                                                String imageURL = (String) doc.get("imageUrl");

                                                NewFriendItem newFriendItem = new NewFriendItem(name, id, imageURL);
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
