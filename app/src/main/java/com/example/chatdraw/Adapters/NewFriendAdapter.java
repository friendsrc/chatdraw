package com.example.chatdraw.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatdraw.R;
import com.example.chatdraw.Items.NewFriendItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewFriendAdapter extends BaseAdapter {
    private List<NewFriendItem> items;
    private Context context;


    public NewFriendAdapter(Context context) {
        super();
        this.context = context;
        items = new ArrayList<>();
    }

    public void addAdapterItem(NewFriendItem item) {
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
        View view = inflater.inflate(R.layout.find_friend_item, parent, false);

        // set the name
        TextView nameTextView = view.findViewById(R.id.find_friend_edittext);
        nameTextView.setText(items.get(position).getName());

        // set the profile picture
        ImageView imageView = view.findViewById(R.id.find_friend_imageview);
        String imageUrl  = items.get(position).getImageUrl();
        if (imageUrl == null) {
            imageView.setImageResource(R.drawable.blank_account);
        } else {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(imageView);
        }
        return view;
    }

    public void clearData() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }
}
