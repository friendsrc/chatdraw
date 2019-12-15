package com.example.chatdraw.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.Items.NewFriendItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupListRecyclerViewAdapter
    extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private ArrayList<NewFriendItem> mDataset;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        // each data item is just a string in this case
        public View view;
        public MyViewHolder(View v) {
            super(v);
            view = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public GroupListRecyclerViewAdapter(ArrayList<NewFriendItem> myDataset) {
        mDataset = myDataset;
    }

    public GroupListRecyclerViewAdapter(ArrayList<NewFriendItem> myDataset,
                                        Context context, RecyclerViewClickListener listener) {
        mDataset = myDataset;
        this.context = context;
        itemListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View friendListItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.find_friend_item, parent, false);

        return new RecyclerViewAdapter.MyViewHolder(friendListItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, int position) {
        NewFriendItem newFriendItem = mDataset.get(position);

        TextView name = holder.view.findViewById(R.id.find_friend_edittext);
        String nameString = newFriendItem.getName();
        if (nameString == null) {
            nameString = "Anonymous";
            name.setTextColor(context.getResources().getColor(R.color.pLight));
        }
        name.setText(nameString);

        ImageView profilePicture = holder.view.findViewById(R.id.find_friend_imageview);
        String imageUrl = newFriendItem.getImageUrl();
        if (profilePicture != null && imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(profilePicture);
        }

    }

    // Return the size of dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void clearData() {
        mDataset = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addData(NewFriendItem newFriendItem) {
        mDataset.add(newFriendItem);
        notifyDataSetChanged();
    }

    public NewFriendItem getItem(int position) { return  mDataset.get(position); }
}
