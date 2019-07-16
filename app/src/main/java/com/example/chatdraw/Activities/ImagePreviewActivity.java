package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        String imageUrl = getIntent().getStringExtra("imageUrl");
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

        final ImageView saveButton = findViewById(R.id.photo_save_button);
        final View viewTop = findViewById(R.id.photo_view1);
        final View viewBottom = findViewById(R.id.photo_view2);

        final boolean[] isToggledOff = {false};

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isToggledOff[0]) {
                    closeButton.animate().translationY(-200);
                    viewTop.animate().translationY(-200);
                    viewBottom.animate().translationY(300);
                    saveButton.animate().translationY(300);
                } else {
                    closeButton.animate().translationY(0);
                    viewTop.animate().translationY(0);
                    viewBottom.animate().translationY(0);
                    saveButton.animate().translationY(0);
                }
                isToggledOff[0] = !isToggledOff[0];
            }
        });

    }
}
