package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatdraw.Callers.AudioPlayer;
import com.example.chatdraw.Callers.BaseActivity;
import com.example.chatdraw.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GroupCallActivity extends BaseActivity {
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    public DatabaseReference mRef;
    private TextView mCallDuration;
    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private Button btnCancel;
    private ImageButton btnSpeaker, btnMute, btnBack;
    private String userID, groupID, callID, groupName, userName, imageUrl;
    private int groupSize;
    private TextView tvCallStatus, tvGroupTitle;
    protected Call call;

    private boolean isMute = false;
    private boolean isSpeaker = false;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            GroupCallActivity.this.runOnUiThread(() -> updateCallDuration());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        Intent intent = getIntent();

        mAudioPlayer = new AudioPlayer(this);
        tvCallStatus = findViewById(R.id.callStatus);
        tvGroupTitle = findViewById(R.id.groupDetails);
        mCallDuration = findViewById(R.id.callDuration);

        groupSize = intent.getIntExtra("participant", 0);
        imageUrl = intent.getStringExtra("imageUrl");
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("userID");
        groupID = intent.getStringExtra("groupID");
        groupName = intent.getStringExtra("groupName");

        tvGroupTitle.setText(groupName);

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(view -> endCall());

        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(view -> {
            AudioController audioController = getSinchServiceInterface().getAudioController();

            if (isSpeaker) {
                btnSpeaker.setBackgroundColor(getResources().getColor(R.color.btn_logut_bg));
                audioController.disableSpeaker();

                isSpeaker = false;
            } else {
                btnSpeaker.setBackgroundColor(getResources().getColor(R.color.greyish));
                audioController.enableSpeaker();

                isSpeaker = true;
            }
        });

        btnMute = findViewById(R.id.btnMute);
        btnMute.setOnClickListener(view -> {
            AudioController audioController = getSinchServiceInterface().getAudioController();

            if (isMute) {
                btnMute.setBackgroundColor(getResources().getColor(R.color.s));
                audioController.unmute();
                isMute = false;
            } else {
                btnMute.setBackgroundColor(getResources().getColor(R.color.greyish));
                audioController.mute();
                isMute = true;
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());
    }

    @Override
    public void onServiceConnected() {
        if (!getSinchServiceInterface().getIsOnGoingCall()) {
            call = getSinchServiceInterface().callConference(groupID);
            callID = call.getCallId();

            Log.v("PPPP", "QUEEN");

        } else {
            callID = getSinchServiceInterface().getCurrentGroupCallID();
            call = getSinchServiceInterface().getCall(callID);

            Log.v("QQQQ", "QUEEN");


            tvGroupTitle.setText(groupName + " (" + 1 + "/" + groupSize + ")");
            tvCallStatus.setText("Connected");
        }

        call.addCallListener(new SinchCallListener());
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

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(callID);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
            Log.d("CallListener", "Call progressing");
        }

        @Override
        public void onCallEstablished(Call call) {
            getSinchServiceInterface().setIsOnGoingCall(true);
            getSinchServiceInterface().setGroupUserName(groupID);
            getSinchServiceInterface().setCurrentGroupCallID(callID);
            getSinchServiceInterface().startForegroundActivity();

            mRef = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID);

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Toast.makeText(GroupCallActivity.this, "There is on going call", Toast.LENGTH_SHORT).show();
                            Log.v("testing", "" + ds);

//                            DatabaseReference tes = FirebaseDatabase.getInstance()
//                                    .getReference("GroupCall")
//                                    .child(groupID)
//                                    .child("members");
//
//                            tes.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    if (!dataSnapshot.exists()) {
//                                        Log.v("kuyyyys", "FU");
//                                    } else {
//                                        Log.v("kuyyy", "OK");
//                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                                            Log.v("kuyy", "" + ds);
//                                            LinkedList<Member> ll = new LinkedList<>();
//                                            ll.add(ds.getValue(Member.class));
//
//                                            for (Member tester: ll) {
//                                                Log.v("PLEASE", "" + tester);
//                                                Log.v("PLEASE", "" + tester.getName());
//                                                Log.v("PLEASE", "" + tester.getimageUrl());
//                                            }
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });
                        }
                    } else {
                        Log.v("New Conference Call", "Group call has been started by: " + userName);
                        Member pp = new Member(userName, imageUrl);

                        Map<String, Object> hmap = new HashMap<>();
                        hmap.put("participants", 1);

                        mRef.setValue(hmap);
                        mRef.child("members").child("user1").setValue(pp);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d("CallListener", "Call established");

            tvCallStatus.setText("Connected");
            tvGroupTitle.setText(groupName + " (" + 1 + "/" + groupSize + ")");

            LinearLayout callLayout = findViewById(R.id.onGoingCallLayout);
            callLayout.setAlpha(1f);

            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.unmute();
            audioController.disableSpeaker();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call call) {
            getSinchServiceInterface().setIsOnGoingCall(false);
            getSinchServiceInterface().setGroupUserName(null);
            getSinchServiceInterface().setCurrentGroupCallID(null);
            getSinchServiceInterface().stopForegroundActivity();

            Log.d("CallListener", "Call ended");
            tvCallStatus.setText("Disconnected");

            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            endCall();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private void endCall() {
        getSinchServiceInterface().setIsOnGoingCall(false);

        mAudioPlayer.stopProgressTone();
        if (call != null) {
            call.hangup();
            call = null;
        }
        finish();
    }
}
