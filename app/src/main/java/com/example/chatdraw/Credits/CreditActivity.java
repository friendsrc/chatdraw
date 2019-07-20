package com.example.chatdraw.Credits;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatdraw.R;

public class CreditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        TextView credit_balance = (TextView) findViewById(R.id.credit_balance);
        Button top_up_button = (Button) findViewById(R.id.top_up_button);

        top_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreditActivity.this, TransactionActivity.class);
                startActivity(intent);
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
}
