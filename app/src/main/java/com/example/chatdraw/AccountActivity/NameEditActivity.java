package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatdraw.R;
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
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentFirebaseUser != null) {
                    if (updateUser(currentFirebaseUser.getUid(), profile)) {
                        finish();
                    }
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Settings");
        }
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

