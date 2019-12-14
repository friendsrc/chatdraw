package com.example.chatdraw.accountactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.chatdraw.R;

import java.util.List;

import io.paperdb.Paper;

public class SetPatternActivity extends AppCompatActivity {

    String save_pattern_key = "pattern_code";
    String final_pattern = "";

    PatternLockView mPatternLockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pattern);

        TextView textView = findViewById(R.id.setPatternText);

        Paper.init(this);
        String save_pattern = Paper.book().read(save_pattern_key);
        if (save_pattern != null && !save_pattern.equals("null")) {

            textView.setText("Enter current password");
            mPatternLockView = findViewById(R.id.pattern_lock_view);
            mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onProgress(List<PatternLockView.Dot> progressPattern) {

                }

                @Override
                public void onComplete(List<PatternLockView.Dot> pattern) {
                    final_pattern = PatternLockUtils.patternToString(mPatternLockView, pattern);

                    Log.v("MYPATTERN", "" + final_pattern);

                    if (final_pattern.equals(save_pattern)) {
                        finish();
                        Intent intention = new Intent(SetPatternActivity.this, CreatePatternActivity.class);
                        startActivity(intention);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        Toast.makeText(SetPatternActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCleared() {

                }
            });
        } else {
            finish();
            Intent intention = new Intent(SetPatternActivity.this, CreatePatternActivity.class);
            startActivity(intention);
        }
    }
}
