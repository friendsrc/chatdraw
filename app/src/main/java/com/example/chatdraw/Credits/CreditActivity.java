package com.example.chatdraw.Credits;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.ProfileEditActivity;
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
                setTopUpAction();
            }
        });

    }

    private void setTopUpAction() {
        final CharSequence[] items={"Paypal", "Visa", "Transfer", "Watch an ads", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreditActivity.this);
        builder.setTitle("Top up credit by");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Paypal")) {

                } else if (items[i].equals("Visa")) {

                } else if (items[i].equals("Transfer")) {

                } else if (items[i].equals("Watch an ads")) {

                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
