package com.example.chatdraw.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.chatdraw.R;
import com.example.chatdraw.activities.ChatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatService extends Service {

  private static final String TAG = "ChatService";
  private static final String CHANNEL_ID = "2222";

  public ChatService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("HEY", "service created");
    createNotificationChannel();

    String userUid;
    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ChatService.this);
    if (acct != null) {
      userUid = acct.getId();
    } else {
      userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    final String id = userUid;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("Users").document(id)
        .get()
        .addOnSuccessListener(documentSnapshot -> db.collection("Previews")
            .document(id)
            .collection("ChatPreviews")
            .addSnapshotListener((snapshots, e) -> {
              if (e != null) {
                Log.w(TAG, "listen:error", e);
                return;
              }

              for (DocumentChange dc : snapshots.getDocumentChanges()) {
                String senderID = dc.getDocument().getString("senderID");
                if (!senderID.equals(id)) {
                  String messageBody = (String) dc.getDocument().get("messageBody");
                  String senderName = (String) dc.getDocument().get("senderName");
                  if (dc.getDocument().getString("receiverID").startsWith("GROUP_")) {
                    senderName = dc.getDocument().getString("receiverName");
                    senderID = dc.getDocument().getString("receiverID");
                  }
                  int messageId = senderID.hashCode();
                  switch (dc.getType()) {
                    case ADDED:
                      break;
                    case MODIFIED:
                      createNotification(messageId, messageBody, senderName, senderID);
                      Log.d(TAG, "Modified message: " + dc.getDocument().getData());
                      break;
                    case REMOVED:
                      Log.d(TAG, "Removed message: " + dc.getDocument().getData());
                      break;
                    default:
                      break;
                  }
                }
              }

            }));
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    return null;
  }

  /**
   * Create notification.
   */
  public void createNotification(int messageId, String messageBody, String senderName,
                                 String senderID) {
    if (senderName == null) {
      senderName = "Anonymous";
    }

    // Create an Intent for the activity you want to start
    Intent resultIntent = new Intent(this, ChatActivity.class);
    resultIntent.putExtra("uID", senderID);
    resultIntent.putExtra("name", senderName);
    resultIntent.putExtra("isFromService", true);
    // Create the TaskStackBuilder and add the intent, which inflates the back stack
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    stackBuilder.addNextIntentWithParentStack(resultIntent);
    // Get the PendingIntent containing the entire back stack
    PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.brush)
        .setContentTitle(senderName)
        .setContentText(messageBody)
        .setContentIntent(resultPendingIntent)
        .setAutoCancel(true)
        .setStyle(new NotificationCompat.BigTextStyle()
            .bigText(messageBody))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(messageId, builder.build());
  }

  private void createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "ChatDraw";
      String description = "Incoming message notification";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);

      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }
}
