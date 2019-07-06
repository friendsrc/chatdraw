package com.example.chatdraw.AccountActivity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatdraw.ChatActivites.MainActivity;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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


        String sessionName = getIntent().getStringExtra("GoogleName");
        final String googleUserID = getIntent().getStringExtra("userID");
        inputName.setText(sessionName);

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

                if (temp.length() < 3) {
                    inputName.setError(getString(R.string.short_name));
                    inputName.requestFocus();
                    return;
                }

                if (temp.length() > 20) {
                    inputName.setError(getString(R.string.long_name));
                    inputName.requestFocus();
                    return;
                }

                final String profile = temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
                final String username = inputUsername.getText().toString().trim();

                if (username.length() < 3 && username.length() != 0) {
                    inputUsername.setError(getString(R.string.short_username));
                    inputUsername.requestFocus();
                    return;
                }

                if (username.length() > 20) {
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

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Users").orderByChild("username").equalTo("@" +username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v("tag", "" + dataSnapshot);
                        if (dataSnapshot.exists()) {
                            // use "username" already exists
                            inputUsername.setError(getString(R.string.username_exists));
                            inputUsername.requestFocus();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);

                            DatabaseReference databaseReference;
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(PersonalActivity.this);
                            if (acct != null) {
                                String personId = acct.getId();

                                if (personId != null) {
                                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(personId);
                                } else {
                                    Toast.makeText(PersonalActivity.this, "Unable to get google profile ID", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(PersonalActivity.this, LoginActivity.class));
                                    return;
                                }
                            } else {
                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                databaseReference = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Users")
                                        .child(currentFirebaseUser.getUid());
                            }

                            if (TextUtils.isEmpty(profile)) {
                                databaseReference.child("name").setValue("Anonymous");
                            } else {
                                databaseReference.child("name").setValue(profile);
                            }

                            if (TextUtils.isEmpty(username)) {
                                databaseReference.child("username").setValue(null);
                            } else {
                                databaseReference.child("username").setValue("@" + username);
                            }

                            startActivity(new Intent(PersonalActivity.this, MainActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PersonalActivity.this , "cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
