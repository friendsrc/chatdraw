package com.example.chatdraw.Drawing;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.example.chatdraw.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

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

        // set redo button
        final Button redoButton = findViewById(R.id.redo_drawing_button);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvasView.redo();
            }
        });

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Canvas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                        .setShowAlphaSlider(false)
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
                mCanvasView.currentBrushSize = (float) progress / 10;
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
        mCanvasView.currentColor = color;
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}
