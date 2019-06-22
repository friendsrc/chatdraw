package com.example.chatdraw.Contacts;

import com.example.chatdraw.R;

public class NewFriendItem {
    private String name;
    private String username;
    private int imageID;

    public NewFriendItem(String name, String username, int imageID) {
        //TODO change parameter to username only, get the other attributes from firestore
        this.name = name;
        this.imageID = imageID;
        this.username = username;
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() { return this.username; }

    public int getImageID() {
        return this.imageID;
    }

}
