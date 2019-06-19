package com.example.chatdraw.Chat;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private List<ChatItem> items;
    private Context context;
    private DatabaseReference databaseReference;

    public ChatAdapter(Context context) {
        super();
        this.context = context;
        this.items = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addAdapterItem(ChatItem item) {
        items.add(item);
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the friend_list_item layout
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        ChatItem chatItem = items.get(position);
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (chatItem.getUserID().equals(currentFirebaseUser.getUid())) {
            view = inflater.inflate(R.layout.right_chat_bubble, parent, false);
        } else {
            view = inflater.inflate(R.layout.left_chat_bubble, parent, false);
            TextView senderName = view.findViewById(R.id.text_message_name);
            //TODO get name from userID
            senderName.setText(chatItem.getUserID());
        }

        TextView messageBody  = view.findViewById(R.id.text_message_body);
        messageBody.setText(chatItem.getMessageBody());
        TextView time = view.findViewById(R.id.text_message_time);
        time.setText(chatItem.getTimeSent());

        return view;
    }
}
