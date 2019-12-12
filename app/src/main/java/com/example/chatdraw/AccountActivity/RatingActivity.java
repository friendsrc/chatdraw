package com.example.chatdraw.AccountActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        final TextView ratingThankyou = findViewById(R.id.rating_thankyou);
        final TextView commentHeader = findViewById(R.id.comment_header);
        final EditText commentSection = findViewById(R.id.comment_section);
        final Button submitButton = findViewById(R.id.submit_button);

        ratingThankyou.setVisibility(View.GONE);
        commentHeader.setVisibility(View.GONE);
        commentSection.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Help and Support");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener((ratingBar12, rating, b) -> {
            if (rating >= 4) {
                ratingThankyou.setVisibility(View.VISIBLE);
                commentHeader.setVisibility(View.GONE);
                commentSection.setVisibility(View.GONE);
                submitButton.setVisibility(View.GONE);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.example.chatdraw&reviewId=0"));
                startActivity(intent);
            } else {
                ratingThankyou.setVisibility(View.GONE);
                commentHeader.setVisibility(View.VISIBLE);
                commentSection.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
            }
        });

        submitButton.setOnClickListener(view -> {
            RatingBar ratingBar1 = (RatingBar) findViewById(R.id.rating_bar);
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(RatingActivity.this);

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
            i.putExtra(Intent.EXTRA_SUBJECT, "Feedback id: " + userID);

            //message is your details
            i.putExtra(Intent.EXTRA_TEXT, "I give you " + (int) ratingBar1.getRating() + " stars because: \n\n" + commentSection.getText());

            try {
                startActivity(Intent.createChooser(i, "Send email using..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(RatingActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });
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
