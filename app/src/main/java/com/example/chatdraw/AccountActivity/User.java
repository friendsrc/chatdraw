package com.example.chatdraw.AccountActivity;

import androidx.annotation.NonNull;

public class User {
    public String email, name, username, imageUrl;

    public User(){

    }

    public User(String email, String name, String username) {
        this.email = email;
        this.name = name;
        this.username = username;
    }

    public User(String email, String name, String username, String imageUrl) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() { return imageUrl; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @NonNull
    @Override
    public String toString() {
        return "User " + this.name + " with username " + this.username + " and imageUrl " + this.imageUrl;
    }
}