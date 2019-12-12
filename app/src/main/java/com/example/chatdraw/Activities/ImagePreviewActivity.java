package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
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

        // get imageUrl and image senderName from intent
        final String imageUrl = getIntent().getStringExtra("imageUrl");
        mSenderName = getIntent().getStringExtra("senderName");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // load the image into imageView
        ImageView imageView = findViewById(R.id.photo_imageView);
        Picasso.get()
                .load(imageUrl)
                .resize(width, height)
                .centerInside()
                .into(imageView);

        // set the back button
        final ImageView closeButton = findViewById(R.id.photo_close_button);
        closeButton.setOnClickListener(v -> finish());

        // set the senderName
        final TextView senderName = findViewById(R.id.photo_sender_name_textview);
        senderName.setText(mSenderName);

        // set the saveButton to save image into internal storage
        final ImageView saveButton = findViewById(R.id.photo_save_button);
        saveButton.setOnClickListener(v -> saveToInternalStorage(imageUrl));

        // get the two half-transparent Views
        final View viewTop = findViewById(R.id.photo_view1);
        final View viewBottom = findViewById(R.id.photo_view2);

        // create a toggle to hide all the views except the photo if the photo imageView is tapped
        final boolean[] isToggledOff = {false};
        imageView.setOnClickListener(v -> {
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
        });

    }

    public void saveToInternalStorage(String imageUrl) {
        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // loaded bitmap is here (bitmap)
                Log.d(TAG, "imageUrl converted into bitmap: " + bitmap.toString());
                mBitmap = bitmap;

                // check for permission to write into external storage
                if (ContextCompat.checkSelfPermission(ImagePreviewActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission not yet granted, ask for permission
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(ImagePreviewActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    // Permission has already been granted, continue to save the image
                    String name = "" + new Timestamp(System.currentTimeMillis()).getTime() + ".jpg";
                    insertImage(
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
                Log.d(TAG,
                        "imageUrl convertion to bitmap failed, exception: " + e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, continue to save the image
                String name = "" + new Timestamp(System.currentTimeMillis()).getTime() + ".jpg";
                insertImage(
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


    public static final void insertImage(ContentResolver cr,
                                         Bitmap source,
                                         String title,
                                         String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis()/1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()/1000);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis()/1000);
        values.put(MediaStore.Images.Media.SIZE, source.getByteCount());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

    }

    private static final void storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = (float) 50.0 / source.getWidth();
        float scaleY = (float) 50.0 / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, MediaStore.Images.Thumbnails.MICRO_KIND);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

}
