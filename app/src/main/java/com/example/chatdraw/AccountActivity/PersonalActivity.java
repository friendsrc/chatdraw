package com.example.chatdraw.AccountActivity;

import android.app.Person;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.MainChat.MainActivity;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                String profile = inputName.getText().toString().trim();
                String username = inputUsername.getText().toString().trim();

                if (username.length() < 3) {
                    inputUsername.setError(getString(R.string.short_username));
                    inputUsername.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (TextUtils.isEmpty(profile)) {
                    if (!TextUtils.isEmpty(username)) {
                        updateUser(currentFirebaseUser.getUid(), null, username);
                    }
                } else {
                    if (!TextUtils.isEmpty(username)) {
                        updateUser(currentFirebaseUser.getUid(), profile, username);
                    } else {
                        updateUser(currentFirebaseUser.getUid(), profile, null);
                    }
                }

                startActivity(new Intent(PersonalActivity.this, MainActivity.class));
            }
        });
    }

    private boolean updateUser(String Uid, String name, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Uid);

        User usering = new User(name, username);
        databaseReference.setValue(usering);

        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show();

        return true;
    }
}
