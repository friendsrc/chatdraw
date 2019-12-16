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
import com.example.chatdraw.Activities.VerifyPatternActivity;
import com.example.chatdraw.Config.GlobalStorage;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private String save_pattern_key = "pattern_code";
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

        signIn = findViewById(R.id.bn_login);
        signIn.setOnClickListener(view -> signInMethod());

        /* Get Firebase auth instance
           Returns an instance of this class corresponding
           to the default FirebaseApp instance.
        */
        auth = FirebaseAuth.getInstance();


        // If there is user login already, start the MainActivity
        if (auth.getCurrentUser() != null) {
            Paper.init(this);
            String save_pattern = Paper.book().read(save_pattern_key);
            if (save_pattern != null && !save_pattern.equals("null")) {
                startActivity(new Intent(LoginActivity.this, VerifyPatternActivity.class));
                finish();
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        } else {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
            if (acct != null) {
                Paper.init(this);
                String save_pattern = Paper.book().read(save_pattern_key);
                if (save_pattern != null && !save_pattern.equals("null")) {
                    startActivity(new Intent(LoginActivity.this, VerifyPatternActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        }

        // setContentView(R.layout.activity_login);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);

        //Get Firebase auth instance
        // auth = FirebaseAuth.getInstance();

        // Signup button clicked
        btnSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // Reset button clicked
        btnReset.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class)));

        // Login button clicked
        btnLogin.setOnClickListener(v -> {
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
                    .addOnCompleteListener(LoginActivity.this, task -> {
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
                    });
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
                final String userID = account.getId();
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
                                user = new User(email, name, username, img_url.toString(), GlobalStorage.welcomeDescription);
                            } else {
                                user = new User(email, name, username, GlobalStorage.welcomeDescription);
                            }

                            // edit: add to firestore
                            final FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Users")
                                    .document(userID)
                                    .get()
                                    .addOnFailureListener(e -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("contacts", new ArrayList<String>());
                                        map.put("groups", new ArrayList<String>());
                                        db.collection("Users").document(userID).set(map);
                                    });

                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference
                                    .child(personId)
                                    .setValue(user).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // startActivity(new Intent(SignupActivity.this, FriendListActivity.class));
                                            Intent intention = new Intent(LoginActivity.this, PersonalActivity.class);
                                            intention.putExtra("GoogleName", name);
                                            intention.putExtra("userID", personId);
                                            startActivity(intention);
                                        } else {
                                            Toast.makeText(LoginActivity.this, "error at signup through google", Toast.LENGTH_SHORT).show();
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