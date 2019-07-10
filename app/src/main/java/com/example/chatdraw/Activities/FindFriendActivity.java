package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.Adapters.NewFriendAdapter;
import com.example.chatdraw.R;
import com.example.chatdraw.Items.NewFriendItem;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity {

    private static String TAG = "FindFriendActivity";

    private ImageView newImageView;
    private TextView newTextView;
    private Button newButton;
    private String newUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        // get the views
        newImageView = findViewById(R.id.find_friend_newImageView);
        newTextView = findViewById(R.id.find_friend_newTextView);
        newButton = findViewById(R.id.find_friend_newButton);

        // set as invisible
        newImageView.setVisibility(View.INVISIBLE);
        newTextView.setVisibility(View.INVISIBLE);
        newButton.setVisibility(View.INVISIBLE);

        // set onClickListener on the button
        // if clicked, go back to FriendListActivity
        // put extra containing the name of the clicked profile
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create Intent to send selected friend's name back to FriendListActivity
                final Intent intent = new Intent();

                // get new friend's uID, add to intent, and then destroy this activity
                FirebaseFirestore.getInstance().collection("Users")
                        .whereEqualTo("username", newUsername)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    String id;
                                    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FindFriendActivity.this);
                                    if (acct != null) {
                                        id = acct.getId();
                                    } else {
                                        id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    }
                                    final String currentUserID = id;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        final String uID = document.getId();
                                        FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(currentUserID)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        ArrayList<String> contacts
                                                                = (ArrayList<String>)
                                                                task.getResult().get("contacts");
                                                        // if the chosen contact already exist in
                                                        // this user's contacts list, make a toast
                                                        if (contacts != null && contacts.contains(uID)) {
                                                            Toast.makeText(
                                                                    FindFriendActivity.this,
                                                                    "Already in Contacts",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        } else  if (uID.equals(currentUserID)) {
                                                            Toast.makeText(
                                                                    FindFriendActivity.this,
                                                                    "Can't add your own account",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        } else {
                                                            intent.putExtra("uID", uID);

                                                            // set the result as successful
                                                            setResult(Activity.RESULT_OK, intent);

                                                            // destroy this activity
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editText = findViewById(R.id.find_friend_edittext);
        ImageView searchButton = findViewById(R.id.find_friend_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                if (!str.trim().equals("")) {
                    // find user and update listview
                    findUserInDatabase(str.trim());
                }
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void findUserInDatabase(String inputText) {
        // Check if the input start with @
        final String text;
        if (inputText.charAt(0) != '@') {
            text = "@" + inputText;
        }  else {
            text = inputText;
        }

        if (text.length() < 2) return;

        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("username", text)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                String name = document.getString("name");
                                newUsername = document.getString("username");
                                String imgUrl = document.getString("imageUrl");

                                if (name == null) {
                                    name = "Anonymous";
                                    newTextView.setTextColor(getResources().getColor(R.color.pLight));
                                }
                                newTextView.setText(name);
                                if (imgUrl != null) {
                                    Picasso.get()
                                            .load(imgUrl)
                                            .fit()
                                            .into(newImageView);
                                } else {
                                    newImageView.setImageResource(R.drawable.blank_account);
                                }

                                newImageView.setVisibility(View.VISIBLE);
                                newTextView.setVisibility(View.VISIBLE);
                                newButton.setVisibility(View.VISIBLE);

                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
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
