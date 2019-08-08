package com.example.chatdraw.Drawing;

public class Point {
    private float x;
    private float y;
    private String senderID;
    private String lineID;

    public Point() {
        this.x = -1;
        this.y = -1;
        this.senderID = null;
        this.lineID = null;
    }

    public Point(float x, float y, String senderID, String lineID) {
        this.x = x;
        this.y = y;
        this.senderID = senderID;
        this.lineID = lineID;
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

    public String getLineID() {
        return lineID;
    }

    public void setLineID(String lineID) {
        this.lineID = lineID;
    }
}
