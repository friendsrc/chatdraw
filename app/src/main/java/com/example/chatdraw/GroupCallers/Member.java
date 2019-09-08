package com.example.chatdraw.GroupCallers;

public class Member {
    public String name, imageUrl;

    public Member() {

    }

    public Member(String name, String imageUrl){
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getimageUrl() {
        return imageUrl;
    }

    public void setimageUrl() {
        this.imageUrl = imageUrl;
    }
}

