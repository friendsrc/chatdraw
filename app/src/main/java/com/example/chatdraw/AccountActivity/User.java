package com.example.chatdraw.AccountActivity;

import androidx.annotation.NonNull;

public class User {
    public String email, name, username, imageUrl, description;
    public Integer credits;

    public User(){

    }

    public User(String email, String name, String username, String description) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.description = description;
    }

    public User(String email, String name, String username, Integer credits, String description) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.credits = credits;
        this.description = description;
    }

    public User(String email, String name, String username, String imageUrl, String description) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.imageUrl = imageUrl;
        this.description = description;
    }


    public Integer getCredits() { return credits; }

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

    public String getDescription() { return description; }

    public void setCredits(Integer newCredit) { this.credits = newCredit; }

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

    public void setDescription(String description) { this.description = description; }

    @NonNull
    @Override
    public String toString() {
        return "User " + this.name + " with username " + this.username + " and imageUrl " + this.imageUrl + " with description " + this.description;
    }
}