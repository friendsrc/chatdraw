package com.example.chatdraw.Items;

public class NewFriendItem {
    private String name;
    private String username;
    private String imageUrl;

    public NewFriendItem(String name, String username, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.username = username;
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() { return this.username; }

    public String getImageUrl() {
        return this.imageUrl;
    }

}
