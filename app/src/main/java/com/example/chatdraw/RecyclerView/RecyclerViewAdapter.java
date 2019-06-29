package com.example.chatdraw.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private ArrayList<FriendListItem> mDataset;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
    public RecyclerViewAdapter(ArrayList<FriendListItem> myDataset) {
        mDataset = myDataset;
    }

    public RecyclerViewAdapter(ArrayList<FriendListItem> myDataset, Context context, RecyclerViewClickListener listener) {
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
                .inflate(R.layout.friend_list_item, parent, false);
        MyViewHolder vh = new MyViewHolder(friendListItem);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        TextView name = holder.view.findViewById(R.id.friend_list_name);
        name.setText(mDataset.get(position).getName());

        TextView status = holder.view.findViewById(R.id.friend_list_chat_preview);
        status.setText(mDataset.get(position).getChatPreview());

        ImageView profilePicture = holder.view.findViewById(R.id.friend_list_profilepicture);
        String imageUrl = mDataset.get(position).getImageURL();

        if (imageUrl != null) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(profilePicture);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public FriendListItem getItem(int position) { return  mDataset.get(position); };
}
