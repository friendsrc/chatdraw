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

        ImageView closeButton = findViewById(R.id.photo_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
