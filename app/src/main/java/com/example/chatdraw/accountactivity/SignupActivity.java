package com.example.chatdraw.accountactivity;

import com.example.chatdraw.R;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* Get Firebase auth instance
           Returns an instance of this class corresponding
           to the default FirebaseApp instance.
        */
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.sign_in_button);
        btnSignUp = findViewById(R.id.sign_up_button);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        // reset button pressed
        btnResetPassword.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class)));

        // redirect back to sign in page
        btnSignIn.setOnClickListener(v -> finish());

        // sign up button clicked
        btnSignUp.setOnClickListener(v -> {
            // deleting whitespace at both ends
            final String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // text utility to check whether the email is empty -> if (email.equals(""))
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() < 8) {
                inputEmail.setError(getString(R.string.invalid_email));
                inputEmail.requestFocus();
                return;
            }

            // text utility to check whether the password is empty -> if (password.equals(""))
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                inputPassword.setError(getString(R.string.invalid_password));
                inputPassword.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            //create user to firebase
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        // Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        /* If sign in fails, display a message to the user. If sign in succeeds
                           the auth state listener will be notified and logic to handle the
                           signed in user can be handled in the listener.
                        */
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String name = null;
                            String username = null;
                            User user = new User(email, name, username, 80);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Congratulations, you get 80 CTD", Toast.LENGTH_SHORT).show();
                                    // startActivity(new Intent(SignupActivity.this, FriendListActivity.class));
                                    startActivity(new Intent(SignupActivity.this, PersonalActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignupActivity.this, "error at signup activity", Toast.LENGTH_SHORT).show();
                                }
                            });

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Map<String, Object> map = new HashMap<>();
                            map.put("contacts", new ArrayList<String>());
                            map.put("groups", new ArrayList<String>());
                            db.collection("Users").document(userUID).set(map);
                        }
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}