package com.example.chatdraw.activities;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.R;
import com.example.chatdraw.adapters.GroupListRecyclerViewAdapter;
import com.example.chatdraw.adapters.RecyclerViewAdapter;
import com.example.chatdraw.items.FriendListItem;
import com.example.chatdraw.items.NewFriendItem;
import com.example.chatdraw.listeners.RecyclerViewClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendListActivity extends AppCompatActivity
    implements RecyclerViewClickListener, Serializable {

  private static String TAG = "FriendListActivity";

  private static final int FIND_FRIEND_REQUEST_CODE = 101;

  private RecyclerView recyclerView;
  private RecyclerView.LayoutManager layoutManager;
  private ArrayList<FriendListItem> friendList;
  private String currentUserID;

  // adapters for RecyclerView
  private RecyclerViewAdapter friendsAdapter;
  private GroupListRecyclerViewAdapter groupsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friend_list);

    // set the RecyclerView
    recyclerView = findViewById(R.id.friend_list_recycler_view);

    // use this setting to improve performance if changes in content do not change
    // the layout size of the RecyclerView
    recyclerView.setHasFixedSize(true);

    // use a linear layout manager
    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);

    // specify an adapter
    friendList = new ArrayList<>();
    friendsAdapter = new RecyclerViewAdapter(friendList, this, (v, position) -> {
      // do nothing
    });
    recyclerView.setAdapter(friendsAdapter);

    // get the current user's uID
    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(FriendListActivity.this);
    if (acct != null) {
      currentUserID = acct.getId();
    } else {
      FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
      if (fbUser != null) {
        currentUserID = fbUser.getUid();
      } else {
        return;
      }
    }

    // set the "add" button to go to the FindFriendActivity
    ImageView imageView = findViewById(R.id.add_friend_imageview);
    imageView.setOnClickListener(v -> {
      Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
      startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
    });

    // set the Action Bar title
    Toolbar myToolbar = findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Contacts");
    }

    // get Contacts list
    getContacts();
    getGroups();

    // set toggle switch
    Button groupsButton = findViewById(R.id.friend_list_groups_button);
    Button friendsButton = findViewById(R.id.friend_list_friends_button);
    groupsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
    friendsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
    groupsButton.setOnClickListener(x -> {
      recyclerView.setAdapter(groupsAdapter);
      groupsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
      friendsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
    });
    friendsButton.setOnClickListener(x -> {
      recyclerView.setAdapter(friendsAdapter);
      groupsButton.setBackgroundColor(getResources().getColor(R.color.bluegray100));
      friendsButton.setBackgroundColor(getResources().getColor(R.color.secondary));
    });
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FIND_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

      if (data == null) {
        return;
      }

      final String uID = data.getStringExtra("uID");
      recyclerView.setAdapter(friendsAdapter);

      final FirebaseFirestore db = FirebaseFirestore.getInstance();
      db.collection("Users")
          .document(currentUserID)
          .update("contacts", FieldValue.arrayUnion(uID))
          .addOnSuccessListener(a -> Toast.makeText(FriendListActivity.this,
              "Contact added successfully.",
              Toast.LENGTH_SHORT).show())
          .addOnFailureListener(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("contacts", new ArrayList<String>());
            map.put("groups", new ArrayList<String>());
            db.collection("Users").document(currentUserID).set(map);
            db.collection("Users")
                .document(currentUserID)
                .update("contacts", FieldValue.arrayUnion(uID))
                .addOnSuccessListener(a -> Toast.makeText(FriendListActivity.this,
                    "Contact added successfully.",
                    Toast.LENGTH_SHORT).show());
          });
      addUserWithID(uID);
    }
  }

  private void addUserWithID(final String uid) {
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");

    databaseRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String name = (String) dataSnapshot.child(uid).child("name").getValue();
        String status = (String) dataSnapshot.child(uid).child("status").getValue();
        String imageUrl = (String) dataSnapshot.child(uid).child("imageUrl").getValue();

        // check if the user doesn't have name/status/imageUrl
        if (status == null) {
          status = "";
        }

        FriendListItem newFriend = new FriendListItem(name, status, uid, imageUrl);
        friendsAdapter.addItem(newFriend);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  /**
   * Fetches the current user's contacts from Firestore.
   */
  public void getContactsFromFirestore() {
    Log.d(TAG, "Getting contacts from firestore");
    final FirebaseFirestore db  = FirebaseFirestore.getInstance();
    db.enableNetwork();
    FirebaseDatabase.getInstance().goOnline();
    db.collection("Users")
        .document(currentUserID)
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
   * Fetches the current user's contacts from the device's cache.
   */
  public void getContacts() {
    Log.d(TAG, "Getting contacts from cache");
    final FirebaseFirestore db  = FirebaseFirestore.getInstance();
    db.disableNetwork();
    FirebaseDatabase.getInstance().goOffline();
    db.collection("Users")
        .document(currentUserID)
        .get()
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            ArrayList<String> arr = (ArrayList<String>) task.getResult().get("contacts");
            if (arr != null) {
              for (String s: arr) {
                addUserWithID(s);
              }
            }
            db.enableNetwork();
          } else {
            getContactsFromFirestore();
          }
        });
  }

  /**
   * Fetches the current user's groups from local cache.
   */
  public void getGroups() {
    Log.d(TAG, "Getting groups from cache");

    // specify an adapter
    ArrayList<NewFriendItem> myDataset = new ArrayList<>();
    final GroupListRecyclerViewAdapter adapter
        = new GroupListRecyclerViewAdapter(myDataset, FriendListActivity.this, this);
    groupsAdapter = adapter;

    final FirebaseFirestore db  = FirebaseFirestore.getInstance();
    db.disableNetwork();
    FirebaseDatabase.getInstance().goOffline();

    FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
        .get()
        .addOnCompleteListener(task -> {
          ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
          if (arr != null && !arr.isEmpty()) {
            for (String s: arr) {
              final String groupID = s;
              FirebaseFirestore.getInstance().collection("Groups").document(groupID)
                  .get()
                  .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                      DocumentSnapshot doc = task1.getResult();
                      if (doc == null) {
                        return;
                      }

                      String name = (String) doc.get("groupName");
                      String imageUrl = (String) doc.get("groupImageUrl");

                      NewFriendItem newFriendItem = new NewFriendItem(name, groupID, imageUrl);
                      adapter.addData(newFriendItem);
                    } else {
                      getGroupsFromFirestore();
                    }
                  });

            }
          }
        });
  }

  /**
   * Fetches the current user's groups from Firestore.
   */
  public void getGroupsFromFirestore() {
    Log.d(TAG, "Getting groups from firestore");

    final FirebaseFirestore db  = FirebaseFirestore.getInstance();
    db.enableNetwork();
    FirebaseDatabase.getInstance().goOnline();

    FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
        .get()
        .addOnCompleteListener(task -> {
          ArrayList<String> arr = (ArrayList<String>) task.getResult().get("groups");
          if (arr != null && !arr.isEmpty()) {
            for (String s: arr) {
              final String groupID = s;
              FirebaseFirestore.getInstance().collection("Groups").document(groupID)
                  .get()
                  .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                      DocumentSnapshot doc = task1.getResult();
                      if (doc == null) {
                        return;
                      }

                      String name = (String) doc.get("groupName");
                      String imageUrl = (String) doc.get("groupImageUrl");

                      NewFriendItem newFriendItem = new NewFriendItem(name, groupID, imageUrl);
                      groupsAdapter.addData(newFriendItem);
                    }
                  });
            }
          }
        });
  }

  @Override
  public void recyclerViewListClicked(View v, int position){

  }
}
