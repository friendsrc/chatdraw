package com.example.chatdraw.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;

public class InviteFriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);

        final TextView link = findViewById(R.id.link_view);

        Button copyButton = findViewById(R.id.copy_button);
        copyButton.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Chatdraw link", link.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(InviteFriendActivity.this, "Link copied", Toast.LENGTH_SHORT).show();
        });

        Button shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hello!\nI am using Chatdraw app to chat. Download and enjoy the experience of drawing while chatting with others. Download now at: " + link.getText());
            Intent intent = Intent.createChooser(shareIntent,"Share");
            startActivity(intent);
        });

    }
}
