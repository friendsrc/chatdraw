package com.example.chatdraw.Chat;

import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatItem {
    private String userID;
    private String messageBody;
    private int imageID;
    private Date timestamp;
    private String timeSent;

    public ChatItem(String messageBody) {
        // get current user's uID
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String id = currentFirebaseUser.getUid();
        this.userID = id;

        // set the message body
        this.messageBody = messageBody;

        // TODO: get the profile picture from database
        this.imageID = R.drawable.blank_account;

        // get the current time
        Calendar cal = Calendar.getInstance();
        this.timestamp=cal.getTime();

        // format the time into hour:minute
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String hour_and_minutes = dateFormat.format(timestamp);
        this.timeSent = hour_and_minutes;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getMessageBody() {
        return this.messageBody;
    }

    public int getImageID() {
        return this.imageID;
    }

    public Date getTimestamp() { return this.timestamp; }

    public String getTimeSent() { return  this.timeSent; }
}
