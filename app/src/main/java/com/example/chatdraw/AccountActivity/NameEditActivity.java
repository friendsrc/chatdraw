package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NameEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_edit);

        final EditText inputName = (EditText) findViewById(R.id.input_name);
        Button name_butt = (Button) findViewById(R.id.name_submit);
        name_butt.setOnClickListener(new View.OnClickListener(){
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

                String profile = temp.substring(0, 1).toUpperCase() + temp.substring(1).toLowerCase();
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(NameEditActivity.this);

                if (acct != null) {
                    String personId = acct.getId();

                    if (updateUser(personId, profile)) {
                        finish();
                    } else {
                        Toast.makeText(NameEditActivity.this, "Name update failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentFirebaseUser != null) {
                        if (updateUser(currentFirebaseUser.getUid(), profile)) {
                            finish();
                        }
                    } else {
                        Toast.makeText(NameEditActivity.this, "Name update failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean updateUser(String Uid, String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Uid).child("name");
        databaseReference.setValue(name);

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

