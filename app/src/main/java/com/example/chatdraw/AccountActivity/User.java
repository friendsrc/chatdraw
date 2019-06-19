package com.example.chatdraw.AccountActivity;

public class User {
    public String email, name, username;

    public User(){

    }

    public User(String name, String username) {
        this.name = name;
        this.username = username;
    }

    public User(String email, String name, String username) {
        this.email = email;
        this.name = name;
        this.username = username;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName() {
        this.name = name;
    }

    public void setUsername() {
        this.username = username;
    }
}