package com.example.chatdraw.Credits;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class CreditActivity extends AppCompatActivity implements RewardedVideoAdListener {
    RewardedVideoAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        TextView credit_balance = (TextView) findViewById(R.id.credit_balance);
        Button history_button = (Button) findViewById(R.id.history_button);
        Button ads_button = (Button) findViewById(R.id.ads_button);

        history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreditActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());

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
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    // user get reward
    @Override
    public void onRewarded(RewardItem rewardItem) {
        Toast.makeText(this, "You get 3 CTD", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }
}
