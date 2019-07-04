package com.example.chatdraw.CreateGroupActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chatdraw.ChatActivites.ChatActivity;
import com.example.chatdraw.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // get this user's ID and add to array
        final String userUID;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(GroupCreateActivity.this);
        if (acct != null) {
            userUID = acct.getId();
        } else {
            userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // set the action bar
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
                for (String s: memberUIDs) {
                    // add member id to group
                    reference.update("members", FieldValue.arrayUnion(s));

                    // add group id to member
                    FirebaseFirestore.getInstance().collection("Users")
                            .document(s)
                            .update("groups", FieldValue.arrayUnion(groupID));
                }

                // add this user
                reference.update("members", FieldValue.arrayUnion(userUID));
                FirebaseFirestore.getInstance().collection("Users")
                        .document(userUID)
                        .update("groups", FieldValue.arrayUnion(groupID));

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

    @Override
    // if the back button is pressed, destroy the activity
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
