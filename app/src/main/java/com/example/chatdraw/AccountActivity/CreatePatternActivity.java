package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.chatdraw.R;

import java.util.List;

import io.paperdb.Paper;

public class CreatePatternActivity extends AppCompatActivity {

    String save_pattern_key = "pattern_code";
    String final_pattern = "";

    PatternLockView mPatternLockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pattern);

        TextView textView = findViewById(R.id.createPatternText);
        textView.setText("Set a new pattern passcode");

        Button btnSetup = findViewById(R.id.btnSetPattern);
        btnSetup.setEnabled(false);

        mPatternLockView = findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                btnSetup.setEnabled(false);
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                final_pattern = PatternLockUtils.patternToString(mPatternLockView, pattern);
                Log.v("MYPATTERN", "" + final_pattern);

                if (final_pattern.length() < 4) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(CreatePatternActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                } else {
                    btnSetup.setEnabled(true);
                    btnSetup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Paper.book().write(save_pattern_key, final_pattern);
                            Toast.makeText(CreatePatternActivity.this, "Pattern saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCleared() {

            }
        });
    }
}
