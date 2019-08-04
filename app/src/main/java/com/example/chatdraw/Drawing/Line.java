package com.example.chatdraw.Drawing;

import java.util.HashMap;
import java.util.LinkedList;

public class Line {

    private int size = 0;
    private LinkedList<Point> points;

    public Line() {
        points = new LinkedList<>();
    }

    public void addPoint(float x, float y) {
        size++;
        points.add(new Point(x, y));
    }

    public int getSize() {
        return size;
    }
}
