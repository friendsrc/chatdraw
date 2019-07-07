package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatdraw.Items.ChatItem;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        // get the added group members' IDs
        final String[] memberUIDs = getIntent().getStringArrayExtra("memberList");

        final ArrayList<String> members = new ArrayList<>();
        for (String s : memberUIDs) members.add(s);

        // get this user's ID and add to array
        final String userUID;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(GroupCreateActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        members.add(userUID);

        // set the action bar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: setup camera/gallery option
        ImageView imageView = findViewById(R.id.group_create_imageview);

        // if 'create group' button is clicked, send information to firestore
        final EditText editText = findViewById(R.id.group_create_edittext);
        Button button = findViewById(R.id.group_create_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a group id
                String groupID = "GROUP_" + userUID + "_" + UUID.randomUUID();

                // get group name from EditText
                String groupName = editText.getText().toString();

                // get firestore
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                DocumentReference reference = firestore
                        .collection("Groups")
                        .document(groupID);

                // add group name to group document
                Map<String, Object> docData = new HashMap<>();
                docData.put("groupName", groupName);
                docData.put("groupID", groupID);
                reference.set(docData);

                // add each member's id to group document and add group id to each
                // member's document
                for (String s: members) {
                    // add member id to group
                    reference.update("members", FieldValue.arrayUnion(s));

                    // add group id to member
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(s)
                            .update("groups", FieldValue.arrayUnion(groupID));

                    // create a placeholder chat item, TODO: set imageUrl
                    ChatItem chatItem = new ChatItem("", groupID, groupName,
                            null, null, s,
                            null, null, null);

                    // Send to user's message preview collection
                    FirebaseFirestore.getInstance()
                            .collection("Previews")
                            .document(s)
                            .collection("ChatPreviews")
                            .document(groupID)
                            .set(chatItem);
                }

                // set the result as successful
                Intent intent = new Intent();
                intent.putExtra("groupID", groupID);
                intent.putExtra("groupName", groupName);
                setResult(Activity.RESULT_OK, intent);

                // destroy this activity
                finish();

            }
        });


    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_plain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // can do sth here
        return super.onOptionsItemSelected(item);
    }

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
