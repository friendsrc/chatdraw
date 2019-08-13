package com.example.chatdraw.Drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.internal.ads.zzagm;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class CanvasView extends View {

    public static final String TAG = "CanvasView";

    // User data
    public String userUID;
    public String friendsUID;

    // Cached information
    private ArrayList<String> lineIDs = new ArrayList<>();
    private ArrayList<String> removedLineIDs =  new ArrayList<>();
    private HashMap<String, Path> mapIDtoPath = new HashMap<>();
    private HashMap<String, Path> mapIDtoRemovedPath = new HashMap<>();
    private HashMap<Path, Paint> mapPathToPaint = new HashMap<>();
    private HashMap<String, Paint> paints = new HashMap<>();



    // Paint
    public Paint mPaint;
    public int currentColor = Color.BLACK;
    public float currentBrushSize = 6f;

    // Path
    private float mX, mY;
    private Path mPath; //current path
    Context context;
    private static final float TOUCH_TOLERANCE = 5;
    private String currentLineID;

    // Firebase
    public DatabaseReference mRef;
    ChildEventListener mChildEventListener;

    // Canvas
    public int width;
    public int height;
    private Canvas mCanvas;

    // Generated bitmap
    private Bitmap mBitmap;


    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);

        context = c;

        // create a new Path
        mPath = new Path();

        // create a new Paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(currentBrushSize);

    }

    // set IDs for connection to Firebase, called directly after CanvasView is instantiated
    public void setIDs(String userID, String friendID) {
        userUID = userID;
        friendsUID = friendID;
        if (userUID.compareTo(friendsUID) > 0) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("Drawing")
                    .child(userUID + "|" + friendsUID);
        } else {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("Drawing")
                    .child(friendsUID + "|" + userUID);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        for (Path p : mapIDtoPath.values()){
            if (mapPathToPaint.containsKey(p)) {
                canvas.drawPath(p, mapPathToPaint.get(p));
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        Log.d(TAG, x + "," + y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
//                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch(x, y);
//                invalidate();
                break;
        }
        return true;
    }

    private void startTouch(float x, float y) {
        currentLineID = userUID + "@" + System.currentTimeMillis();
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(x, y, userUID, currentLineID,
                                true, currentColor, currentBrushSize));
    }

    private void moveTouch(float x, float y) {
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(x, y, userUID, currentLineID,
                                true, currentColor, currentBrushSize));
    }


    private void upTouch(float x, float y) {
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(-1, -1, userUID, currentLineID,
                                true, currentColor, currentBrushSize));

    }

    public void undo() {
        if (lineIDs.isEmpty()) return;
        final String lineID  = lineIDs.remove(lineIDs.size()-  1);
        removedLineIDs.add(lineID);
        Path path  = mapIDtoPath.remove(lineID);
        mapIDtoRemovedPath.put(lineID, path);
        invalidate();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Point point = ds.getValue(Point.class);
                    if (point.getLineID().equals(lineID)) {
                        point.setVisible(false);
                        ds.getRef().setValue(point);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void redo() {
        if (removedLineIDs.isEmpty()) return;
        final String lineID  = removedLineIDs.remove(removedLineIDs.size() -  1);
        lineIDs.add(lineID);
        Path path = mapIDtoRemovedPath.remove(lineID);
        mapIDtoPath.put(lineID, path);
        invalidate();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Point point = ds.getValue(Point.class);
                    if (point.getLineID().equals(lineID)) {
                        point.setVisible(true);
                        ds.getRef().setValue(point);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void clearCanvas() {
        mapIDtoPath.clear();
        lineIDs.clear();
        mRef.removeValue();
        mPath.reset();
        invalidate();
    }

    public void getFromFirebase() {
        final DatabaseReference ref = mRef;

        final Point[] prevPoint = new Point[1];
        final Point[] currPoint = new Point[1];

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded");
                currPoint[0] =  dataSnapshot.getValue(Point.class);

                if (currPoint[0] == null) return;

                float x = currPoint[0].getX();
                float y  = currPoint[0].getY();

                if (!paints.containsKey(currPoint[0].getLineID())) {
                    // start of a new line
                    currentColor = currPoint[0].getColor();
                    currentBrushSize =  currPoint[0].getBrushSize();

                    mPaint = new Paint();
                    mPaint.setAntiAlias(true);
                    mPaint.setColor(currentColor);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeJoin(Paint.Join.ROUND);
                    mPaint.setStrokeWidth(currentBrushSize);
                    paints.put(currPoint[0].getLineID(), mPaint);
                    mapPathToPaint.put(mPath, mPaint);
                    lineIDs.add(currPoint[0].getLineID());

                    mPath.moveTo(x, y);
                    if (currPoint[0].isVisible()) {
                        mapIDtoPath.put(currPoint[0].getLineID(), mPath);
                    } else {
                        mapIDtoRemovedPath.put(currPoint[0].getLineID(), mPath);
                    }
                    mX = x;
                    mY = y;
                } else if (currPoint[0].getX() == -1) {
                    // line ended
                    String lineID = currPoint[0].getLineID();
                    paints.remove(lineID);
                    Path path;
                    if (mapIDtoPath.containsKey(lineID)) {
                        path = mapIDtoPath.get(lineID);
                    } else {
                        path = mapIDtoRemovedPath.get(lineID);
                    }

                    path.lineTo(mX, mY);
                    mPath = new Path();
                } else if (prevPoint[0] != null){
                    // middle points
                    float dx = Math.abs(x - mX);
                    float dy = Math.abs(y - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        String key = currPoint[0].getLineID();
                        if (mapIDtoPath.containsKey(key)) {
                            mapIDtoPath.get(currPoint[0].getLineID())
                                    .quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        } else {
                            mapIDtoRemovedPath.get(currPoint[0].getLineID())
                                    .quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                        }

                        mX = x;
                        mY = y;
                    }
                }

                prevPoint[0] = currPoint[0];
                invalidate();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged()");
                Point point = dataSnapshot.getValue(Point.class);
                if (point.isVisible()) {
                    if (mapIDtoRemovedPath.containsKey(point.getLineID())) {
                        Path path = mapIDtoRemovedPath.remove(point.getLineID());
                        mapIDtoPath.put(point.getLineID(), path);

                    }
                } else {
                    if (mapIDtoPath.containsKey(point.getLineID())) {
                        Path path  = mapIDtoPath.remove(point.getLineID());
                        mapIDtoRemovedPath.put(point.getLineID(), path);
                    }
                }
                invalidate();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved()");
                mapIDtoPath.clear();
                lineIDs.clear();
                invalidate();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        ref.addChildEventListener(mChildEventListener);

    }


}