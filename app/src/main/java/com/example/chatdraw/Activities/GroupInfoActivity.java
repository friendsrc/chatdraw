package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.AccountActivity.User;
import com.example.chatdraw.Adapters.GroupMemberListAdapter;
import com.example.chatdraw.Adapters.GroupSimpleMenuAdapter;
import com.example.chatdraw.Config.GroupReportWebService;
import com.example.chatdraw.Config.NonScrollListView;
import com.example.chatdraw.Items.GroupMemberListItem;
import com.example.chatdraw.Items.SimpleMenuItem;
import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GroupInfoActivity extends AppCompatActivity {

    private String userUID;
    private String groupUID;
    private String groupName;
    private String groupImageUrl;
    private ArrayList<String> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Intent intent = getIntent();
        groupUID = intent.getStringExtra("groupUid");
        userUID = intent.getStringExtra("userUid");

        TextView tvName = (TextView) findViewById(R.id.group_name);
        TextView tvDetails = (TextView) findViewById(R.id.group_details);

        // create a list view to add user to the group
        GroupSimpleMenuAdapter groupInviteFriendAdapter = new GroupSimpleMenuAdapter(this);
        NonScrollListView invitationListView = (NonScrollListView) findViewById(R.id.inviteFriends);
        invitationListView.setAdapter(groupInviteFriendAdapter);

        SimpleMenuItem simpleMenuInviteItem = new SimpleMenuItem("Add member", R.drawable.add_person);
        SimpleMenuItem simpleMenuLinkItem = new SimpleMenuItem("Invite via a link", R.drawable.ic_link_blue_24dp);
        groupInviteFriendAdapter.addAdapterItem(simpleMenuInviteItem);
        groupInviteFriendAdapter.addAdapterItem(simpleMenuLinkItem);
        groupInviteFriendAdapter.notifyDataSetChanged();

        invitationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    Toast.makeText(GroupInfoActivity.this, "Add member not configured yet " + position, Toast.LENGTH_SHORT).show();
                } else if (position == 1) {
                    // Use dynamic link
                    // https://firebase.google.com/docs/dynamic-links/android/receive
                    // https://firebase.google.com/docs/dynamic-links/use-cases/user-to-user
                    Toast.makeText(GroupInfoActivity.this, "Invite via link not configured yet " + position, Toast.LENGTH_SHORT).show();
                }
            }
        });

        GroupSimpleMenuAdapter groupExitGroupAdapter = new GroupSimpleMenuAdapter(this);
        NonScrollListView exitReportListView = (NonScrollListView) findViewById(R.id.exitReportListView);
        exitReportListView.setAdapter(groupExitGroupAdapter);

        SimpleMenuItem menuExitGroupItem = new SimpleMenuItem("Leave group", R.drawable.ic_exit_to_app_red_24dp);
        SimpleMenuItem menuReportGroupItem = new SimpleMenuItem("Report group", R.drawable.ic_report_red_24dp);
        groupExitGroupAdapter.addAdapterItem(menuExitGroupItem);
        groupExitGroupAdapter.addAdapterItem(menuReportGroupItem);
        groupExitGroupAdapter.notifyDataSetChanged();

        exitReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    new AlertDialog.Builder(GroupInfoActivity.this)
                            .setTitle("Confirm")
                            .setMessage("Do you really want to leave the group?")
                            .setIcon(R.drawable.ic_report_problem_red)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DocumentReference docRef = FirebaseFirestore.getInstance()
                                            .collection("Groups")
                                            .document(groupUID);

                                    DocumentReference previewRef = FirebaseFirestore.getInstance()
                                            .collection("Previews")
                                            .document(userUID)
                                            .collection("ChatPreviews")
                                            .document(groupUID);

                                    Map<String, Object> deleteMember = new HashMap<>();
                                    deleteMember.put("members", FieldValue.arrayRemove(userUID));

                                    docRef.update(deleteMember)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    previewRef.delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Intent intent = new Intent(GroupInfoActivity.this, MainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(intent);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(GroupInfoActivity.this, "Unable to delete chat preview", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                } else { // report group
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://docs.google.com/forms/d/e/")
                            .build();
                    final GroupReportWebService spreadsheetWebService = retrofit.create(GroupReportWebService.class);

                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                    builder.setTitle("Enter report description");

                    // Set up the input
                    final EditText input = new EditText(GroupInfoActivity.this);
                    input.setHint("Enter your description here");
                    input.setHintTextColor(getResources().getColor(R.color.gray_very_light));
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String descriptionReport = input.getText().toString();

                            Call<Void> completeQuestionnaireCall = spreadsheetWebService.completeQuestionnaire(groupUID, userUID, descriptionReport);
                            completeQuestionnaireCall.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    Toast.makeText(GroupInfoActivity.this, "Response submitted", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(GroupInfoActivity.this, "Failed to report the group", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            }
        });

        // create Adapter and set to NonScrollListView
        GroupMemberListAdapter groupMemberListAdapter = new GroupMemberListAdapter(this);
        NonScrollListView listView = findViewById(R.id.memberListView);
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

                    tvName.setText(groupName);
                    tvDetails.setText("Created at: ....");

                    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (String memberUid: groupMembers) {
                                String name = dataSnapshot.child(memberUid).getValue(User.class).getName();
                                String description = dataSnapshot.child(memberUid).getValue(User.class).getDescription();
                                String imageUrl = dataSnapshot.child(memberUid).getValue(User.class).getImageUrl();

                                // create a new GroupMemberListItem and add to NonScrollListView
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