package com.example.chatdraw.Drawing;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.otaliastudios.zoom.ZoomLayout;

public class DrawActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private static final String TAG = "DrawActivity";
    private String userUID;
    private String friendsUID;

    private DatabaseReference mRef;
    private CanvasView mCanvasView;

    private ImageView mColorButton;
    private ColorPickerDialog.Builder mColorPickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        // get user and friends ID
        Intent intent = getIntent();
        userUID = intent.getStringExtra("userUID");
        friendsUID = intent.getStringExtra("friendsUID");

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set up canvas
        final CanvasView canvasView = findViewById(R.id.canvas);
        mCanvasView = canvasView;

        ZoomLayout zoomLayout = findViewById(R.id.zoom_layout);
        canvasView.actionBar = myToolbar;


        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        android.graphics.Point outPoint = new android.graphics.Point();
        float mRealSizeHeight;
        float mRealSizeWidth;
        if (Build.VERSION.SDK_INT >= 19) {
            // include navigation bar
            display.getRealSize(outPoint);
        } else {
            // exclude navigation bar
            display.getSize(outPoint);
        }
        if (outPoint.y > outPoint.x) {
            mRealSizeHeight = outPoint.y;
            mRealSizeWidth = outPoint.x;
        } else {
            mRealSizeHeight = outPoint.x;
            mRealSizeWidth = outPoint.y;
        }

        canvasView.widthMultiplier = 2048f / mRealSizeWidth;
        canvasView.heightMultiplier = 2048f / mRealSizeHeight;


        // set drawing scale
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
//        canvasView.widthMultiplier = 2048f / width;
//        canvasView.heightMultiplier = 2048f / (height);
        canvasView.setIDs(userUID, friendsUID, findViewById(R.id.canvas_container), zoomLayout);
        canvasView.getFromFirebase();

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


        // set color picker
        mColorButton = findViewById(R.id.color_picker_imageview);
        mColorButton.setColorFilter(
                Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY);
        mColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] colorPresets = {Color.parseColor("#000000"),
                        Color.parseColor("#293462"),
                        Color.parseColor("#216583"),
                        Color.parseColor("#f76262"),
                        Color.parseColor("#fff1c1"),
                };
                mColorPickerDialog = ColorPickerDialog.newBuilder();
                mColorPickerDialog
                        .setShowAlphaSlider(true)
                        .setColor(Color.parseColor("#000000"))
                        .setPresets(colorPresets)
                        .show(DrawActivity.this);
            }
        });

        // set brush size picker seekbar
        SeekBar sizePicker = findViewById(R.id.brush_size_seekbar);
        sizePicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCanvasView.myCurrentBrushSize = (float) progress / 10;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_draw, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onColorSelected(int dialogId, int color) {
        mColorButton.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        mCanvasView.myCurrentColor = color;
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.draw_undo:
                mCanvasView.undo();
                break;
            case R.id.draw_redo:
                mCanvasView.redo();
                break;
            case R.id.draw_clear:
                mCanvasView.clearCanvas();
                break;
            case R.id.draw_export:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            909);
                } else {
                    mCanvasView.exportDrawing();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 909) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCanvasView.exportDrawing();
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }
}
