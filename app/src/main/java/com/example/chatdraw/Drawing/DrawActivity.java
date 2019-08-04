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

import com.example.chatdraw.R;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        CanvasView canvasView = findViewById(R.id.canvas);
        Intent intent = getIntent();
        canvasView.setIDs(intent.getStringExtra("userUID")
                , intent.getStringExtra("friendsUID"));
        canvasView.getFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
