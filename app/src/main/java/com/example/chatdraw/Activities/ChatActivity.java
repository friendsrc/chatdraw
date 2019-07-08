package com.example.chatdraw.Activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
    private String groupName;
    private String groupImageUrl;
    private ArrayList<String> membersID;
    private ArrayList<DocumentReference> membersPreview;

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

        // get friend's UID
        Intent intent = getIntent();
        friendsUID = intent.getStringExtra("uID");

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


        // set the action bar title
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //  set onClickListener on the 'Send Message' button
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get  the inputted  message
                EditText editText = findViewById(R.id.chat_edittext);
                String message = editText.getText().toString();
                // create a new ChatItem
                ChatItem newChatItem = addMessageToAdapter(message);
                sendMessage(newChatItem); // send the ChatItem to Firebase
                editText.setText(""); // erase the content of the EditText
            }
        });

        if (friendsUID.startsWith("GROUP_")) {
            Log.d("HEY", "is a group");
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
                            groupName = snapshot.getString("groupName");
                            groupImageUrl = snapshot.getString("groupImageUrl");
                            getMessages();
                        }
                    });
        } else {
            Log.d("HEY", "is not a group");
            isGroup = false;
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
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    // send the ChatItem to Firebase
    private void sendMessage(ChatItem chatItem) {
        Log.d(TAG, "sending Message");
        if (!chatItem.getMessageBody().equals("")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (!isGroup) {
                // Send to this user's message collection
                db.collection("Messages")
                        .document(userUID)
                        .collection("Friends")
                        .document(friendsUID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Send to the receiver's message collection
                db.collection("Messages")
                        .document(friendsUID)
                        .collection("Friends")
                        .document(userUID)
                        .collection("ChatHistory")
                        .add(chatItem);

                if (chatItem.getMessageBody().length() > 43) {
                    chatItem.setMessageBody(chatItem.getMessageBody().substring(0, 40) + "...");
                }

                // Send to user's message preview collection
                db.collection("Previews").document(userUID)
                        .collection("ChatPreviews").document(friendsUID)
                        .set(chatItem);

                // Send to the receiver's message preview collection
                db.collection("Previews").document(friendsUID)
                        .collection("ChatPreviews").document(userUID)
                        .set(chatItem);

            } else {
                // Send to group's message collection
                chatItem.setReceiverName(groupName);
                db.collection("GroupMessages")
                        .document(groupID)
                        .collection("ChatHistory")
                        .add(chatItem);

                // Send to group's preview collection
                if (chatItem.getMessageBody().length() > 43) {
                    chatItem.setMessageBody(chatItem.getMessageBody().substring(0, 40) + "...");
                    chatItem.setSenderID(groupID);
                    chatItem.setSenderName(groupName);
                    chatItem.setSenderImageUrl(groupImageUrl);
                    chatItem.setReceiverImageUrl(groupImageUrl);
                }
                if (membersPreview == null) {
                    membersPreview = new ArrayList<>();
                    CollectionReference previews = db.collection("Previews");
                    for (String s: membersID) {
                        membersPreview.add(previews.document(s).collection("ChatPreviews").document(groupID));
                    }
                }
                for (DocumentReference d: membersPreview) {
                    d.set(chatItem);
                }
            }

        }
    }

    public void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isGroup) {
            db.collection("Messages")
                    .document(userUID)
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

    public ChatItem addMessageToAdapter(String messageBody) {
        ChatItem chatItem;
        if (isGroup) {
            chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0],
                    userImageUrl[0], groupID, groupName, null, groupImageUrl);
        } else {
            chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0],
                    userImageUrl[0], friendsUID, friendName[0], friendUsername[0], friendImageUrl[0]);
        }

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
