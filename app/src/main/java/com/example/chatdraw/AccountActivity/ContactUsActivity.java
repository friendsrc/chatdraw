package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ContactUsActivity extends AppCompatActivity {
    Integer mSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Contact us");
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(ContactUsActivity.this);
        final String[] list = getResources().getStringArray(R.array.contact_question_code);

        final Button contact_button = findViewById(R.id.contact_click);
        Button submitButton = findViewById(R.id.contact_submit_button);

        final EditText tvQuestion = findViewById(R.id.question_section);
        final EditText tvMessage = findViewById(R.id.message_section);

        contact_button.setOnClickListener(view -> builder.create().show());

        builder.setTitle("Select your question code")
                .setSingleChoiceItems(list, mSelected, (dialogInterface, i) -> {
                    contact_button.setText(list[i]);
                    mSelected = i;
                    dialogInterface.dismiss();
                });

        submitButton.setOnClickListener(view -> {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(ContactUsActivity.this);

            String userID = "";

            if (acct != null) {
                userID = acct.getId();
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    userID = user.getUid();
                }
            }


            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"chatdrawfeedback@gmail.com"});

            if (mSelected == -1) {
                i.putExtra(Intent.EXTRA_SUBJECT, "" + tvQuestion.getText());
            } else {
                i.putExtra(Intent.EXTRA_SUBJECT, "[" + list[mSelected] + "] " + tvQuestion.getText());
            }
            //message is your details
            i.putExtra(Intent.EXTRA_TEXT, "Dear " + userID + ",\n\n" + tvMessage.getText());

            try {
                startActivity(Intent.createChooser(i, "Send email using..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ContactUsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
