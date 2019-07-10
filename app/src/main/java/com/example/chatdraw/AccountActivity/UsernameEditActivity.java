package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;

public class UsernameEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_edit);

        final EditText inputUsername = (EditText) findViewById(R.id.input_username);
        Button username_butt = (Button) findViewById(R.id.username_submit);
        username_butt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final String username = inputUsername.getText().toString().trim();

                if (username.length() < 3) {
                    inputUsername.setError(getString(R.string.short_username));
                    inputUsername.requestFocus();
                    return;
                }

                if (username.length() > 20) {
                    inputUsername.setError(getString(R.string.long_username));
                    inputUsername.requestFocus();
                    return;
                }

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
                            // "username" does not exist yet.
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(UsernameEditActivity.this);

                            if (acct != null) {
                                String personId = acct.getId();

                                if (updateUser(personId, username)) {
                                    finish();
                                } else {
                                    Toast.makeText(UsernameEditActivity.this, "Username update failed", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentFirebaseUser != null) {
                                    if (updateUser(currentFirebaseUser.getUid(), username)) {
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(UsernameEditActivity.this, "Username update failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UsernameEditActivity.this , "cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private boolean updateUser(String Uid, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Uid).child("username");
        databaseReference.setValue("@" + username);

        FirebaseFirestore.getInstance().collection("Users").document(Uid).update("username", "@" + username);


        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show();

        return true;
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
