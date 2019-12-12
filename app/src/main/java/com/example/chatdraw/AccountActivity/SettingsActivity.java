package com.example.chatdraw.AccountActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {
    private Button btnChangePassword, btnRemoveUser,
            changePassword, signOut;
    private TextView email;

    private EditText password, newPassword;
    private ProgressBar progressBar;
    private FirebaseUser user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        super.onStart();

        /* Get Firebase auth instance
           Returns an instance of this class corresponding
           to the default FirebaseApp instance.
        */
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.useremail);

        btnChangePassword = findViewById(R.id.change_password_button);
        btnRemoveUser = findViewById(R.id.remove_user_button);

        changePassword = findViewById(R.id.changePass);
        signOut = findViewById(R.id.sign_out);

        newPassword = findViewById(R.id.newPassword);
        password = findViewById(R.id.password);

        user = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SettingsActivity.this);

        if (acct != null) {
            btnChangePassword.setVisibility(View.GONE);
            btnRemoveUser.setVisibility(View.GONE);

            DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
            final String personId = acct.getId();

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String tempEmail = (String) dataSnapshot.child(personId).child("email").getValue();
                    email.setText(tempEmail);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (user != null) {
            email.setText(user.getEmail());
        } else {
            Toast.makeText(this, "Error in showing user email", Toast.LENGTH_SHORT).show();
        }

        newPassword.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnChangePassword.setOnClickListener(v -> {
            newPassword.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            newPassword.setFocusable(true);
            changePassword.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.GONE);
        });

        // change password button clicked do comparison check of password
        changePassword.setOnClickListener(v -> {
        String newpass = newPassword.getText().toString().trim();
        String confirmpass = password.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        if (newpass.equals("")) {
            newPassword.setError("Enter password");
            progressBar.setVisibility(View.GONE);
        } else if (confirmpass.equals("")) {
            password.setError("Re-enter password");
            progressBar.setVisibility(View.GONE);
        } else if (user != null) {
            if (!newpass.equals(confirmpass)) {
                password.setError("Password not match");
                progressBar.setVisibility(View.GONE);
            } else if (newPassword.getText().toString().trim().length() < 6) {
                newPassword.setError("Password too short, enter minimum 6 characters");
                progressBar.setVisibility(View.GONE);
            } else {
                user.updatePassword(newPassword.getText().toString().trim())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                            signOut();

                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            }
        }
        });

        // when button remove user clicked, delete account
        btnRemoveUser.setOnClickListener(v -> new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Confirm")
                .setMessage("Do you really want to delete your account?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    if (user != null) {
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Your profile is deleted. Create an account now!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(SettingsActivity.this, SignupActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Failed to delete your account! Please re-open the app!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, null).show());

        // set the action bar title
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Privacy and Security");

        // add a back button to the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // delete and signout
        signOut.setOnClickListener(v -> signOut());
    }

    // this listener will be called when there is change in firebase user session
    FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SettingsActivity.this);

            if (acct != null) {
                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
                final String personId = acct.getId();

                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String tempEmail = (String) dataSnapshot.child(personId).child("email").getValue();
                        email.setText(tempEmail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                if (user != null) {
                    email.setText(user.getEmail());
                } else {
                    // user auth state is changed - user is null
                    // launch login activity
                    Toast.makeText(SettingsActivity.this, "Error2", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
    };

    //sign out method
    public void signOut() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SettingsActivity.this);

        if (acct != null) {
            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestId()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, task -> {
                        Toast.makeText(SettingsActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
        } else {
            auth.signOut();

            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

            // this listener will be called when there is change in firebase user session
//            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
//                @Override
//                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                    FirebaseUser user = firebaseAuth.getCurrentUser();
//                    if (user == null) {
//                        // user auth state is changed - user is null
//                        // launch login activity
//                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
//                            finish();
//                    }
//                }
//            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
