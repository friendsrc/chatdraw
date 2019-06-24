package com.example.chatdraw.Contacts;

public class FriendListItem {
    private String uID;
    private String username;
    private String name;
    private String chatPreview;
    private int imageID;

    public FriendListItem(String name, String chatPreview, int imageID) {
        this.name = name;
        this.chatPreview = chatPreview;
        this.imageID = imageID;
    }

    public FriendListItem(String name, String chatPreview, int imageID, String uID) {
        this.name = name;
        this.chatPreview = chatPreview;
        this.imageID = imageID;
        this.uID = uID;
    }

    public FriendListItem(String username, String name, String chatPreview, int imageID) {
        this.username = username;
        this.name = name;
        this.chatPreview = chatPreview;
        this.imageID = imageID;
    }

    public String getUID() {
        return this.uID;
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

    public String getUsername() { return this.username; }
}
