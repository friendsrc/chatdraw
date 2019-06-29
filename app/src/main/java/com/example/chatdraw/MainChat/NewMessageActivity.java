package com.example.chatdraw.MainChat;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.chatdraw.Contacts.FriendListAdapter;
import com.example.chatdraw.RecyclerView.FriendListItem;
import com.example.chatdraw.CreateGroup.NewGroupActivity;
import com.example.chatdraw.R;
import com.example.chatdraw.RecyclerView.RecyclerViewAdapter;
import com.example.chatdraw.RecyclerView.RecyclerViewClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity implements RecyclerViewClickListener {

    public static final String TAG = "NewMessageActivity";
    private RecyclerViewAdapter mAdapter;
    private ArrayList<FriendListItem> myDataset;

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

        LinearLayout linearLayout = findViewById(R.id.new_group_chat_linearlayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMessageActivity.this, NewGroupActivity.class);
                startActivity(intent);
            }
        });

        // set the action bar title
        getSupportActionBar().setTitle("New Message");

        // add a back button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                        if (status == null) status = "[status]";

                        FriendListItem friendListItem = new FriendListItem(name, status, uID, imageURL);

                        myDataset.add(friendListItem);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        Intent intent = new Intent();
        FriendListItem friendListItem = mAdapter.getItem(position);
        intent.putExtra("uID", friendListItem.getUID());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
