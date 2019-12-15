package com.example.chatdraw.drawing;

public class Point {
  private float pointX;
  private float pointY;
  private String senderID;
  private String lineID;
  private boolean isVisible;
  private int color;
  private float brushSize;

  /**
   * Default constructor for Point.
   */
  public Point() {
    this.pointX = -1;
    this.pointY = -1;
    this.senderID = null;
    this.lineID = null;
    this.isVisible = false;
    this.color = 0;
    this.brushSize = 0;
  }

  /**
   * Constructor for Point.
   */
  public Point(float pointX, float pointY, String senderID, String lineID, boolean isVisible,
               int color, float brushSize) {
    this.pointX = pointX;
    this.pointY = pointY;
    this.senderID = senderID;
    this.lineID = lineID;
    this.isVisible = isVisible;
    this.color = color;
    this.brushSize = brushSize;
  }

  public float getPointX() {
    return pointX;
  }

  public void setPointX(float pointX) {
    this.pointX = pointX;
  }

  public float getPointY() {
    return pointY;
  }

  public void setPointY(float pointY) {
    this.pointY = pointY;
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
