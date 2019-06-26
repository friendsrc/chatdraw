package com.example.chatdraw.Chat;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatdraw.MainChat.MainActivity;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "ChatActivity";
    private String friendsUID;
    final String[] friendName = new String[1];
    final String[] friendUsername = new String[1];
    final String[] friendImageUrl = new String[1];

    private String userUID;
    final String[] userName = new String[1];
    final String[] userUsername = new String[1];
    final String[] userImageUrl = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        // get friend's UID
        friendsUID = intent.getStringExtra("uID");

        // get user's UID
        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                        Log.d("HEY", "username = " + friendUsername[0]);
                        Log.d("HEY","name = " +  friendName[0]);
                        Log.d("HEY", "image = " + friendImageUrl[0]);
                    }
                });

        // set the action bar title
        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set adapter to listview
        final ChatAdapter chatAdapter = new ChatAdapter(this);
        ListView listView = findViewById(R.id.chat_listview);
        listView.setAdapter(chatAdapter);

        getMessages(chatAdapter);

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
                ChatItem newChatItem = updateListView(chatAdapter, message);

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
        }
    }

    public void getMessages(final ChatAdapter chatAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Messages")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Friends")
                .document(friendsUID)
                .collection("ChatHistory")
                .orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        chatAdapter.clearData();
                        for (DocumentSnapshot q: queryDocumentSnapshots) {
                            ChatItem chatItem = q.toObject(ChatItem.class);
                            // TODO: make this work
//                            if (chatItem != null && !chatItem.getSenderID().equals(userUID)) {
//                                String updatedImageURL = friendImageUrl[0];
//                                chatItem.setSenderImageUrl(updatedImageURL);
//                            }
//                            chatAdapter.addAdapterItem(chatItem);
//                            chatAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, destroy this activity
        finish();
        return true;
    }

    public ChatItem updateListView(ChatAdapter chatAdapter, String messageBody) {
        // create a new ChatItem
        ChatItem chatItem = new ChatItem(messageBody, userUID, userName[0], userUsername[0], userImageUrl[0], friendsUID, friendName[0], friendUsername[0], friendImageUrl[0]);

        // add the new ChatItem to the ChatAdapter
        chatAdapter.addAdapterItem(chatItem);
        chatAdapter.notifyDataSetChanged();
        return chatItem;
    }
}
