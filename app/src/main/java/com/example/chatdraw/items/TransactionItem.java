package com.example.chatdraw.items;

import java.util.Calendar;
import java.util.Date;

public class TransactionItem {
    private String mText1;
    private String mText2;
    private Date timestamp;

    public TransactionItem() {

    }

    public TransactionItem(String text1, String text2) {
        this.mText1 = text1;
        this.mText2 = text2;
        this.timestamp= Calendar.getInstance().getTime();
    }


    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }

    public Date getTimestamp() { return timestamp; }

    public void setText1(String text1) {
        this.mText1 = text1;
    }

    public void setText2(String text2) {
        this.mText2 = text2;
    }

    public void setTimestamp (Date timestamp) {
        this.timestamp = timestamp;
    }
}
