package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GroupCallActivity extends BaseActivity {
    private static final int NUM_COLS = 2;
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private TableLayout table;
    private HashMap<String, Member> userIdMemberMap;
    public DatabaseReference mRef;
    private TextView mCallDuration;
    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private Button btnCancel;
    private ImageButton btnSpeaker, btnMute, btnBack, btnDraw;
    private String userID, groupID, callID, groupName, userName, imageUrl;
    private int groupSize;
    private TextView tvCallStatus, tvGroupTitle, tvGroupName;
    protected Call call;
    private boolean isFirstUser = false;
    private boolean isPassingDelete = false;

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
        tvGroupName = findViewById(R.id.groupName);
        tvCallStatus = findViewById(R.id.callStatus);
        tvGroupTitle = findViewById(R.id.groupDetails);
        mCallDuration = findViewById(R.id.callDuration);

        groupSize = intent.getIntExtra("participant", 0);
        imageUrl = intent.getStringExtra("imageUrl");
        userName = intent.getStringExtra("userName");
        userID = intent.getStringExtra("userID");
        groupID = intent.getStringExtra("groupID");
        groupName = intent.getStringExtra("groupName");

        tvGroupName.setText(groupName);

        table = findViewById(R.id.tableForImages);

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setEnabled(false);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
            }
        });

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

        btnDraw = findViewById(R.id.btnDraw);
    }

    private void populateImages(Map hmap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowHeight = displayMetrics.heightPixels;
        int windowWidth = displayMetrics.widthPixels;

        Log.d("WOIIII", "" + windowHeight + " " + windowWidth);
        int buttonHeight = (windowHeight - 500) / 2;

        // for now we consider number of member in a group maximum of 50
        Member[] memberArray = new Member[50];

        int totalParticipant = hmap.size();
        int num_rows = totalParticipant/2;

        if (totalParticipant % 2 != 0) {
            num_rows = totalParticipant/2 + 1;
        }

        int counter = 0;
        for (Object key: hmap.keySet()) {
            Member tempMember = (Member) hmap.get(key);

            Log.v("TESTERINGER", "" + tempMember);
            memberArray[counter] = tempMember;
            counter++;
        }

        table.removeAllViews();

        for (int row = 0; row < num_rows; row++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));

            table.addView(tableRow);

            for (int col = 0; col < NUM_COLS; col++) {
                if ((row * 2) + col > totalParticipant - 1) {
                    break;
                }

                LinearLayout ll = new LinearLayout(this);
                ll.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        buttonHeight,
                        1.0f
                ));

                ll.setOrientation(LinearLayout.VERTICAL);

                ImageView imgView = new ImageView(this);
                imgView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        buttonHeight - 100,
                        1.0f
                ));

                // imgView.setImageResource(R.drawable.ic_credits);
                String imgUrl = memberArray[(row * 2) + col].getimageUrl();
                Picasso.get()
                        .load(imgUrl)
//                        .resize(windowWidth/2 - 40, buttonHeight - 100)
                        .fit()
                        .into(imgView);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        80,
                        1.0f
                );
                params.setMargins(10,0,10, 0);

                TextView tv1 = new TextView(this);
                tv1.setLayoutParams(params);
                tv1.setGravity(Gravity.TOP|Gravity.CENTER);
                tv1.setBackgroundColor(getResources().getColor(R.color.bluegray600));
                tv1.setTextColor(getResources().getColor(R.color.white));
                tv1.setTextSize(18);

                String myName = memberArray[(row * 2) + col].getName();
                tv1.setText(myName);

                ll.addView(imgView);
                ll.addView(tv1);

                // Make text not clip on small button
                // button.setPadding(0, 0, 0, 0);
                tableRow.addView(ll);
            }
        }
    }


    public void enableCancelButton() {
        btnCancel.setEnabled(true);
    }

    @Override
    public void onServiceConnected() {
        if (!getSinchServiceInterface().getGroupIsOnGoingCall()) {
            call = getSinchServiceInterface().callConference(groupID);
            callID = call.getCallId();

            Log.v("PPPP", "QUEEN");

        } else {
            callID = getSinchServiceInterface().getCurrentGroupCallID();
            call = getSinchServiceInterface().getCall(callID);

            Log.v("QQQQ", "QUEEN");

            btnCancel.setEnabled(true);

            mRef = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID);

            Log.v("DORAEMON", "LOL");
            DatabaseReference tes = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID)
                    .child("members");

            tes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists() && isPassingDelete) {
                        tes.removeValue();
                        mRef.removeValue();
                        tes.removeEventListener(this);
                    } else if (dataSnapshot.exists() && isPassingDelete) {
                        isPassingDelete = false;
                        tes.removeEventListener(this);
                    } else if (!dataSnapshot.exists()) {
                        Log.v("New Conference Call", "Group call has been started by: " + userName);
                        Member pp = new Member(userName, imageUrl);

                        Map<String, Object> hmap = new HashMap<>();
                        hmap.put("participants", 1);

                        mRef.setValue(hmap);
                        mRef.child("members").child(userID).setValue(pp);
                        tvGroupTitle.setText("(1/" + groupSize + ")");
                    } else {
                        Log.v("kuyyy", "OK" + dataSnapshot);

                        HashMap<String, Member> userIdMemberMap = new HashMap<>();

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.v("kuyy", "" + ds);
                            userIdMemberMap.put(ds.getKey(), ds.getValue(Member.class));
                            Log.v("kuyy", "" + userIdMemberMap);
                        }

                        Member pp = new Member(userName, imageUrl);
                        userIdMemberMap.put(userID, pp);

                        Log.v("TESTERING", "" + userIdMemberMap);
                        populateImages(userIdMemberMap);

                        Map<String, Object> hmap = new HashMap<>();

                        int numberOfParticipant = userIdMemberMap.size();
                        if (numberOfParticipant > 5) {
                            tes.removeEventListener(this);
                        }
                        tvGroupTitle.setText("(" + numberOfParticipant + "/" + groupSize + ")");
                        hmap.put("participants", numberOfParticipant);

                        Log.v("HELLLLOOO", userIdMemberMap.size() + " " + userIdMemberMap);

                        mRef.updateChildren(hmap);
                        mRef.child("members").setValue(userIdMemberMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

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
            getSinchServiceInterface().setGroupIsOnGoingCall(true);
            getSinchServiceInterface().setGroupName(groupName);
            getSinchServiceInterface().setGroupUserName(groupID);
            getSinchServiceInterface().setCurrentGroupCallID(callID);
            getSinchServiceInterface().startForegroundActivity();

            mRef = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID);

            Log.v("DORAEMON", "LOL");
            DatabaseReference tes = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID)
                    .child("members");

            tes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists() && isPassingDelete) {
                        tes.removeValue();
                        mRef.removeValue();
                        tes.removeEventListener(this);
                    } else if (dataSnapshot.exists() && isPassingDelete) {
                        isPassingDelete = false;
                        tes.removeEventListener(this);
                    } else if (!dataSnapshot.exists()) {
                        Log.v("New Conference Call", "Group call has been started by: " + userName);
                        Member pp = new Member(userName, imageUrl);

                        Map<String, Object> hmap = new HashMap<>();
                        hmap.put("participants", 1);

                        mRef.setValue(hmap);
                        mRef.child("members").child(userID).setValue(pp);
                        tvGroupTitle.setText("(1/" + groupSize + ")");
                    } else {
                        Log.v("kuyyy", "OK" + dataSnapshot);

                        HashMap<String, Member> userIdMemberMap = new HashMap<>();

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.v("kuyy", "" + ds);
                            userIdMemberMap.put(ds.getKey(), ds.getValue(Member.class));
                            Log.v("kuyy", "" + userIdMemberMap);
                        }

                        Member pp = new Member(userName, imageUrl);
                        userIdMemberMap.put(userID, pp);

                        Log.v("TESTERING", "" + userIdMemberMap);
                        populateImages(userIdMemberMap);

                        Map<String, Object> hmap = new HashMap<>();

                        int numberOfParticipant = userIdMemberMap.size();
                        if (numberOfParticipant > 5) {
                            tes.removeEventListener(this);
                        }
                        tvGroupTitle.setText("(" + numberOfParticipant + "/" + groupSize + ")");
                        hmap.put("participants", numberOfParticipant);

                        Log.v("HELLLLOOO", userIdMemberMap.size() + " " + userIdMemberMap);

                        mRef.updateChildren(hmap);
                        mRef.child("members").setValue(userIdMemberMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d("CallListener", "Call established");

            tvCallStatus.setText("Connected");

            LinearLayout callLayout = findViewById(R.id.onGoingCallLayout);
            callLayout.setAlpha(1f);

            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.unmute();
            audioController.disableSpeaker();
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    btnCancel.setEnabled(true);
                }
            };

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(runnable, 3, SECONDS);
        }

        @Override
        public void onCallEnded(Call call) {
            getSinchServiceInterface().setGroupIsOnGoingCall(false);
            getSinchServiceInterface().setGroupName(null);
            getSinchServiceInterface().setGroupUserName(null);
            getSinchServiceInterface().setCurrentGroupCallID(null);
            getSinchServiceInterface().stopForegroundActivity();

            DatabaseReference tes = FirebaseDatabase.getInstance()
                    .getReference("GroupCall")
                    .child(groupID)
                    .child("members");

            isPassingDelete = true;
            Log.v("DELETE", "" + tes.getRef().child(userID));
            tes.getRef().child(userID).removeValue();


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
        getSinchServiceInterface().setGroupIsOnGoingCall(false);

        mAudioPlayer.stopProgressTone();
        if (call != null) {
            call.hangup();
            call = null;
        }
        finish();
    }
}
