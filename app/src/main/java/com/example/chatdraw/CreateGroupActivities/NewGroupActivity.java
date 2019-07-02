package com.example.chatdraw.CreateGroupActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.chatdraw.Items.FriendListItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Adapters.RecyclerViewAdapter;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class NewGroupActivity extends AppCompatActivity implements RecyclerViewClickListener {

    public static final int GROUP_CREATE_REQUEST_CODE = 1001;

    public static final String TAG = "NewGroupActivity";
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
        getSupportActionBar().setTitle("New Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // create a hashmap to store chosen contacts
        chosenContacts = new HashMap<>();

        // get contacts from Firebase
        getContacts();

        // get layout
        final LinearLayout layout = findViewById(R.id.new_group_layout);

        ImageView imageView = findViewById(R.id.new_group_nextbutton_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: add checker and add people list
                Intent intent = new Intent(NewGroupActivity.this, GroupCreateActivity.class);
//                String memberList = "";
//                for (FriendListItem f : chosenContacts.values()) {
//                    memberList += f.getUID();
//                    memberList += "\t";
//                }
                String[] memberList = new String[chosenContacts.size()];
                int i = 0;
                for (FriendListItem f: chosenContacts.values()) {
                    memberList[i] = f.getUID();
                    i++;
                }
                intent.putExtra("memberList", memberList);
                startActivityForResult(intent, GROUP_CREATE_REQUEST_CODE);
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

                        FriendListItem friendListItem = new FriendListItem(name, status, uID, imageURL);

                        myDataset.add(friendListItem);
                        mAdapter.notifyDataSetChanged();
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

}
