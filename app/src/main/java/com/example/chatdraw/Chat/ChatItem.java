package com.example.chatdraw.Chat;

public class ChatItem {
    private String name;
    private String messageBody;
    private int imageID;
    private String timeSent;

    public ChatItem(String name, String messageBody, int imageID, String timeSent) {
        this.name = name;
        this.messageBody = messageBody;
        this.imageID = imageID;
        this.timeSent = timeSent;
    }

    public String getName() {
        return this.name;
    }

    public String getMessageBody() {
        return this.messageBody;
    }

    public int getImageID() {
        return this.imageID;
    }

    public String getTimeSent() { return  this.timeSent; }
}
