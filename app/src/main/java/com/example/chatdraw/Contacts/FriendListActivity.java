package com.example.chatdraw.Contacts;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.Chat.ChatItem;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FriendListActivity extends AppCompatActivity {

    private static String TAG = "FriendListActivity";

    private static final int FIND_FRIEND_REQUEST_CODE = 101;
    public static final String FRIEND_LIST_KEY = "contacts_list";
    public static final String USER_ID_KEY = "user_id";

    private FriendListAdapter mFriendListAdapter;
    private List<FriendListItem> mFriendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Create a custom adapter for the friend list ListView
        final FriendListAdapter friendListAdapter = new FriendListAdapter(this);
        mFriendListAdapter = friendListAdapter;

        // Set the "add" button to go to the FindFriendActivity
        ImageView imageView = findViewById(R.id.add_friend_imageview);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(FriendListActivity.this, FindFriendActivity.class);
                startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE);
            }
        });

        // set the Action Bar title
        getSupportActionBar().setTitle("Contacts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // instantiating the friendList
        mFriendList = new ArrayList<>();

        // checked SharedPreferences for existing friendlist
        checkSavedMessages(mFriendListAdapter);
    }


    @Override
    // if the back button is pressed, save friendlist before destroying the activity
    public boolean onSupportNavigateUp() {
        // get SharedPreferences
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        // parse the FriendList into JSON string
        String friendlistJSONString = new Gson().toJson(mFriendList);

        // get user ID
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String id = currentFirebaseUser.getUid();


        // save the JSON string and user ID into SharedPreferences
        editor.putString(FRIEND_LIST_KEY, friendlistJSONString);
        editor.putString(USER_ID_KEY, id);
        editor.commit();

        // destroy the activity
        finish();

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FIND_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final String username = data.getStringExtra("username");
            FirebaseFirestore.getInstance().collection("Users").whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d(TAG, task.toString());
                            // get name and status from firestore
                            List<User> users = task.getResult().toObjects(User.class);
                            User newFriend = users.get(0);
                            String name = newFriend.getName();

                            // TODO get profile picture and status from firestore

                            // add the new contact to ListView
                            FriendListItem friendListItem = updateListView(mFriendListAdapter,
                                    name, "[status]", R.drawable.blank_account);
                            mFriendList.add(friendListItem);

                            // add the new contact to contact list in Firestore
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(uid)
                                    .update("contacts", FieldValue.arrayUnion(username));
                        }
                    });

        }
    }

    public FriendListItem updateListView(FriendListAdapter friendListAdapter, String name, String status, int imageID) {
        // find the friend list ListView
        ListView listView = findViewById(R.id.friend_list_listview);

        // Instantiate a new FriendListItem and add it to the custom adapter
        FriendListItem newFriend = new FriendListItem(name, status, imageID);
        friendListAdapter.addAdapterItem(newFriend);

        // set the adapter to the ListView
        listView.setAdapter(friendListAdapter);
        return newFriend;
    }

    public void checkSavedMessages(final FriendListAdapter friendListAdapter) {
        // get the Contacts listview and set the adapter
        ListView listView = findViewById(R.id.friend_list_listview);
        listView.setAdapter(friendListAdapter);

        // check if the contact in the SharedPreferences belong to the currently signed-in user
        String savedID = getPreferences(MODE_PRIVATE).getString(USER_ID_KEY, null);
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final ArrayList<FriendListItem> arrayList = new ArrayList<>();

        if ( !(savedID == null) && savedID.equals(currentUserID) ) {
            // if it does belong to this user, use the SharedPreferences contact list data to update
            // the listview

            // get friendList JSON String from SharedPreferences
            String friendlistJSONString
                    = getPreferences(MODE_PRIVATE).getString(FRIEND_LIST_KEY, null);

            // if nothing is saved yet, return;
            if (friendlistJSONString == null) return;

            try {
                // Parse the JsonArray into ArrayList and update the Contacts listview
                JSONArray jsonArray = new JSONArray(friendlistJSONString);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String status = jsonObject.getString("chatPreview");
                        int imageID = jsonObject.getInt("imageID");
                        String name = jsonObject.getString("name");
                        FriendListItem friendListItem = new FriendListItem(name, status, imageID);
                        friendListAdapter.addAdapterItem(friendListItem);
                        friendListAdapter.notifyDataSetChanged();
                        arrayList.add(friendListItem);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (false) {
            // TODO: get user's contacts ID list from Firestore
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String id = currentFirebaseUser.getUid();
            FirebaseFirestore.getInstance().collection("Users").document(id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            User newFriend = task.getResult().toObject(User.class);
                            String name = newFriend.getName();
                        }
                    });
        }
        mFriendList = arrayList;

    }
}
