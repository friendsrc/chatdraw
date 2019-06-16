package com.example.chatdraw.Chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.chatdraw.Contacts.FriendListAdapter;
import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        // set the action bar title
        getSupportActionBar().setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final ChatAdapter chatAdapter = new ChatAdapter(this);
        mChatAdapter = chatAdapter;
        updateListView(mChatAdapter, "John Doe",
                "Try typing a message!", R.drawable.blank_account);

        final EditText editText = findViewById(R.id.chat_edittext);
        ImageView sendImageView = findViewById(R.id.chat_send_imageview);
        sendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                updateListView(chatAdapter, "thisUser", message, R.drawable.blank_account);
                editText.setText("");
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    public void updateListView(ChatAdapter chatAdapter, String name, String messageBody, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.chat_listview);

        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String formattedDate=dateFormat.format(date);

        ChatItem newChatItem = new ChatItem(name, messageBody, imageID, formattedDate);
        mChatAdapter.addAdapterItem(newChatItem);

        // set the adapter to the ListView
        listView.setAdapter(mChatAdapter);
    }
}
