package com.example.chatdraw.ChatActivites;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Adapters.ChatRecyclerViewAdapter;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity implements RecyclerViewClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static String TAG = "ChatActivity";
    private String friendsUID;
    final String[] friendName = new String[1];
    final String[] friendUsername = new String[1];
    final String[] friendImageUrl = new String[1];

    private String userUID;
    final String[] userName = new String[1];
    final String[] userUsername = new String[1];
    final String[] userImageUrl = new String[1];

    private RecyclerView mRecyclerView;
    private ChatRecyclerViewAdapter mAdapter;
    private ArrayList<ChatItem> myDataset;

    SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean isGroup = false;
    private String groupID;
    private ArrayList<String> membersID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRecyclerView = findViewById(R.id.chat_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        myDataset = new ArrayList<>();
        mAdapter = new ChatRecyclerViewAdapter(myDataset, ChatActivity.this, this);
        mRecyclerView.setAdapter(mAdapter);

        // set 'pull-to-fetch-older-messages'
        mSwipeRefreshLayout = findViewById(R.id.chat_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        Intent intent = getIntent();

        // get friend's UID
        friendsUID = intent.getStringExtra("uID");

        if (friendsUID.startsWith("GROUP_")) {
            isGroup = true;
            groupID = friendsUID;
            FirebaseFirestore.getInstance().collection("Groups")
                    .document(groupID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            membersID = (ArrayList<String>) snapshot.get("members");
                        }
                    });
        }

        // get user's UID
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChatActivity.this);
        if (acct != null) {
            this.userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // get user's display name and profile picture
        FirebaseFirestore.getInstance().collection("Users")
                .document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        userName[0] = task.getResult().getString("name");
                        userUsername[0] = task.getResult().getString("username");
                        userImageUrl[0] = task.getResult().getString("imageUrl");
                    }
                });


        // get friends's display name and profile picture
        FirebaseFirestore.getInstance().collection("Users")
                .document(friendsUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        friendName[0] = task.getResult().getString("name");
                        friendUsername[0] = task.getResult().getString("username");
                        friendImageUrl[0] = task.getResult().getString("imageUrl");
                        getMessages();
                    }
                });

        // set the action bar title
        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //  set onClickListener on the 'Send Message' button
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // find the EditText for message input
                EditText editText = findViewById(R.id.chat_edittext);

                // get  the inputted  message
                String message = editText.getText().toString();

                // create a new ChatItem
                ChatItem newChatItem = updateListView(message);

                sendMessage(newChatItem); // send the ChatItem to Firebase
                editText.setText(""); // erase the content of the EditText
            }
        });
    }

    // send the ChatItem to Firebase
    private void sendMessage(ChatItem chatItem) {
        Log.d(TAG, "sending Message");
        if (!chatItem.getMessageBody().equals("")) {

//            // Send to Realtime Database
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages");
//            databaseReference.push().setValue(chatItem);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userID = FirebaseAuth.getInstance().getUid();

            if (!isGroup) {
                // Send to this user's message collection
                db.collection("Messages")
                        .document(userID)
                        .collection("Friends")
                        .document(friendsUID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Send to the receiver's message collection
                db.collection("Messages")
                        .document(friendsUID)
                        .collection("Friends")
                        .document(userID)
                        .collection("ChatHistory")
                        .add(chatItem);

                if (chatItem.getMessageBody().length() > 43) {
                    chatItem.setMessageBody(chatItem.getMessageBody().substring(0, 40) + "...");
                }

                // Send to user's message preview collection
                db.collection("Previews").document(userID)
                        .collection("ChatPreviews").document(friendsUID)
                        .set(chatItem);

                // Send to the receiver's message preview collection
                db.collection("Previews").document(friendsUID)
                        .collection("ChatPreviews").document(userID)
                        .set(chatItem);
            } else {
                // Send to group's message collection
                db.collection("GroupMessages")
                        .document(groupID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Send to group's preview collection
                if (chatItem.getMessageBody().length() > 43) {
                    chatItem.setMessageBody(chatItem.getMessageBody().substring(0, 40) + "...");
                }
                db.collection("GroupPreviews")
                        .document(groupID)
                        .set(chatItem);
            }


        }
    }

    public void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isGroup) {
            db.collection("Messages")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("Friends")
                    .document(friendsUID)
                    .collection("ChatHistory")
                    .orderBy("timestamp")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            mAdapter.clearData();
                            for (DocumentSnapshot q: queryDocumentSnapshots) {
                                ChatItem chatItem = q.toObject(ChatItem.class);

                                if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                    String updatedImageURL = friendImageUrl[0];
                                    chatItem.setSenderImageUrl(updatedImageURL);
                                }
                                mAdapter.addData(chatItem);
                                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
        } else {
            db.collection("GroupMessages")
                    .document(groupID)
                    .collection("ChatHistory")
                    .orderBy("timestamp")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            mAdapter.clearData();
                            for (DocumentSnapshot q: queryDocumentSnapshots) {
                                ChatItem chatItem = q.toObject(ChatItem.class);

                                if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
                                    String updatedImageURL = friendImageUrl[0];
                                    chatItem.setSenderImageUrl(updatedImageURL);
                                }
                                mAdapter.addData(chatItem);
                                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, destroy this activity
        finish();
        return true;
    }

    public ChatItem updateListView(String messageBody) {
        // create a new ChatItem
        ChatItem chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0], userImageUrl[0], friendsUID, friendName[0], friendUsername[0], friendImageUrl[0]);

        // add the new ChatItem to the ChatAdapter
        mAdapter.addData(chatItem);
        return chatItem;
    }

    @Override
    public void onRefresh() {
        //TODO: paginate data
//        Toast.makeText(this, "Getting older messages", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Not yet configured", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
        // TODO: add delete/copy option
    }
}
