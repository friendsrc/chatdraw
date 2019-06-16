package com.example.chatdraw.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatdraw.Contacts.FriendListItem;
import com.example.chatdraw.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private List<ChatItem> items;
    private Context context;


    public ChatAdapter(Context context) {
        super();
        this.context = context;
        items = new ArrayList<>();
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

        if (chatItem.getName().equals("thisUser")) {
            view = inflater.inflate(R.layout.right_chat_bubble, parent, false);
        } else {
            view = inflater.inflate(R.layout.left_chat_bubble, parent, false);
            TextView senderName = view.findViewById(R.id.text_message_name);
            senderName.setText(chatItem.getName());
        }

        TextView messageBody  = view.findViewById(R.id.text_message_body);
        messageBody.setText(chatItem.getMessageBody());
        TextView time = view.findViewById(R.id.text_message_time);
        time.setText(chatItem.getTimeSent());

        return view;
    }
}
