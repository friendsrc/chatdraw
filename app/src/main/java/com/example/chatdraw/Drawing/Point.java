package com.example.chatdraw.Drawing;

public class Point {
    private float x;
    private float y;
    private String senderID;

    public Point() {
        this.x = -1;
        this.y = -1;
        this.senderID = null;
    }

    public Point(float x, float y, String senderID) {
        this.x = x;
        this.y = y;
        this.senderID = senderID;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getSenderID() {
        return this.senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
}
