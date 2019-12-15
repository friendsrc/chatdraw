package com.example.chatdraw.Activities;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
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
        newButton.setOnClickListener(v -> {
            // create Intent to send selected friend's name back to FriendListActivity
            final Intent intent = new Intent();

            // get new friend's uID, add to intent, and then destroy this activity
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("Users")
                    .orderByChild("username")
                    .equalTo(newUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                    final String uID = userSnapshot.getKey();
                                    final String currentUserID = getCurrentUid();

                                    if (currentUserID.equals("")) {
                                        Toast.makeText(FindFriendActivity.this,
                                            "User is not validated!",
                                            Toast.LENGTH_SHORT).show();
                                    } else {
                                        FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(currentUserID)
                                                .get()
                                                .addOnCompleteListener(task -> {
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
                                                    } else if (uID.equals(currentUserID)) {
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
                                                });
                                    }
                                }
                            } else {
                                Toast.makeText(FindFriendActivity.this,
                                    "No record found!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        });

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Find friends");
        }

        final EditText editText = findViewById(R.id.find_friend_edittext);
        ImageView searchButton = findViewById(R.id.find_friend_search_button);
        searchButton.setOnClickListener(v -> {
            String str = editText.getText().toString();
            if (!str.trim().equals("")) {
                // find user and update list view
                findUserInDatabase(str.trim());
            }
        });
    }

    public String getCurrentUid() {
        final String currentUserID;
        GoogleSignInAccount acct =
            GoogleSignIn.getLastSignedInAccount(FindFriendActivity.this);

        if (acct != null) {
            currentUserID = acct.getId();
            return currentUserID;
        } else {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                currentUserID = fbUser.getUid();
                return currentUserID;
            } else {
                Toast.makeText(FindFriendActivity.this, "User is not validated!",
                    Toast.LENGTH_SHORT).show();
                return "";
            }
        }
    }

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Users")
                .orderByChild("username")
                .equalTo(text)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        InputMethodManager imm =
                            (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        LinearLayout myLayout = findViewById(R.id.find_friend_layout);

                        if (!dataSnapshot.exists()) {
                            newImageView.setVisibility(View.INVISIBLE);
                            newTextView.setVisibility(View.INVISIBLE);
                            newButton.setVisibility(View.INVISIBLE);

                            // close the user keyboard
                            try {
                                imm.hideSoftInputFromWindow(myLayout.getWindowToken(), 0);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }

                            Toast.makeText(FindFriendActivity.this,
                                    "User not found", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DataSnapshot o : dataSnapshot.getChildren()) {
                                User addedUser = o.getValue(User.class);

                                String name = addedUser.getName();
                                newUsername = addedUser.getUsername();
                                String imgUrl = addedUser.getImageUrl();

                                if (name == null) {
                                    name = "Anonymous";
                                    newTextView
                                        .setTextColor(getResources().getColor(R.color.pLight));
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

                                // close the user keyboard
                                try {
                                    imm.hideSoftInputFromWindow(myLayout.getWindowToken(), 0);
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }

                                newImageView.setVisibility(View.VISIBLE);
                                newTextView.setVisibility(View.VISIBLE);
                                newButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
