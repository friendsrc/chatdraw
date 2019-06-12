package com.example.chatdraw;

public class FriendListItem {
    private String name;
    private String chatPreview;
    private int imageID;

    public FriendListItem(String name, String chatPreview, int imageID) {
        this.name = name;
        this.chatPreview = chatPreview;
        this.imageID = imageID;
    }

    public String getName() {
        return this.name;
    }

    public String getChatPreview() {
        return this.chatPreview;
    }

    public int getImageID() {
        return this.imageID;
    }
}
