package com.example.chatdraw.Drawing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.internal.ads.zzagm;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.UUID;


public class CanvasView extends View {

    public static final String TAG = "CanvasView";

    // User data
    public String userUID;
    public String friendsUID;

    // Cached information
    private ArrayList<String> lineIDs = new ArrayList<>();
    private ArrayList<String> removedLineIDs =  new ArrayList<>();
    private TreeMap<String, Path> mapIDtoPath = new TreeMap<>();
    private HashMap<String, Path> mapIDtoRemovedPath = new HashMap<>();
    private HashMap<Path, Paint> mapPathToPaint = new HashMap<>();
    private HashMap<String, Paint> paints = new HashMap<>();

    // Paint currently chosen by the user
    public Paint myPaint;
    public int myCurrentColor = Color.BLACK;
    public float myCurrentBrushSize = 6f;

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
        myPaint = new Paint();
        myPaint.setAntiAlias(true);
        myPaint.setColor(myCurrentColor);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeJoin(Paint.Join.ROUND);
        myPaint.setStrokeWidth(myCurrentBrushSize);

//        mBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);

    }

    // set IDs for connection to Firebase, called directly after CanvasView is instantiated
    public void setIDs(String userID, String friendID) {
        userUID = userID;
        friendsUID = friendID;
        if (friendsUID.startsWith("GROUP_")) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("Drawing")
                    .child(friendsUID);
        } else if (userUID.compareTo(friendsUID) > 0) {
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
        mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);

        for (Path p : mapIDtoPath.values()){
            if (mapPathToPaint.containsKey(p)) {
                mCanvas.drawPath(p, mapPathToPaint.get(p));
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                break;
        }
        return true;
    }

    private void startTouch(float x, float y) {
        // create a unique line id
        currentLineID = userUID + "@" + System.currentTimeMillis();

        // send point to firebase
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(x, y, userUID, currentLineID,
                                true, myCurrentColor, myCurrentBrushSize));
    }

    private void moveTouch(float x, float y) {
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(x, y, userUID, currentLineID,
                                true, myCurrentColor, myCurrentBrushSize));
    }


    private void upTouch() {
        // points with value x = -1 and y = -1 indicates the end of a line
        mRef.child(System.currentTimeMillis() + "")
                .setValue(
                        new Point(-1, -1, userUID, currentLineID,
                                true, myCurrentColor, myCurrentBrushSize));

    }

    public void undo() {
        if (lineIDs.isEmpty()) return;

        // remove line id
        final String lineID  = lineIDs.remove(lineIDs.size()-  1);

        // add the removed path to the collection of removed path
        removedLineIDs.add(lineID);
        Path path  = mapIDtoPath.remove(lineID);
        mapIDtoRemovedPath.put(lineID, path);

        // update the drawing
        invalidate();

        // set the points in firebase to be invisible
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

        // remove line id from removedLineIDs collection
        final String lineID  = removedLineIDs.remove(removedLineIDs.size() -  1);

        // add to the visible path collection
        lineIDs.add(lineID);
        Path path = mapIDtoRemovedPath.remove(lineID);
        mapIDtoPath.put(lineID, path);

        // update the canvas
        invalidate();

        // set the points in firebase to be visible
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

    public void exportDrawing() {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        folder.mkdirs();
        File imageFile = new File(folder, UUID.randomUUID() + ".png");
        try {
            storeBitmap(imageFile, mBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(
                context,
                new String[]{},
                new String[]{"image/png"},
                null);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
    }

    private void storeBitmap(File file, Bitmap bitmap) throws Exception {
        if (!file.exists() && !file.createNewFile())
            throw new Exception("Not able to create " + file.getPath());
        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.flush();
        stream.close();
        Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show();
    }

    public void clearCanvas() {
        // remove collection values
        mapIDtoPath.clear();
        mapIDtoRemovedPath.clear();
        lineIDs.clear();
        removedLineIDs.clear();
        paints.clear();
        mapPathToPaint.clear();

        // remove data in firebase
        mRef.removeValue();

        // reset path
        mPath.reset();

        // set the canvas to be blank
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

                if (!paints.containsKey(currPoint[0].getLineID())) { // start of a new line
                    int color = currPoint[0].getColor();
                    Float size =  currPoint[0].getBrushSize();
                    mPath = new Path();

                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeWidth(size);
                    paints.put(currPoint[0].getLineID(), paint);
                    mapPathToPaint.put(mPath, paint);
                    lineIDs.add(currPoint[0].getLineID());

                    mPath.moveTo(x, y);
                    if (currPoint[0].isVisible()) {
                        mapIDtoPath.put(currPoint[0].getLineID(), mPath);
                    } else {
                        mapIDtoRemovedPath.put(currPoint[0].getLineID(), mPath);
                    }
                    mX = x;
                    mY = y;
                } else if (currPoint[0].getX() == -1) { // end of line
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
                } else if (prevPoint[0] != null){ // middle of the line
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