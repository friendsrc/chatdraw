package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.Adapters.GroupMemberListAdapter;
import com.example.chatdraw.Config.GlobalStorage;
import com.example.chatdraw.Items.GroupMemberListItem;
import com.example.chatdraw.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupUID;
    private String groupName;
    private String groupImageUrl;
    private ArrayList<String> groupMembers;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Intent intent = getIntent();
        groupUID = intent.getStringExtra("id");
        listView = (ListView) findViewById(R.id.memberListView);

        // create Adapter and set to ListView
        GroupMemberListAdapter groupMemberListAdapter = new GroupMemberListAdapter(this);
        ListView listView = findViewById(R.id.memberListView);
        listView.setAdapter(groupMemberListAdapter);

        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupUID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // get group data
                    groupName = documentSnapshot.get("groupName").toString();
                    if (documentSnapshot.get("groupImageUrl") != null) {
                        groupImageUrl = documentSnapshot.get("groupImageUrl").toString();
                    }
                    groupMembers = (ArrayList<String>) documentSnapshot.get("members");

                    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (String memberUid: groupMembers) {
                                String name = dataSnapshot.child(memberUid).getValue(User.class).getName();
                                String description = dataSnapshot.child(memberUid).getValue(User.class).getDescription();
                                String imageUrl = dataSnapshot.child(memberUid).getValue(User.class).getImageUrl();

                                Log.v("GROUPDETAILS", name + "@@@" + description + "@@@" + imageUrl);

                                // create a new GroupMemberListItem and add to ListView
                                GroupMemberListItem groupMemberListItem = new GroupMemberListItem(name, description, memberUid, imageUrl);
                                groupMemberListAdapter.addAdapterItem(groupMemberListItem);
                                groupMemberListAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // set the toolbar
                    Toolbar myToolbar = findViewById(R.id.my_toolbar);
                    setSupportActionBar(myToolbar);

                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        actionBar.setTitle("Group Info");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navbar_group_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // if the back button is pressed, go back to previous activity
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_info_add_member:
                Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                break;
            case R.id.group_info_edit:
                Intent intent = new Intent(GroupInfoActivity.this, GroupInfoEditActivity.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupUID", groupUID);
                intent.putExtra("groupImageUrl", groupImageUrl);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
    // set on click listener to the ListView
//    ListView listView = findViewById(R.id.main_chat_listview);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//@Override
//public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//        FriendListItem friendListItem = (FriendListItem) mFriendListAdapter.getItem(position);
//        intent.putExtra("name", friendListItem.getName());
//        intent.putExtra("uID", friendListItem.getUID());
//        startActivity(intent);
//        }
//        });