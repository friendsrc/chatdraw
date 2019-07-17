package com.example.chatdraw.Credits;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.chatdraw.Adapters.CreditRecyclerViewAdapter;
import com.example.chatdraw.Items.TransactionItem;
import com.example.chatdraw.R;

import java.util.ArrayList;
import java.util.List;

public class CreditActivity extends AppCompatActivity {
    private CreditRecyclerViewAdapter adapter;
    private ArrayList<TransactionItem> exampleList;

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

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Credits");
        }

        fillExampleList();
        setUpRecyclerView();
    }

    private void fillExampleList() {
        exampleList = new ArrayList<>();
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 1", "Line 2"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 3", "Line 4"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 5", "Line 6"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 7", "Line 8"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 9", "Line 10"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 11", "Line 12"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 13", "Line 14"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 15", "Line 16"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 17", "Line 18"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 19", "Line 20"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 21", "Line 22"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 23", "Line 24"));
        exampleList.add(new TransactionItem(R.drawable.add_person, "Line 25", "Line 26"));
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new CreditRecyclerViewAdapter(exampleList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) findViewById(R.id.action_search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private void setTopUpAction() {
        final CharSequence[] items={"Paypal", "Visa", "Transfer", "Watch an ad", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreditActivity.this);
        builder.setTitle("Top up credit by");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Paypal")) {

                } else if (items[i].equals("Visa")) {

                } else if (items[i].equals("Transfer")) {

                } else if (items[i].equals("Watch an ad")) {

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
