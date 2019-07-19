package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

public class ImagePreviewActivity extends AppCompatActivity {

    private static final String TAG = "ImagePreviewActivity";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 111;

    private Bitmap mBitmap;
    private String mSenderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        final String imageUrl = getIntent().getStringExtra("imageUrl");
        mSenderName = getIntent().getStringExtra("senderName");
        ImageView imageView = findViewById(R.id.photo_imageView);
        Picasso.get()
                .load(imageUrl)
                .fit()
                .into(imageView);

        final ImageView closeButton = findViewById(R.id.photo_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TextView senderName = findViewById(R.id.photo_sender_name_textview);
        senderName.setText(mSenderName);

        final ImageView saveButton = findViewById(R.id.photo_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToInternalStorage(imageUrl);
            }
        });

        final View viewTop = findViewById(R.id.photo_view1);
        final View viewBottom = findViewById(R.id.photo_view2);


        final boolean[] isToggledOff = {false};
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isToggledOff[0]) {
                    closeButton.animate().translationY(-300);
                    senderName.animate().translationY(-300);
                    viewTop.animate().translationY(-300);
                    viewBottom.animate().translationY(300);
                    saveButton.animate().translationY(300);
                } else {
                    closeButton.animate().translationY(0);
                    senderName.animate().translationY(0);
                    viewTop.animate().translationY(0);
                    viewBottom.animate().translationY(0);
                    saveButton.animate().translationY(0);
                }
                isToggledOff[0] = !isToggledOff[0];
            }
        });

    }

    public void saveToInternalStorage(String imageUrl) {
        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // loaded bitmap is here (bitmap)
                Log.d(TAG, "imageUrl converted into bitmap: " + bitmap.toString());
                mBitmap = bitmap;

                if (ContextCompat.checkSelfPermission(ImagePreviewActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(ImagePreviewActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    // Permission has already been granted
                    String name = "Image-" + new Timestamp(System.currentTimeMillis()).getTime() + ".jpg";
                    MediaStore.Images.Media.insertImage(
                            getContentResolver(),
                            mBitmap,
                            name,
                            "Sent by " + mSenderName
                    );
                    Toast.makeText(ImagePreviewActivity.this,
                            "Image saved", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                e.printStackTrace();
                Log.d(TAG, "imageUrl convertion to bitmap failed, exception: " + e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    String name = "Image-" + new Timestamp(System.currentTimeMillis()).getTime() + ".jpg";
                    MediaStore.Images.Media.insertImage(
                            getContentResolver(),
                            mBitmap,
                            name,
                            "Sent by " + mSenderName
                    );
                    Toast.makeText(ImagePreviewActivity.this,
                            "Image saved", Toast.LENGTH_SHORT).show();

                } else {
                    // permission denied
                    Toast.makeText(ImagePreviewActivity.this,
                            "Permission not granted, failed to save image",
                            Toast.LENGTH_SHORT).show();

                }
                return;
            }

        }
    }

}
