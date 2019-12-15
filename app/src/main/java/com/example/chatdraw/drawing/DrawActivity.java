package com.example.chatdraw.drawing;

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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chatdraw.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.otaliastudios.zoom.ZoomLayout;

public class DrawActivity extends AppCompatActivity implements ColorPickerDialogListener {

  private static final String TAG = "DrawActivity";
  private String userUid;
  private String friendsUid;

  private DatabaseReference databaseReference;
  private ZoomLayout zoomLayout;
  private CanvasView canvasView;

  private ImageView colorImageView;
  private ColorPickerDialog.Builder colorPickerDialogBuilder;

  private Dialog finishActivityDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_draw);

    // get user and friends ID
    Intent intent = getIntent();
    userUid = intent.getStringExtra("userUid");
    friendsUid = intent.getStringExtra("friendUid");

    // set the action bar
    Toolbar myToolbar = findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle("");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    // set up canvas
    final CanvasView canvasView = findViewById(R.id.canvas);
    this.canvasView = canvasView;

    zoomLayout = findViewById(R.id.zoom_layout);
    canvasView.actionBar = myToolbar;


    WindowManager windowManager =
        (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
    final Display display = windowManager.getDefaultDisplay();
    android.graphics.Point outPoint = new android.graphics.Point();
    float realHeight;
    float realWidth;
    if (Build.VERSION.SDK_INT >= 19) {
      // include navigation bar
      display.getRealSize(outPoint);
    } else {
      // exclude navigation bar
      display.getSize(outPoint);
    }
    if (outPoint.y > outPoint.x) {
      realHeight = outPoint.y;
      realWidth = outPoint.x;
    } else {
      realHeight = outPoint.x;
      realWidth = outPoint.y;
    }

    canvasView.widthMultiplier = 2048f / realWidth;
    canvasView.heightMultiplier = 2048f / realHeight;

    canvasView.setIDs(userUid, friendsUid, findViewById(R.id.canvas), zoomLayout);
    canvasView.getFromFirebase();

    if (friendsUid.startsWith("GROUP_")) {
      Log.d("TESTT", "Draw starts with " + friendsUid);
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(friendsUid);
    } else if (userUid.compareTo(friendsUid) > 0) {
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(userUid + "|" + friendsUid);
    } else {
      databaseReference = FirebaseDatabase.getInstance().getReference()
          .child("drawing")
          .child(friendsUid + "|" + userUid);
    }


    // set color picker
    colorImageView = findViewById(R.id.color_picker_imageview);
    colorImageView.setColorFilter(
        Color.parseColor("#000000"), PorterDuff.Mode.MULTIPLY);
    colorImageView.setOnClickListener(v -> {
      int[] colorPresets = {Color.parseColor("#000000"),
          Color.parseColor("#293462"),
          Color.parseColor("#216583"),
          Color.parseColor("#f76262"),
          Color.parseColor("#fff1c1"),
      };
      colorPickerDialogBuilder = ColorPickerDialog.newBuilder();
      colorPickerDialogBuilder
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
        DrawActivity.this.canvasView.myCurrentBrushSize = (float) progress / 10;
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
    colorImageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    canvasView.myCurrentColor = color;
  }

  @Override
  public void onDialogDismissed(int dialogId) {

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.draw_undo:
        canvasView.undo();
        break;
      case R.id.draw_redo:
        canvasView.redo();
        break;
      case R.id.draw_clear:
        canvasView.clearCanvas();
        break;
      case R.id.draw_export:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
          // Permission is not granted
          ActivityCompat.requestPermissions(this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              909);
        } else {
          canvasView.exportDrawing();
        }
        break;
      case R.id.draw_pinch_toggle:
        if (item.getTitle().equals("Disable pinch")) {
          zoomLayout.setZoomEnabled(false);
          zoomLayout.setZoomEnabled(false);
          item.setTitle("Enable pinch");
        } else {
          zoomLayout.setZoomEnabled(true);
          zoomLayout.setZoomEnabled(true);
          item.setTitle("Disable pinch");
        }
        break;
      case R.id.draw_scroll_toggle:
        if (item.getTitle().equals("Disable scroll")) {
          zoomLayout.setScrollEnabled(false);
          zoomLayout.setTwoFingersScrollEnabled(false);
          zoomLayout.setThreeFingersScrollEnabled(false);
          item.setTitle("Enable scroll");
        } else {
          zoomLayout.setScrollEnabled(true);
          zoomLayout.setTwoFingersScrollEnabled(true);
          zoomLayout.setThreeFingersScrollEnabled(true);
          item.setTitle("Disable scroll");
        }
        break;
      default:
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
        canvasView.exportDrawing();
      } else {
        Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * Displays exit canvas confirmation popup.
   */
  public void showClosePopup() {
    finishActivityDialog = new Dialog(this);
    finishActivityDialog.setContentView(R.layout.confirmationpopup);
    finishActivityDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    finishActivityDialog.setCancelable(true);

    Button yesButton = finishActivityDialog.findViewById(R.id.close_canvas_agree_btn);
    Button cancelButton = finishActivityDialog.findViewById(R.id.close_canvas_cancel_btn);

    cancelButton.setOnClickListener(view -> finishActivityDialog.dismiss());
    yesButton.setOnClickListener(view -> {
      finishActivityDialog.dismiss();
      finish();
    });

    finishActivityDialog.show();
    finishActivityDialog.getWindow().setGravity(Gravity.CENTER);
  }
}
