package com.example.chatdraw.items;

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
        this.timestamp= Calendar.getInstance().getTime();

        // format the time into hour:minute
        this.timeSent = new SimpleDateFormat("HH:mm").format(timestamp);
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
