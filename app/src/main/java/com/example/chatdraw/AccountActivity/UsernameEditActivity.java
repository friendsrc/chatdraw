package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                            Toast.makeText(UsernameEditActivity.this , "username exists", Toast.LENGTH_SHORT).show();
                        } else {
                            // "username" does not exist yet.
                            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentFirebaseUser != null) {
                                if (updateUser(currentFirebaseUser.getUid(), username)) {
                                    finish();
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Settings");
        }
    }

    private boolean updateUser(String Uid, String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Uid).child("username");
        databaseReference.setValue("@" + username);

        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
