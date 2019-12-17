package com.example.chatdraw.Items;

public class SimpleMenuItem {
    private String name;
    private int imageDrawable;

    public SimpleMenuItem(String name, int imageDrawable) {
        this.name = name;
        this.imageDrawable = imageDrawable;
    }

    public String getName() {
        return this.name;
    }

    public int getImageDrawable() { return this.imageDrawable; }
}
