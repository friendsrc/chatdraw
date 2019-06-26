package com.example.chatdraw.AccountActivity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatdraw.MainChat.MainActivity;
import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalActivity extends AppCompatActivity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        Button laterButton, finishButton;
        final EditText inputName, inputUsername;

        inputName = (EditText) findViewById(R.id.profile_text);
        inputUsername = (EditText) findViewById(R.id.username_text);
        laterButton = (Button) findViewById(R.id.later_button);
        finishButton = (Button) findViewById(R.id.finish_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        laterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalActivity.this, MainActivity.class));
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String temp = inputName.getText().toString().trim();

                if (temp.length() > 20) {
                    inputName.setError(getString(R.string.long_name));
                    inputName.requestFocus();
                    return;
                }

                String profile = temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
                String username = "@" + inputUsername.getText().toString().trim();

                if (username.length() < 4 && username.length() != 1) {
                    inputUsername.setError(getString(R.string.short_username));
                    inputUsername.requestFocus();
                    return;
                }

                if (username.length() > 21) {
                    inputUsername.setError(getString(R.string.long_username));
                    inputUsername.requestFocus();
                    return;
                }

//                SharedPreferences prefs = getSharedPreferences("myprefs", MODE_PRIVATE);
//                SharedPreferences.Editor prefsEditor = prefs.edit();
//                prefsEditor.putString("profilename", profile);
//                prefsEditor.putString("username", username);
//                prefsEditor.apply();

//                // load the highscore using sharedPreferences for permanent storing of an important value
//                SharedPreferences prefs = getSharedPreferences("myprefs", MODE_PRIVATE);
//                highscore = prefs.getInt("highscore", 0);


                progressBar.setVisibility(View.VISIBLE);
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = currentFirebaseUser.getEmail();

                if (TextUtils.isEmpty(profile)) {
                    if (!TextUtils.isEmpty(username)) {
                        updateUser(currentFirebaseUser.getUid(), userEmail, null, username);
                    }
                } else {
                    if (!TextUtils.isEmpty(username)) {
                        updateUser(currentFirebaseUser.getUid(), userEmail, profile, username);
                    } else {
                        updateUser(currentFirebaseUser.getUid(), userEmail, profile, null);
                    }
                }

                startActivity(new Intent(PersonalActivity.this, MainActivity.class));
            }
        });
    }

    private boolean updateUser(String Uid, String email, String name, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Uid);

        User usering = new User(email, name, username);
        databaseReference.setValue(usering);

        // update firestore
        FirebaseFirestore.getInstance().collection("Users").document(Uid)
                .set(usering);

        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show();

        return true;
    }
}
