package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.chatdraw.R;

public class GroupInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        TextView textView = findViewById(R.id.test_test);
        textView.setText("blah with id " + getIntent().getStringExtra("id"));
    }
}
