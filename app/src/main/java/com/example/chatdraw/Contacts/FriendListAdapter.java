package com.example.chatdraw.Contacts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends BaseAdapter {
    private List<FriendListItem> items;
    private Context context;


    public FriendListAdapter(Context context) {
        super();
        this.context = context;
        items = new ArrayList<>();
    }

    public void clearData() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addAdapterItem(FriendListItem item) {
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
        View view = inflater.inflate(R.layout.friend_list_item, parent, false);

        // set the name
        TextView nameTextView = view.findViewById(R.id.friend_list_name);
        nameTextView.setText(items.get(position).getName());

        // set the chatpreview
        TextView chatTextView = view.findViewById(R.id.friend_list_chat_preview);
        chatTextView.setText(items.get(position).getChatPreview());

        // set the profile picture
        String imgUrl = items.get(position).getImageURL();
        ImageView imageView = view.findViewById(R.id.friend_list_profilepicture);

        if (imgUrl != null) {
            Picasso.get()
                    .load(imgUrl)
                    .fit()
                    .into(imageView);
            Log.d("HEY", "calling picasso");
        } else {
            imageView.setImageResource(items.get(position).getImageID());
        }

        return view;
    }
}
