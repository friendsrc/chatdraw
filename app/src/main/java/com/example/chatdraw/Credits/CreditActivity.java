package com.example.chatdraw.Credits;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.LoginActivity;
import com.example.chatdraw.AccountActivity.PersonalActivity;
import com.example.chatdraw.AccountActivity.SettingsActivity;
import com.example.chatdraw.AccountActivity.SignupActivity;
import com.example.chatdraw.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreditActivity extends AppCompatActivity implements RewardedVideoAdListener {
    RewardedVideoAd mAd;
    Button ads_button;
    TextView credit_balance;
    String personId;
    private boolean notChangedYet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        credit_balance = (TextView) findViewById(R.id.credit_balance);
        Button history_button = (Button) findViewById(R.id.history_button);
        ads_button = (Button) findViewById(R.id.ads_button);

        ads_button.setEnabled(false);

        history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreditActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });

        MobileAds.initialize(this, "ca-app-pub-6617407029399736~9775527525");
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        mAd.loadAd("ca-app-pub-6617407029399736/1425306353", new AdRequest.Builder().build());

        ads_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAd.isLoaded()) {
                    mAd.show();
                } else {
                    Toast.makeText(CreditActivity.this, "No ads available currently", Toast.LENGTH_SHORT).show();
                }
            }
        });

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(CreditActivity.this);
                String personId = "";

                if (acct != null) {
                    personId = acct.getId();
                } else {
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentFirebaseUser != null) {
                        personId = currentFirebaseUser.getUid();
                    } else {
                        Toast.makeText(CreditActivity.this, "User is not validated", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(CreditActivity.this, SignupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                Long currentCredits = (Long) dataSnapshot.child(personId).child("credits").getValue();
                credit_balance.setText("" + currentCredits);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Credits");
        }


        // uncomment below to add dummy data
//        TransactionItem translog = new TransactionItem("Sign up", "#019253");
//        db.collection("Messages")
//                .document(userID)
//                .collection("History")
//                .add(translog);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        ads_button.setEnabled(true);
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        // Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        // Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        DatabaseReference DatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        String current_balance = credit_balance.getText().toString();
        Toast.makeText(this, "Money: " + current_balance, Toast.LENGTH_SHORT).show();
        DatabaseRef.child(personId).child("credits").setValue(Integer.parseInt(current_balance));

        ads_button.setEnabled(false);
        mAd.loadAd("ca-app-pub-6617407029399736/1425306353", new AdRequest.Builder().build());
    }

    // user get reward
    @Override
    public void onRewarded(RewardItem rewardItem) {
        notChangedYet = true;

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(CreditActivity.this);

        if (acct != null) {
            personId = acct.getId();
        } else {
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentFirebaseUser != null) {
                personId = currentFirebaseUser.getUid();
            } else {
                Toast.makeText(CreditActivity.this, "User is not validated", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreditActivity.this, SignupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

        final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (notChangedYet) {
                    Long currentCredits = (Long) dataSnapshot.child(personId).child("credits").getValue();

                    credit_balance.setText((currentCredits + 3) + "");
                    notChangedYet = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toast.makeText(this, "You get " + rewardItem.getAmount() + " CTD", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        // Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // destroy the activity
        finish();
        return true;
    }
}
