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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatActivity extends AppCompatActivity {

    private static String TAG = "Chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        // set the action bar title
        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set adapter to listview
        final ChatAdapter chatAdapter = new ChatAdapter(this);
        ListView listView = findViewById(R.id.chat_listview);
        listView.setAdapter(chatAdapter);

        // testing the listview
        updateListView(chatAdapter, "John Doe",
                "Try typing a message!", R.drawable.blank_account);

        //  set onClickListener on the 'Send Message' button
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // find the EditText for message input
                EditText editText = findViewById(R.id.chat_edittext);

                // get  the inputted  message
                String message = editText.getText().toString();

                // get the current user's Uid
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String name = currentFirebaseUser.getUid();

                // create a new ChatItem
                ChatItem newChatItem = updateListView(chatAdapter, name, message, R.drawable.blank_account);

                sendMessage(newChatItem); // send the ChatItem to Firebase
                editText.setText(""); // erase the content of the EditText
            }
        });

        // Retrieve the current Registration Token
//        getFirebaseToken();
    }

    // send the ChatItem to Firebase
    private void sendMessage(ChatItem chatItem) {
        Log.d(TAG, "sending Message");
        if (!chatItem.getMessageBody().equals("")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Messages");
            databaseReference.push().setValue(chatItem);
        }
    }

    public void sendUpstreamMessage() {
        Log.d(TAG, "sending  upstream message");
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        Long SENDER_ID = 437200162274L;
        AtomicInteger msgId = new AtomicInteger();
        fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(msgId.incrementAndGet()))
                .addData("my_message", "Hello World")
                .addData("my_action","SAY_HELLO")
                .build());
    }

//    public void getFirebaseToken() {
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//
//                        // Log and toast
//                        String msg = "Instance ID token = " + token;
//                        Log.d(TAG, msg);
//                        Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, destroy this activity
        finish();
        return true;
    }

    public ChatItem updateListView(ChatAdapter chatAdapter, String name, String messageBody, int imageID) {
        // get time in hour:minutes
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String formattedDate = dateFormat.format(date);

        // create a new ChatItem
        ChatItem chatItem = new ChatItem(name, messageBody, imageID, formattedDate);

        // add the new ChatItem to the ChatAdapter
        chatAdapter.addAdapterItem(chatItem);
        chatAdapter.notifyDataSetChanged();
        return chatItem;
    }
}
