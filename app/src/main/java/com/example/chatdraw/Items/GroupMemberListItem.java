package com.example.chatdraw.Items;

import java.io.Serializable;

public class GroupMemberListItem implements Serializable {
    private String uID;
    private String name;
    private String imageURL;
    private String description;

    public GroupMemberListItem(String name, String description, String imageURL) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
    }

    public GroupMemberListItem(String name, String description, String uID, String imageURL) {
        this.name = name;
        this.description = description;
        this.uID = uID;
        this.imageURL = imageURL;
    }

    public String getUID() {
        return this.uID;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageURL() { return this.imageURL; }
}
