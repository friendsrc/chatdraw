package com.example.chatdraw.Items;

import java.io.Serializable;

public class FriendListItem implements Serializable {

    private String uID;
    private String name;
    private String imageURL;
    private String chatPreview;

    public FriendListItem(String name, String chatPreview, String imageURL) {
        this.name = name;
        this.chatPreview = chatPreview;
        this.imageURL = imageURL;
    }

    public FriendListItem(String name, String chatPreview, String uID, String imageURL) {
        this.name = name;
        this.chatPreview = chatPreview;
        this.uID = uID;
        this.imageURL = imageURL;
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

    public String getImageURL() { return this.imageURL; }
}
