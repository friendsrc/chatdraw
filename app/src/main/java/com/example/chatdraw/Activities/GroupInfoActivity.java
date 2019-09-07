package com.example.chatdraw.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupUID;
    private String groupName;
    private String groupImageUrl;
    private ArrayList<String> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Intent intent = getIntent();
        groupUID = intent.getStringExtra("id");

        FirebaseFirestore.getInstance()
                .collection("Groups")
                .document(groupUID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // get group data
                        groupName = documentSnapshot.get("groupName").toString();
                        if (documentSnapshot.get("groupImageUrl") != null) {
                            groupImageUrl = documentSnapshot.get("groupImageUrl").toString();
                        }
                        groupMembers = (ArrayList<String>) documentSnapshot.get("members");

                        // set the toolbar
                        Toolbar myToolbar = findViewById(R.id.my_toolbar);
                        setSupportActionBar(myToolbar);

                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayHomeAsUpEnabled(true);
                            actionBar.setTitle(groupName);
                        }
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
