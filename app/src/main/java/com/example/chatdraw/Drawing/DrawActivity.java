package com.example.chatdraw.Drawing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Region;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;

import com.example.chatdraw.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";
    private String userUID;
    private String friendsUID;

    private DatabaseReference mRef;
    private CanvasView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        // get user and friends ID
        Intent intent = getIntent();
        userUID = intent.getStringExtra("userUID");
        friendsUID = intent.getStringExtra("friendsUID");

        // set up canvas
        final CanvasView canvasView = findViewById(R.id.canvas);
        mCanvasView = canvasView;
        canvasView.setIDs(userUID, friendsUID);
        canvasView.getFromFirebase();

        if (userUID.compareTo(friendsUID) > 0) {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("Drawing")
                    .child(userUID + "|" + friendsUID);
        } else {
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child("Drawing")
                    .child(friendsUID + "|" + userUID);
        }

        // set clear canvas button
        Button clearButton = findViewById(R.id.clear_drawing_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.clearCanvas();
            }
        });

        // set undo button
        final Button undoButton = findViewById(R.id.undo_drawing_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.undo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
