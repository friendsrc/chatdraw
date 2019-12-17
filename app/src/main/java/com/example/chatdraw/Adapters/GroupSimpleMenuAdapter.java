package com.example.chatdraw.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatdraw.Items.SimpleMenuItem;
import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupSimpleMenuAdapter extends BaseAdapter {
    private List<SimpleMenuItem> items;
    private Context context;

    public GroupSimpleMenuAdapter(Context context) {
        super();
        this.context = context;
        items = new ArrayList<>();
    }

    public void clearData() {
        items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addAdapterItem(SimpleMenuItem item) {
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
        View view = inflater.inflate(R.layout.simple_menu_item, parent, false);

        // set the name
        TextView nameTextView = view.findViewById(R.id.menu_name);
        String name = items.get(position).getName();
        if (name == null) {
            name = "An error occurred";
            nameTextView.setTextColor(context.getResources().getColor(R.color.pLight));
        }
        nameTextView.setText(name);

        // set the profile picture
        int imgUrl = items.get(position).getImageDrawable();
        ImageView imageView = view.findViewById(R.id.menu_picture);
        imageView.setImageResource(imgUrl);

        return view;
    }
}
