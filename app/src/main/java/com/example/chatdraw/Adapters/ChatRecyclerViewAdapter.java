package com.example.chatdraw.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.AccountActivity.LoginActivity;
import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private ArrayList<ChatItem> mDataset;
    private Context context;
    private static RecyclerViewClickListener itemListener;
    private String userId;

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
    public ChatRecyclerViewAdapter(ArrayList<ChatItem> myDataset) {
        mDataset = myDataset;
    }

    public ChatRecyclerViewAdapter(ArrayList<ChatItem> myDataset, Context context, RecyclerViewClickListener listener) {
        mDataset = myDataset;
        this.context = context;
        itemListener = listener;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        if (acct != null) {
            this.userId = acct.getId();
        } else {
            this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.get(position).getSenderID().equals(userId)) {
            return 0;
        } else {
            return 1;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View friendListItem;
        if (viewType == 0) {
            friendListItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_chat_bubble, parent, false);
        } else {
            friendListItem = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_chat_bubble, parent, false);
        }

        RecyclerViewAdapter.MyViewHolder vh = new RecyclerViewAdapter.MyViewHolder(friendListItem);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ChatItem chatItem = mDataset.get(position);

        TextView name = holder.view.findViewById(R.id.text_message_name);
        if (name != null) {
            String nameString = mDataset.get(position).getSenderName();
            Log.d("HEY", "namestring = " + nameString);
            if (nameString == null) {
                nameString = "Anonymous";
                name.setTextColor(context.getResources().getColor(R.color.pLight));
            }
            name.setText(nameString);
        }

        TextView message = holder.view.findViewById(R.id.text_message_body);
        message.setText(mDataset.get(position).getMessageBody());

        ImageView profilePicture = holder.view.findViewById(R.id.image_message_profile);
        if (profilePicture != null) {
            String imageUrl = mDataset.get(position).getSenderImageUrl();
            if (profilePicture != null && imageUrl != null) {
                Picasso.get()
                        .load(imageUrl)
                        .fit()
                        .into(profilePicture);
            }
        }

        TextView time = holder.view.findViewById(R.id.text_message_time);
        time.setText(mDataset.get(position).getTimeSent());


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void clearData() {
        mDataset = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addData(ChatItem chatItem) {
        mDataset.add(chatItem);
        notifyDataSetChanged();
    }

    public ChatItem getItem(int position) { return  mDataset.get(position); };
}
