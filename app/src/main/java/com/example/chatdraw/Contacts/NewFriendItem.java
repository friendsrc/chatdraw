package com.example.chatdraw.Contacts;

public class NewFriendItem {
    private String name;
    private int imageID;

    public NewFriendItem(String name, int imageID) {
        this.name = name;
        this.imageID = imageID;
    }

    public String getName() {
        return this.name;
    }

    public int getImageID() {
        return this.imageID;
    }
}
