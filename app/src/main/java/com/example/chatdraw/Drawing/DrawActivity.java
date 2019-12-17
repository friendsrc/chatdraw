package com.example.chatdraw.Drawing;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.chatdraw.Activities.ChatActivity;
import com.example.chatdraw.Activities.GroupInfoActivity;
import com.example.chatdraw.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.otaliastudios.zoom.ZoomLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class DrawActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private static final String TAG = "DrawActivity";
    private String userUID;
    private String friendsUID;

    private DatabaseReference mRef;
    private ZoomLayout mZoomLayout;
    private CanvasView mCanvasView;

    private ImageView mColorButton;
    private ColorPickerDialog.Builder mColorPickerDialog;
    private Spinner spinner;
    private ImageView switchCanvasImageView;

    private Dialog mCloseDialog;

    private boolean isGroup = false;

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

        mZoomLayout = findViewById(R.id.zoom_layout);
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

        canvasView.setIDs(userUID, friendsUID, findViewById(R.id.canvas), mZoomLayout);
        canvasView.getFromFirebase();

        if (friendsUID.startsWith("GROUP_")) {
            isGroup = true;
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


        spinner = findViewById(R.id.draw_spinner);
        ArrayList<String> memberUids = new ArrayList<>();
        HashMap<String, String> uidsToNames = new HashMap<>();
        ArrayList<String> names = new ArrayList<>();
        names.add("Everyone");

        if (isGroup) {
            FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(friendsUID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ArrayList<String>  membersUids =
                        (ArrayList<String>) documentSnapshot.get("members");
                    for (String s: membersUids) {
                        memberUids.add(s);
                        FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(s)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = (String) dataSnapshot.child("name").getValue();
                                    if (name == null) name = "anonymous";
                                    uidsToNames.put(s, name);
                                    names.add(name);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    }
                });
        } else {
            memberUids.add(userUID);
            memberUids.add(friendsUID);

            for (String s: memberUids) {
                FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(s)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.child("name").getValue();
                            if (name == null) name = "anonymous";
                            uidsToNames.put(s, name);
                            names.add(name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            }
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String id = i == 0 ? CanvasView.EVERYONE_ID : memberUids.get(i - 1);
                Log.d("TESTT", id + " is chosen at spinner");
                mCanvasView.filterById(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set color picker
        mColorButton = findViewById(R.id.color_picker_imageview);
        mColorButton.setColorFilter(
                Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY);
        mColorButton.setOnClickListener(v -> {
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

        switchCanvasImageView = findViewById(R.id.switch_canvas_imageview);
        switchCanvasImageView.setOnClickListener(view -> {
            Intent intent1 =
                new Intent(DrawActivity.this, PersonalDrawActivity.class);
            intent1.putExtra("userUID", userUID);
            intent1.putExtra("friendsUID", friendsUID + userUID);
            startActivity(intent1);
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        showClosePopup();
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
                if (mCanvasView.isEveryoneVisible()) {
                    mCanvasView.undo();
                } else {
                    Toast.makeText(DrawActivity.this,
                        "You need to choose 'Show drawings by: Everyone' before"
                        + " you can undo drawing", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.draw_redo:
                if (mCanvasView.isEveryoneVisible()) {
                    mCanvasView.redo();
                } else {
                    Toast.makeText(DrawActivity.this,
                        "You need to choose 'Show drawings by: Everyone' before"
                            + " you can redo", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.draw_clear:
                if (mCanvasView.isEveryoneVisible()) {
                    mCanvasView.clearCanvas();
                } else {
                    Toast.makeText(DrawActivity.this,
                        "You need to choose 'Show drawings by: Everyone' before"
                            + " you can clear the canvas.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.draw_export:
                if (mCanvasView.isEveryoneVisible()) {
                    if (ContextCompat
                        .checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            909);
                    } else {
                        mCanvasView.exportDrawing();
                    }
                } else {
                    Toast.makeText(DrawActivity.this,
                        "You need to choose 'Show drawings by: Everyone' before"
                            + " you can export drawing", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.draw_pinch_toggle:
                if (item.getTitle().equals("Disable pinch")) {
                    mZoomLayout.setZoomEnabled(false);
                    mZoomLayout.setZoomEnabled(false);
                    item.setTitle("Enable pinch");
                } else {
                    mZoomLayout.setZoomEnabled(true);
                    mZoomLayout.setZoomEnabled(true);
                    item.setTitle("Disable pinch");
                }
                break;
            case R.id.draw_scroll_toggle:
                if (item.getTitle().equals("Disable scroll")) {
                    mZoomLayout.setScrollEnabled(false);
                    mZoomLayout.setTwoFingersScrollEnabled(false);
                    mZoomLayout.setThreeFingersScrollEnabled(false);
                    item.setTitle("Enable scroll");
                } else {
                    mZoomLayout.setScrollEnabled(true);
                    mZoomLayout.setTwoFingersScrollEnabled(true);
                    mZoomLayout.setThreeFingersScrollEnabled(true);
                    item.setTitle("Disable scroll");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 909) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mCanvasView.exportDrawing();
            } else {
                Toast.makeText(this, "Permission not granted",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showClosePopup() {
        mCloseDialog = new Dialog(this);
        mCloseDialog.setContentView(R.layout.confirmationpopup);
        mCloseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mCloseDialog.setCancelable(true);

        Button yesButton = mCloseDialog.findViewById(R.id.close_canvas_agree_btn);
        Button cancelButton = mCloseDialog.findViewById(R.id.close_canvas_cancel_btn);

        cancelButton.setOnClickListener(view -> mCloseDialog.dismiss());
        yesButton.setOnClickListener(view -> {
            mCloseDialog.dismiss();
            finish();
        });

        mCloseDialog.show();
        mCloseDialog.getWindow().setGravity(Gravity.CENTER);
    }
}
