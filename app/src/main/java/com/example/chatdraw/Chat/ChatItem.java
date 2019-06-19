package com.example.chatdraw.Chat;

public class ChatItem {
    private String userID;
    private String messageBody;
    private int imageID;
    private String timeSent;

    public ChatItem(String userID, String messageBody, int imageID, String timeSent) {
        this.userID = userID;
        this.messageBody = messageBody;
        this.imageID = imageID;
        this.timeSent = timeSent;
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

    public String getTimeSent() { return  this.timeSent; }
}
