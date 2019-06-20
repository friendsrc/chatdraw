package com.example.chatdraw.MainChat;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.chatdraw.R;

public class NewGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        getSupportActionBar().setTitle("New Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
