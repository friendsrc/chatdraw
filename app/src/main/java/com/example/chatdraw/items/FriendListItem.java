package com.example.chatdraw.items;

import java.io.Serializable;

public class FriendListItem implements Serializable {

  private String uid;
  private String name;
  private String imageUrl;
  private String chatPreview;

  /**
   * Constructor for FriendListItem.
   */
  public FriendListItem(String name, String chatPreview, String uid, String imageUrl) {
    this.name = name;
    this.chatPreview = chatPreview;
    this.uid = uid;
    this.imageUrl = imageUrl;
  }

  public String getUid() {
    return this.uid;
  }

  public String getName() {
    return this.name;
  }

  public String getChatPreview() {
    return this.chatPreview;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }
}
