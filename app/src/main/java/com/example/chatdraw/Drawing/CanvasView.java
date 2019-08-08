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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;


public class CanvasView extends View {

    public static final String TAG = "CanvasView";

    public String userUID;
    public String friendsUID;

    public DatabaseReference mRef;

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    Context context;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(6f);

    }

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

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the mPath with the mPaint on the canvas when onDraw
        canvas.drawPath(mPath, mPaint);
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
//        mPath.moveTo(x, y);
//        mX = x;
//        mY = y;

        mRef.child(System.currentTimeMillis() + "")
                .setValue(new Point(x, y, userUID));
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
//        float dx = Math.abs(x - mX);
//        float dy = Math.abs(y - mY);
//        if (dx >= TOLERANCE || dy >= TOLERANCE) {
//            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
//            mX = x;
//            mY = y;
//        }

        mRef.child(System.currentTimeMillis() + "")
                .setValue(new Point(x, y, userUID));
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    // when ACTION_UP stop touch
    private void upTouch() {
//        mPath.lineTo(mX, mY);

        mRef.child(System.currentTimeMillis() + "")
                .setValue(new Point(-1, -1, userUID));

    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        Log.d(TAG, x + "," + y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }


    public void getFromFirebase() {
        final DatabaseReference ref = mRef;

        final Point[] prevPoint = new Point[1];
        final Point[] currPoint = new Point[1];

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded");
                currPoint[0] =  dataSnapshot.getValue(Point.class);
                if (prevPoint[0] == null) {
                    if (currPoint[0].getX() == -1) {
                        // do nothing
                    } else {
                        mPath.moveTo(currPoint[0].getX(), currPoint[0].getY());
                    }
                } else if (prevPoint[0].getX() == -1) {
                    if (currPoint[0].getX()  == -1) {
                        // do nothing
                    } else {
                        mPath.moveTo(currPoint[0].getX(), currPoint[0].getY());
                    }
                } else {
                    if (currPoint[0].getX() == -1) {
                        // do nothing
                    } else {
                        mPath.quadTo(prevPoint[0].getX(), prevPoint[0].getY(),
                                currPoint[0].getX(), currPoint[0].getY());
                    }
                }

                prevPoint[0] = currPoint[0];
                invalidate();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged()");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                clearCanvas();
                invalidate();
                getFromFirebase();
                ref.removeEventListener(this);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}