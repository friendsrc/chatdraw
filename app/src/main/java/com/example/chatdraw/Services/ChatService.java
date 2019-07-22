package com.example.chatdraw.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class ChatService extends Service {

    private static final String TAG = "ChatService";
    private static final String CHANNEL_ID = "2222";

    public ChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service created");
        createNotificationChannel();

        String userUID;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChatService.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        final String id = userUID;
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> contacts = (ArrayList<String>) documentSnapshot.get("contacts");
                        ArrayList<String> groups = (ArrayList<String>) documentSnapshot.get("groups");
                        contacts.addAll(groups);

                        db.collection("Previews")
                                .document(id)
                                .collection("ChatPreviews")
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "listen:error", e);
                                            return;
                                        }

                                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                            switch (dc.getType()) {
                                                case ADDED:
                                                    String messageBody = (String) dc.getDocument().get("messageBody");
                                                    String senderName = (String) dc.getDocument().get("senderName");
                                                    int messageId = dc.getDocument().get("senderID").hashCode();
                                                    createNotification(messageId, messageBody, senderName);
                                                    Log.d(TAG, "New message: " + dc.getDocument().getData());
                                                    break;
                                                case MODIFIED:
                                                    String body = (String) dc.getDocument().get("messageBody");
                                                    String sender = (String) dc.getDocument().get("senderName");
                                                    int id = dc.getDocument().get("senderID").hashCode();
                                                    createNotification(id, body, sender);
                                                    Log.d(TAG, "Modified message: " + dc.getDocument().getData());
                                                    break;
                                                case REMOVED:
                                                    Log.d(TAG, "Removed message: " + dc.getDocument().getData());
                                                    break;
                                            }
                                        }

                                    }
                                });

                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind");



        return null;
    }

    public void createNotification(int messageId, String messageBody, String senderName) {
        if (senderName == null) senderName = "Anonymous";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.blank_account)
                .setContentTitle(senderName)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(messageId, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ChatDraw";
            String description = "Incoming Message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
