package com.example.chatdraw.drawing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.otaliastudios.zoom.ZoomLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;


public class CanvasView extends View {

  public static final String TAG = "CanvasView";

  // User data
  public String userUid;
  public String friendUid;

  // drawing scale
  ZoomLayout zoomLayout;
  View canvasContainer;
  Toolbar actionBar;
  public float widthMultiplier;
  public float heightMultiplier;

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
  public float myCurrentBrushSize = 20f;

  // Path
  private float xposition;
  private float yposition;
  private Path currentPath;
  Context context;
  private static final float TOUCH_TOLERANCE = 5;
  private String currentLineID;

  // Firebase
  public DatabaseReference databaseReference;
  ChildEventListener childEventListener;

  // Canvas
  public int width;
  public int height;
  private Canvas canvas;

  // Generated bitmap
  private Bitmap bitmap;


  /**
   * Constructor for CanvasView.
   * @param c context
   * @param attrs attribute set
   */
  public CanvasView(Context c, AttributeSet attrs) {
    super(c, attrs);
    context = c;

    // create a new Path
    currentPath = new Path();

    // create a new Paint
    myPaint = new Paint();
    myPaint.setAntiAlias(true);
    myPaint.setColor(myCurrentColor);
    myPaint.setStyle(Paint.Style.STROKE);
    myPaint.setStrokeJoin(Paint.Join.ROUND);
    myPaint.setStrokeWidth(myCurrentBrushSize);

  }

  /**
   * Sets the IDs for connection to Firebase, called directly after CanvasView is instantiated.
   */
  public void setIDs(String userID, String friendID, View view, ZoomLayout layout) {
    userUid = userID;
    friendUid = friendID;
    if (friendUid.startsWith("GROUP_")) {
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(friendUid);
    } else if (userUid.compareTo(friendUid) > 0) {
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(userUid + "|" + friendUid);
    } else {
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(friendUid + "|" + userUid);
    }

    canvasContainer = view;
    zoomLayout = layout;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    bitmap = Bitmap.createBitmap(2548, 2548, Bitmap.Config.ARGB_8888);
    this.canvas = new Canvas(bitmap);
    this.canvas.drawColor(Color.WHITE);

    for (Path p : mapIDtoPath.values()) {
      if (mapPathToPaint.containsKey(p)) {
        this.canvas.drawPath(p, mapPathToPaint.get(p));
        canvas.drawPath(p, mapPathToPaint.get(p));
      }
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int[] viewLocation = new int[2];
    getLocationOnScreen(viewLocation);

    float x = event.getX() * widthMultiplier  / zoomLayout.getZoom() - zoomLayout.getPanX();
    float y = event.getY()  * widthMultiplier / zoomLayout.getZoom() - zoomLayout.getPanY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startTouch(x, y);
        break;
      case MotionEvent.ACTION_MOVE:
        moveTouch(x, y);
        break;
      case MotionEvent.ACTION_UP:
        upTouch(x, y);
        break;
      default:
        break;
    }
    return true;
  }

  private void startTouch(float x, float y) {
    // create a unique line id
    currentLineID = userUid + "@" + System.currentTimeMillis();

    // send point to firebase
    databaseReference.child(System.currentTimeMillis() + "")
        .setValue(
            new Point(x, y, userUid, currentLineID,
                true, myCurrentColor, myCurrentBrushSize));
  }

  private void moveTouch(float x, float y) {
    databaseReference.child(System.currentTimeMillis() + "")
        .setValue(
            new Point(x, y, userUid, currentLineID,
                true, myCurrentColor, myCurrentBrushSize));
  }


  private void upTouch(float x, float y) {
    // points with value xposition = -1 and yposition = -1 indicates the end of a line
    databaseReference.child(System.currentTimeMillis() + "")
        .setValue(
            new Point(x, y, userUid, currentLineID,
                true, myCurrentColor, myCurrentBrushSize));

    databaseReference.child(System.currentTimeMillis() + "")
        .setValue(
            new Point(-1, -1, userUid, currentLineID,
                true, myCurrentColor, myCurrentBrushSize));

  }

  /**
   * Undo the last line drawn.
   */
  public void undo() {
    if (lineIDs.isEmpty()) {
      return;
    }

    // remove line id
    final String lineID  = lineIDs.remove(lineIDs.size() -  1);

    // add the removed path to the collection of removed path
    removedLineIDs.add(lineID);
    Path path  = mapIDtoPath.remove(lineID);
    mapIDtoRemovedPath.put(lineID, path);

    // update the drawing
    invalidate();

    // set the points in firebase to be invisible
    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

  /**
   * Redo the last undo.
   */
  public void redo() {
    if (removedLineIDs.isEmpty()) {
      return;
    }

    // remove line id from removedLineIDs collection
    final String lineID  = removedLineIDs.remove(removedLineIDs.size() -  1);

    // add to the visible path collection
    lineIDs.add(lineID);
    Path path = mapIDtoRemovedPath.remove(lineID);
    mapIDtoPath.put(lineID, path);

    // update the canvas
    invalidate();

    // set the points in firebase to be visible
    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

  /**
   * Export the drawing into .png format.
   */
  public void exportDrawing() {
    File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    folder.mkdirs();
    File imageFile = new File(folder, UUID.randomUUID() + ".png");
    try {
      storeBitmap(imageFile, bitmap);
    } catch (Exception e) {
      e.printStackTrace();
    }
    MediaScannerConnection.scanFile(
        context,
        new String[]{},
        new String[]{"image/png"},
        null);
    context.sendBroadcast(
        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
  }

  private void storeBitmap(File file, Bitmap bitmap) throws Exception {
    if (!file.exists() && !file.createNewFile()) {
      throw new Exception("Not able to create " + file.getPath());
    }
    FileOutputStream stream = new FileOutputStream(file);
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    stream.flush();
    stream.close();
    Toast.makeText(context, "Image saved", Toast.LENGTH_SHORT).show();
  }

  /**
   * Remove all data.
   */
  public void clearCanvas() {
    // remove collection values
    mapIDtoPath.clear();
    mapIDtoRemovedPath.clear();
    lineIDs.clear();
    removedLineIDs.clear();
    paints.clear();
    mapPathToPaint.clear();

    // remove data in firebase
    databaseReference.removeValue();

    // reset path
    currentPath.reset();

    // set the canvas to be blank
    invalidate();
  }

  /**
   * Fetch data of lines from Firebase.
   */
  public void getFromFirebase() {
    final DatabaseReference ref = databaseReference;

    final Point[] prevPoint = new Point[1];
    final Point[] currPoint = new Point[1];

    childEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Log.d(TAG, "onChildAdded");

        currPoint[0] =  dataSnapshot.getValue(Point.class);
        if (currPoint[0] == null) {
          return;
        }

        float x = currPoint[0].getPointX();
        float y  = currPoint[0].getPointY();

        if (!paints.containsKey(currPoint[0].getLineID())) { // start of a new line
          removedLineIDs.clear();
          int color = currPoint[0].getColor();
          currentPath = new Path();

          Paint paint = new Paint();
          paint.setAntiAlias(true);
          paint.setColor(color);
          paint.setStyle(Paint.Style.STROKE);
          paint.setStrokeJoin(Paint.Join.ROUND);
          float size =  currPoint[0].getBrushSize();
          paint.setStrokeWidth(size);
          paints.put(currPoint[0].getLineID(), paint);
          mapPathToPaint.put(currentPath, paint);
          lineIDs.add(currPoint[0].getLineID());

          currentPath.moveTo(x, y);
          if (currPoint[0].isVisible()) {
            mapIDtoPath.put(currPoint[0].getLineID(), currentPath);
          } else {
            mapIDtoRemovedPath.put(currPoint[0].getLineID(), currentPath);
          }
          CanvasView.this.xposition = x;
          CanvasView.this.yposition = y;
        } else if (currPoint[0].getPointX() == -1) { // end of line
          String lineID = currPoint[0].getLineID();
          paints.remove(lineID);
          Path path;
          if (mapIDtoPath.containsKey(lineID)) {
            path = mapIDtoPath.get(lineID);
          } else {
            path = mapIDtoRemovedPath.get(lineID);
          }

          path.lineTo(CanvasView.this.xposition, CanvasView.this.yposition);
          currentPath = new Path();
        } else if (prevPoint[0] != null) { // middle of the line
          float dx = Math.abs(x - CanvasView.this.xposition);
          float dy = Math.abs(y - CanvasView.this.yposition);
          if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            String key = currPoint[0].getLineID();
            if (mapIDtoPath.containsKey(key)) {
              mapIDtoPath.get(currPoint[0].getLineID())
                  .quadTo(CanvasView.this.xposition, CanvasView.this.yposition,
                      (x + CanvasView.this.xposition) / 2,
                      (y + CanvasView.this.yposition) / 2);
            } else {
              mapIDtoRemovedPath.get(currPoint[0].getLineID())
                  .quadTo(CanvasView.this.xposition, CanvasView.this.yposition,
                      (x + CanvasView.this.xposition) / 2,
                      (y + CanvasView.this.yposition) / 2);
            }

            CanvasView.this.xposition = x;
            CanvasView.this.yposition = y;
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
    ref.addChildEventListener(childEventListener);
  }
}