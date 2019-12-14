package com.example.chatdraw.activities;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.R;
import com.example.chatdraw.adapters.RecyclerViewAdapter;
import com.example.chatdraw.items.FriendListItem;
import com.example.chatdraw.listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity implements RecyclerViewClickListener {

  private static final int NEW_GROUP_REQUEST_CODE = 505;


  public static final String TAG = "NewMessageActivity";
  private RecyclerViewAdapter adapter;
  private ArrayList<FriendListItem> myDataset;
  private String userUid;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_message);

    RecyclerView recyclerView = findViewById(R.id.new_message_recycler_view);

    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    // specify an adapter
    myDataset = new ArrayList<>();
    adapter = new RecyclerViewAdapter(myDataset, NewMessageActivity.this, this);
    recyclerView.setAdapter(adapter);

    // get Contacts list
    String id;
    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(NewMessageActivity.this);
    if (acct != null) {
      id = acct.getId();
    } else {
      id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    userUid = id;

    getContacts();


    LinearLayout linearLayout = findViewById(R.id.new_group_chat_linearlayout);
    linearLayout.setOnClickListener(v -> {
      Intent intent = new Intent(NewMessageActivity.this, NewGroupActivity.class);
      startActivityForResult(intent, NEW_GROUP_REQUEST_CODE);
    });

    // set the action bar
    Toolbar myToolbar = findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle("New Message");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

  private void addUserWithID(final String uid) {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");

    databaseRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String name = (String) dataSnapshot.child(uid).child("name").getValue();
        String status = (String) dataSnapshot.child(uid).child("status").getValue();
        String imageUrl = (String) dataSnapshot.child(uid).child("imageUrl").getValue();

        if (status == null) {
          status = "";
        }

        FriendListItem newFriend = new FriendListItem(name, status, uid, imageUrl);
        adapter.addItem(newFriend);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  @Override
  public void recyclerViewListClicked(View v, int position) {
    final FriendListItem friendListItem = adapter.getItem(position);

    FirebaseFirestore.getInstance().collection("Previews").document(userUid)
        .collection("ChatPreviews")
        .document(friendListItem.getUID())
        .get()
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
              Toast.makeText(NewMessageActivity.this,
                  "Chat already exists.", Toast.LENGTH_SHORT).show();
            } else {
              Intent intent = new Intent();
              intent.putExtra("uID", friendListItem.getUID());
              setResult(Activity.RESULT_OK, intent);
              finish();
            }
          } else {
            Log.d(TAG, "Failed with: ", task.getException());
          }
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == NEW_GROUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      setResult(55, data);
      finish();
    }
  }

  /**
   * Gets the current user's contacts from Firestore.
   */
  public void getContactsFromFirestore() {
    Log.d(TAG, "Getting contacts from firestore");
    final FirebaseFirestore db  = FirebaseFirestore.getInstance();
    db.enableNetwork();
    FirebaseDatabase.getInstance().goOnline();
    db.collection("Users")
        .document(userUid)
        .get()
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
            if (arr != null) {
              for (String s: arr) {
                addUserWithID(s);
              }
            }
          }
        });
  }

  /**
   * Gets the current user's contacts from local cache.
   */
  public void getContacts() {
    Log.d(TAG, "Getting contacts from cache");
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.disableNetwork();
    db.collection("Users")
        .document(userUid)
        .get()
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
            if (arr != null) {
              for (String s : arr) {
                addUserWithID(s);
              }
            }
            db.enableNetwork();
          } else {
            getContactsFromFirestore();
          }
        });
  }
}
