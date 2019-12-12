package com.example.chatdraw.Credits;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

import com.example.chatdraw.Adapters.CreditRecyclerViewAdapter;
import com.example.chatdraw.Items.TransactionItem;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import javax.annotation.Nullable;

public class TransactionActivity extends AppCompatActivity {
    private CreditRecyclerViewAdapter adapter;
    private LinkedList<TransactionItem> exampleList;
    private String userID;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Transaction history");
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        exampleList = new LinkedList<>();
        adapter = new CreditRecyclerViewAdapter(exampleList);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(TransactionActivity.this);

        if (acct != null) {
            userID = acct.getId();
        } else {
            //get the signed in user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userID = user.getUid();
            } else {
                return;
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Transactions")
                .document(userID)
                .collection("History")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(25)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    // remove previous data
                    adapter.clearData();
                    for (DocumentSnapshot q: queryDocumentSnapshots) {
                        TransactionItem transactionItem = q.toObject(TransactionItem.class);
                        Log.v("HEREEE", "" + transactionItem);

                        exampleList.addFirst(transactionItem);
                        recyclerView.scrollToPosition(adapter.getItemCount() + 1);
                    }

                    adapter = new CreditRecyclerViewAdapter(exampleList);
                    recyclerView.setAdapter(adapter);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = findViewById(R.id.action_search_view);

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

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }
}
