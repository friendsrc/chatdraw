package com.example.chatdraw.drawing;

public class Point {
    private float x;
    private float y;
    private String senderID;
    private String lineID;
    private boolean isVisible;
    private int color;
    private float brushSize;

    public Point() {
        this.x = -1;
        this.y = -1;
        this.senderID = null;
        this.lineID = null;
        this.isVisible = false;
        this.color = 0;
        this.brushSize = 0;
    }

    public Point(float x, float y, String senderID, String lineID, boolean isVisible, int color, float brushSize) {
        this.x = x;
        this.y = y;
        this.senderID = senderID;
        this.lineID = lineID;
        this.isVisible = isVisible;
        this.color = color;
        this.brushSize = brushSize;
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

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(float brushSize) {
        this.brushSize = brushSize;
    }
}
