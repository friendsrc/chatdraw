package com.example.chatdraw.items;

import java.util.Calendar;
import java.util.Date;

public class TransactionItem {
  private String text1;
  private String text2;
  private Date timestamp;

  public TransactionItem() {

  }

  /**
   * Constructor for TransactionItem.
   */
  public TransactionItem(String text1, String text2) {
    this.text1 = text1;
    this.text2 = text2;
    this.timestamp = Calendar.getInstance().getTime();
  }


  public String getText1() {
    return text1;
  }

  public String getText2() {
    return text2;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setText1(String text1) {
    this.text1 = text1;
  }

  public void setText2(String text2) {
    this.text2 = text2;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
