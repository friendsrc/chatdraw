package com.example.chatdraw.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.Activities.ChatActivity;
import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.example.chatdraw.Listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.LinkedList;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.MyViewHolder> {
    private LinkedList<ChatItem> mDataset;
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
    public ChatRecyclerViewAdapter(LinkedList<ChatItem> myDataset) {
        mDataset = myDataset;
    }

    public ChatRecyclerViewAdapter(LinkedList<ChatItem> myDataset, Context context, RecyclerViewClickListener listener) {
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
        ChatItem chatItem = mDataset.get(position);
        String[] arr = chatItem.getMessageBody().split("\t");

        if (chatItem.getSenderID().equals(userId)) { // chat is sent by this user
            if (chatItem.getMessageBody().startsWith(userId)) {
                switch (arr[1]) {
                    case "IMAGE":
                        return 20; // chat item is of type image

                    case "PDF":
                        return 30; // chat item is of type pdf

                    case "INFO":
                        return 44;
                }
            }
            return 0; // chat item is of type text

        } else { // chat is not sent by this user
            if (chatItem.getMessageBody().startsWith(chatItem.getSenderID())) {
                switch (arr[1]) {
                    case "IMAGE":
                        return 21; // chat item is of type image

                    case "PDF":
                        return 31; // chat item is of type pdf

                    case "INFO":
                        return 44;
                }
            }
            return 1; // chat item is of type text
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
        // create a new view
        View friendListItem;
        switch (viewType) {
            case 0:  // text, from this user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.right_chat_bubble, parent, false);
                break;
            case 1:  // text, from another user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.left_chat_bubble, parent, false);
                break;
            case 20:  // image, from this user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.right_chat_bubble_photo, parent, false);
                break;
            case 21:  // image, from another user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.left_chat_bubble_photo, parent, false);
                break;
            case 30:  // pdf, from this user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.right_chat_bubble_pdf, parent, false);
                break;
            case 31:  // pdf, from another user
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.left_chat_bubble_pdf, parent, false);
                break;
            case 44: // chat action
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_action_item, parent, false);
                break;
            default:
                friendListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.right_chat_bubble, parent, false);
                break;
        }

        return new ChatRecyclerViewAdapter.MyViewHolder(friendListItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ChatRecyclerViewAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ChatItem chatItem = mDataset.get(position);

        TextView chatActionTextView = holder.view.findViewById(R.id.chat_action_textview);
        if (chatActionTextView != null) {
            chatActionTextView.setText(chatItem.getMessageBody().split("\t")[2]);
            return;
        }

        TextView name = holder.view.findViewById(R.id.text_message_name);
        if (name != null) {
            String nameString = mDataset.get(position).getSenderName();
            if (nameString == null) {
                nameString = "Anonymous";
                name.setTextColor(context.getResources().getColor(R.color.pLight));
            }
            name.setText(nameString);
        }

        String[] arr = chatItem.getMessageBody().split("\t");

        if (arr.length > 1) {
            if (arr[1].equals("IMAGE")) {
                ImageView message = holder.view.findViewById(R.id.text_message_body_image);
                message.setOnClickListener(v -> ((ChatActivity) context).goToImagePreview(chatItem));
            } else if (arr[1].equals("PDF")) {
                TextView message = holder.view.findViewById(R.id.text_message_body);
                message.setOnClickListener(v -> {
                    String url = chatItem.getMessageBody().split("\t")[3];
                    ((ChatActivity) context).viewPdf(url);
                });
            }
        }


        if (chatItem.getMessageBody().startsWith(chatItem.getSenderID())) {
            if (arr[1].equals("IMAGE")) {
                ImageView message = holder.view.findViewById(R.id.text_message_body_image);
                Picasso.get()
                        .load(arr[2])
                        .fit()
                        .into(message);
                message.setOnLongClickListener(v -> {
                    if (!chatItem.getSenderID().equals(userId))  return true;
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.nontextmessagepopup);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(true);



                    TextView delete = dialog.findViewById(R.id.delete_message_textview);
                    delete.setOnClickListener(v14 -> deleteMessage(chatItem.getSenderID(), chatItem.getReceiverID(),
                        chatItem.getTimestamp(), chatItem.getMessageBody(), dialog, position));

                    dialog.show();
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    return true;
                });
            } else if (arr[1].equals("PDF")) {
                if (!chatItem.getSenderID().equals(userId));
                TextView message = holder.view.findViewById(R.id.text_message_body);
                message.setText(arr[2]);
                message.setOnLongClickListener(v -> {
                    if (!chatItem.getSenderID().equals(userId))  return true;

                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.nontextmessagepopup);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setCancelable(true);

                    TextView delete = dialog.findViewById(R.id.delete_message_textview);
                    delete.setOnClickListener(v13 -> deleteMessage(chatItem.getSenderID(), chatItem.getReceiverID(),
                        chatItem.getTimestamp(), chatItem.getMessageBody(), dialog, position));

                    dialog.show();
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    return true;
                });
            }
        } else {
            TextView message = holder.view.findViewById(R.id.text_message_body);
            message.setText(mDataset.get(position).getMessageBody());
            message.setOnLongClickListener(v -> {
                if (!chatItem.getSenderID().equals(userId))  return true;

                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.messageoptionpopup);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setCancelable(true);

                TextView copy = dialog.findViewById(R.id.copy_message_textview);
                copy.setOnClickListener(v12 -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", message.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    dialog.dismiss();
                    Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
                });

                TextView delete = dialog.findViewById(R.id.delete_message_textview);
                delete.setOnClickListener(v1 -> deleteMessage(chatItem.getSenderID(), chatItem.getReceiverID(),
                    chatItem.getTimestamp(), chatItem.getMessageBody(), dialog, position));

                dialog.show();
                dialog.getWindow().setGravity(Gravity.CENTER);
                return true;
            });
        }

        ImageView profilePicture = holder.view.findViewById(R.id.image_message_profile);
        if (profilePicture != null) {
            String imageUrl = mDataset.get(position).getSenderImageUrl();
            if (profilePicture != null && imageUrl != null) {
                Picasso.get()
                        .load(imageUrl)
                        .fit()
                        .into(profilePicture);
            }
            profilePicture.setOnClickListener(v -> Toast.makeText(context, " Profile photo clicked", Toast.LENGTH_SHORT).show());
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
        mDataset = new LinkedList<>();
        notifyDataSetChanged();
    }

    public void addData(ChatItem chatItem) {
        mDataset.addFirst(chatItem);
        notifyDataSetChanged();
    }


    public ChatItem getItem(int position) { return  mDataset.get(position); }


    public void deleteMessage(String senderID, String receiverID, Date timestamp,
                              String messageBody, Dialog dialog, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // remove from firebase
        if (receiverID.startsWith("GROUP_")) {
            FirebaseFirestore.getInstance()
                    .collection("GroupMessages")
                    .document(receiverID)
                    .collection("ChatHistory")
                    .whereEqualTo("timestamp", timestamp)
                    .whereEqualTo("messageBody", messageBody)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (DocumentSnapshot d: snapshots.getDocuments()) {
                            d.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        } else {
            FirebaseFirestore.getInstance()
                    .collection("Messages")
                    .document(senderID)
                    .collection("Friends")
                    .document(receiverID)
                    .collection("ChatHistory")
                    .whereEqualTo("timestamp", timestamp)
                    .whereEqualTo("messageBody", messageBody)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        for (DocumentSnapshot d: snapshots.getDocuments()) {
                            d.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        }

        // check if firebase message preview should be updated
        if (position == mDataset.size() - 1) {
            // the deleted message is  the last sent message
            // update preview to second to last message

            ChatItem chatItem = mDataset.get(mDataset.size() - 2);
            if (chatItem.getMessageBody().startsWith(userId)) {
                String[] arr = chatItem.getMessageBody().split("\t");
                switch (arr[1]) {
                    case "IMAGE":
                        chatItem.setMessageBody("[Image]");
                        break;
                    case "PDF":
                        chatItem.setMessageBody("[Pdf]");
                        break;
                    case "INFO":
                        chatItem.setMessageBody(arr[2]);
                        break;
                    default:
                        chatItem.setMessageBody("[Unknown file type]");
                        break;
                }
            }
            if (mDataset.size() >= 2) {
                // Send to user's message preview collection
                db.collection("Previews").document(senderID)
                        .collection("ChatPreviews").document(receiverID)
                        .set(mDataset.get(mDataset.size() - 2));

                // Send to the receiver's message preview collection
                db.collection("Previews").document(receiverID)
                        .collection("ChatPreviews").document(senderID)
                        .set(mDataset.get(mDataset.size() - 2));
            } else {
                chatItem.setMessageBody("");
                // Send to user's message preview collection
                db.collection("Previews").document(senderID)
                        .collection("ChatPreviews").document(receiverID)
                        .set(chatItem);

                // Send to the receiver's message preview collection
                db.collection("Previews").document(receiverID)
                        .collection("ChatPreviews").document(senderID)
                        .set(chatItem);
            }
        }

        // remove from dataset
        mDataset.remove(position);
    }

}
