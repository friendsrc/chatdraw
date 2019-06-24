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

    public ChatAdapter(Context context) {
        super();
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void clearData() {
        items = new ArrayList<>();
        notifyDataSetChanged();
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

        // get the ChatItem at the specified position
        ChatItem chatItem = items.get(position);

        // get the current user
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // if the message is send by the current user, create a right chat bubble
        // else, create a left chat bubble
        if (chatItem.getSenderID().equals(currentFirebaseUser.getUid())) {
            view = inflater.inflate(R.layout.right_chat_bubble, parent, false);
        } else {
            view = inflater.inflate(R.layout.left_chat_bubble, parent, false);
            TextView senderName = view.findViewById(R.id.text_message_name);
            //TODO get name from userID
            senderName.setText(chatItem.getSenderID());
        }

        // set the text of the chat bubble
        TextView messageBody  = view.findViewById(R.id.text_message_body);
        messageBody.setText(chatItem.getMessageBody());

        // set time 'time sent'
        TextView time = view.findViewById(R.id.text_message_time);
        time.setText(chatItem.getTimeSent());

        return view;
    }
}
