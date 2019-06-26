package com.example.chatdraw.Chat;

import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatItem {
    private String senderID;
    private String senderName;
    private String senderUsername;
    private String senderImageUrl;

    private String receiverID;
    private String receiverName;
    private String receiverUsername;
    private String receiverImageUrl;

    private String messageBody;
    private Date timestamp;
    private String timeSent;

    public ChatItem() {
    }

    public ChatItem(String messageBody, String senderID, String senderName, String senderUsername, String senderImageUrl, String receiverID, String receiverName, String receiverUsername, String receiverImageUrl) {

        this.senderID = senderID;
        this.senderName = senderName;
        this.senderUsername = senderUsername;
        this.senderImageUrl = senderImageUrl;
        this.receiverID = receiverID;
        this.receiverName = receiverName;
        this.receiverUsername = receiverUsername;
        this.receiverImageUrl = receiverImageUrl;
        this.messageBody = messageBody;

        // get the current time
        Calendar cal = Calendar.getInstance();
        this.timestamp=cal.getTime();

        // format the time into hour:minute
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String hour_and_minutes = dateFormat.format(timestamp);
        this.timeSent = hour_and_minutes;
    }

    public String getSenderID() {
        return this.senderID;
    }

    public String getReceiverID() { return this.receiverID; }

    public String getMessageBody() {
        return this.messageBody;
    }

    public Date getTimestamp() { return this.timestamp; }

    public String getTimeSent() { return  this.timeSent; }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImageUrl() {
        return senderImageUrl;
    }

    public void setSenderImageUrl(String senderImageUrl) {
        this.senderImageUrl = senderImageUrl;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getReceiverImageUrl() {
        return receiverImageUrl;
    }

    public void setReceiverImageUrl(String receiverImageUrl) {
        this.receiverImageUrl = receiverImageUrl;
    }
}
