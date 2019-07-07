package com.example.chatdraw.AccountActivity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatdraw.Activities.MainActivity;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    private SignInButton signIn;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // request the user's ID, email address, and basic profile
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        // build API client with access to Sign-In API and options above
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
//                .addConnectionCallbacks(this)
//                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient= GoogleSignIn.getClient(this, signInOptions);

        signIn = (SignInButton) findViewById(R.id.bn_login);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInMethod();
            }
        });

        /* Get Firebase auth instance
           Returns an instance of this class corresponding
           to the default FirebaseApp instance.
        */
        auth = FirebaseAuth.getInstance();


        // If there is user login already, start the MainActivity
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));

            finish();
        } else {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
            if (acct != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                finish();
            }
        }

        // setContentView(R.layout.activity_login);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        // auth = FirebaseAuth.getInstance();

        // Signup button clicked
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        // Reset button clicked
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        // Login button clicked
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                // text utility to check whether the email is empty -> if (email.equals(""))
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // text utility to check whether the password is empty -> if (password.equals(""))
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user to firebase
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                    /* If sign in fails, display a message to the user. If sign in succeeds
                       the auth state listener will be notified and logic to handle the
                       signed in user can be handled in the listener.
                    */
                                progressBar.setVisibility(View.GONE);

                                // if everything has been input but didn't meet the criteria (email and password not match)
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    private void signInMethod(){
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleResult(task);
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleResult(result);
        }
    }

    private void handleResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                String userID = account.getId();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.orderByKey().equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Toast.makeText(LoginActivity.this, "" + dataSnapshot, Toast.LENGTH_SHORT).show();
                        if (dataSnapshot.exists()) {
                            // if a user from google sign in has signed in before
                            Intent intention = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intention);
                        } else {
                            final String name = account.getDisplayName();
                            String email = account.getEmail();
                            final String personId = account.getId();
                            final Uri img_url = account.getPhotoUrl();
                            String username = null;
                            User user;

                            if (img_url != null) {
                                user = new User(email, name, username, img_url.toString());
                            } else {
                                user = new User(email, name, username);
                            }

                            // edit: add to firestore
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(personId)
                                    .set(user);

                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference
                                    .child(personId)
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // startActivity(new Intent(SignupActivity.this, FriendListActivity.class));
                                        Intent intention = new Intent(LoginActivity.this, PersonalActivity.class);
                                        intention.putExtra("GoogleName", name);
                                        intention.putExtra("userID", personId);
                                        startActivity(intention);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "error at signup through google", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch(ApiException e) {
            Log.w("TAG", "signInResult:failed code=" + e);
            Toast.makeText(this, "Login fail", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("connection: ", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("connection: ","onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("connection: ","onConnectionFailed");
    }
}