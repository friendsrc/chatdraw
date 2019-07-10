package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private static final int NEW_GROUP_REQUEST_CODE = 505;


    public static final String TAG = "NewMessageActivity";
    private RecyclerViewAdapter mAdapter;
    private ArrayList<FriendListItem> myDataset;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        RecyclerView recyclerView = findViewById(R.id.new_message_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        myDataset = new ArrayList<>();
        mAdapter = new RecyclerViewAdapter(myDataset, NewMessageActivity.this, this);
        recyclerView.setAdapter(mAdapter);


        // get Contacts list
        String id;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(NewMessageActivity.this);
        if (acct != null) {
            id = acct.getId();
        } else {
            id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        userUID = id;


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

        LinearLayout linearLayout = findViewById(R.id.new_group_chat_linearlayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMessageActivity.this, NewGroupActivity.class);
                startActivityForResult(intent, NEW_GROUP_REQUEST_CODE);
            }
        });

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("New Message");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // create an action bar button
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

                        // check if the user doesn't have name/status
                        if (status == null) status = "";

                        FriendListItem friendListItem = new FriendListItem(name, status, uID, imageURL);

                        myDataset.add(friendListItem);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        final FriendListItem friendListItem = mAdapter.getItem(position);

        FirebaseFirestore.getInstance().collection("Previews").document(userUID)
                .collection("ChatPreviews")
                .document(friendListItem.getUID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Toast.makeText(NewMessageActivity.this,
                                        "Chat already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("uID", friendListItem.getUID());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "Failed with: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_GROUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(55, data);
            finish();
        }
    }
}
