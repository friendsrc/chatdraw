package com.example.chatdraw.Callers;

import com.example.chatdraw.Drawing.DrawActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.example.chatdraw.R;
import com.squareup.picasso.Picasso;

public class CallScreenActivity extends BaseActivity {
    static final String TAG = CallScreenActivity.class.getSimpleName();

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private boolean isIncomingCall = false;

    private String mCallId;
    private String mFriendIncomingCallID;
    private String mFriendCallID;
    private String mFriendName;
    private String mUserID;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;

    private ImageView mImageCall;
    private ImageButton mCallBack;
    private ImageButton mDraw;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(() -> updateCallDuration());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);
        mCallerName = findViewById(R.id.remoteUser);
        mCallState = findViewById(R.id.callState);
        mImageCall = findViewById(R.id.profileImgID);
        mCallBack = findViewById(R.id.btnCallBack);
        mDraw = findViewById(R.id.btnDraw);
        Button endCallButton = findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(v -> endCall());

        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        mFriendIncomingCallID = getIntent().getStringExtra(SinchService.FRIEND_ID);

        if (mFriendIncomingCallID != null) {
            isIncomingCall = true;
        }

        mUserID = getIntent().getStringExtra("userID");
        mFriendCallID = getIntent().getStringExtra("FriendID");
        mFriendName = getIntent().getStringExtra("FriendName");

        mCallBack.setOnClickListener(v -> finish());

        mDraw.setOnClickListener(view -> {
            // Go to draw activity
            Intent drawIntent = new Intent(CallScreenActivity.this, DrawActivity.class);
            drawIntent.putExtra("userUID", mUserID + mCallId);
            drawIntent.putExtra("friendsUID", mFriendCallID + mCallId);
            startActivity(drawIntent);
        });
    }

    @Override
    public void onServiceConnected() {
        // TODO Need to fix HERE for Drawing
        Call call = getSinchServiceInterface().getCall(mCallId);

        if ((call != null) || getSinchServiceInterface().getIsOnGoingCall()) {
            DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    call.addCallListener(new SinchCallListener());
                    String userUID = call.getRemoteUserId();

                    String name = (String) dataSnapshot.child(userUID).child("name").getValue();
                    String imageURL = (String) dataSnapshot.child(userUID).child("imageUrl").getValue();

                    Picasso.get()
                            .load(imageURL)
                            .fit()
                            .into(mImageCall);

                    mCallerName.setText(name);
                    mCallState.setText(call.getState().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(CallScreenActivity.this, "Failed to place a call. Error code: 801", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
        finish();
    }

    private void endCall() {
        getSinchServiceInterface().setIsOnGoingCall(false);
        getSinchServiceInterface().setFriendName(null);
        getSinchServiceInterface().setCurrentUserCallID(null);

        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            getSinchServiceInterface().setTryConnectCallID(null);
            getSinchServiceInterface().setTryConnectUser(null);
            getSinchServiceInterface().setIsOnGoingCall(false);
            getSinchServiceInterface().setFriendName(null);
            getSinchServiceInterface().setFriendUserName(null);
            getSinchServiceInterface().setCurrentUserCallID(null);
            getSinchServiceInterface().stopForegroundActivity();

            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            getSinchServiceInterface().setTryConnectCallID(null);
            getSinchServiceInterface().setTryConnectUser(null);
            getSinchServiceInterface().setIsOnGoingCall(true);

            if (isIncomingCall) {
                getSinchServiceInterface().setFriendUserName(mFriendIncomingCallID);
            } else {
                getSinchServiceInterface().setFriendUserName(mFriendCallID);
            }

            getSinchServiceInterface().setFriendName(mFriendName);
            getSinchServiceInterface().setCurrentUserCallID(mCallId);
            getSinchServiceInterface().startForegroundActivity();

            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.disableSpeaker();
            audioController.enableAutomaticAudioRouting(true, AudioController.UseSpeakerphone.SPEAKERPHONE_AUTO);

            if (getSinchServiceInterface().getIsDrawingCall()) {
                // Go to draw activity
                Intent intent = new Intent(CallScreenActivity.this, DrawActivity.class);
                intent.putExtra("userUID", mUserID);
                intent.putExtra("friendsUID", mFriendCallID);
                startActivity(intent);
            }
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }
}
