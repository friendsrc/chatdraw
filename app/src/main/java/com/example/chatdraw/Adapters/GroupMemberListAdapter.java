package com.example.chatdraw.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatdraw.Config.GlobalStorage;
import com.example.chatdraw.Items.GroupMemberListItem;
import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberListAdapter extends BaseAdapter {
    private List<GroupMemberListItem> items;
    private Context context;

    public GroupMemberListAdapter(Context context) {
        super();
        this.context = context;
        items = new ArrayList<>();
    }

    public void clearData() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addAdapterItem(GroupMemberListItem item) {
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
        // inflate the group_member_list_item layout
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.group_member_list_item, parent, false);

        // set the name
        TextView nameTextView = view.findViewById(R.id.member_list_name);
        String name = items.get(position).getName();
        if (name == null) {
            name = "Anonymous";
            nameTextView.setTextColor(context.getResources().getColor(R.color.pLight));
        }
        nameTextView.setText(name);

        // set the user description
        TextView chatTextView = view.findViewById(R.id.member_description);
        String description = items.get(position).getDescription();
        if (description == null) {
            description = GlobalStorage.welcomeDescription;
        }

        chatTextView.setText(description);

        // set the profile picture
        String imgUrl = items.get(position).getImageURL();
        ImageView imageView = view.findViewById(R.id.member_list_picture);

        if (imgUrl != null) {
            Picasso.get()
                    .load(imgUrl)
                    .fit()
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.blank_account);
        }

        return view;
    }
}
