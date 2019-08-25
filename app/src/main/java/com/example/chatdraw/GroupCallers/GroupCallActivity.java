package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.Callers.AudioPlayer;
import com.example.chatdraw.Callers.BaseActivity;
import com.example.chatdraw.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GroupCallActivity extends BaseActivity {
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private TextView mCallDuration;
    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private Button btnCancel;
    private ImageButton btnSpeaker, btnMute, btnBack;
    private String userID, groupID, callID, groupName;
    private int participant;
    private TextView tvCallStatus, tvGroupTitle;
    protected Call call;

    private boolean isMute = false;
    private boolean isSpeaker = false;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            GroupCallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        Intent intent = getIntent();

        mAudioPlayer = new AudioPlayer(this);
        tvCallStatus = (TextView) findViewById(R.id.callStatus);
        tvGroupTitle = (TextView) findViewById(R.id.groupDetails);
        mCallDuration = (TextView) findViewById(R.id.callDuration);

        participant = intent.getIntExtra("participant", 0);
        userID = intent.getStringExtra("userID");
        groupID = intent.getStringExtra("groupID");
        groupName = intent.getStringExtra("groupName");

        tvGroupTitle.setText(groupName);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
            }
        });

        btnSpeaker = (ImageButton) findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        btnMute = (ImageButton) findViewById(R.id.btnMute);
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onServiceConnected() {
        if (!getSinchServiceInterface().getIsOnGoingCall()) {
            call = getSinchServiceInterface().callConference(groupID);
            callID = call.getCallId();
        } else {
            callID = getSinchServiceInterface().getCurrentGroupCallID();
            call = getSinchServiceInterface().getCall(callID);

            tvGroupTitle.setText(groupName + " (" + (participant + 1) + "/10)");
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

            Log.d("CallListener", "Call established");

            tvCallStatus.setText("Connected");
            tvGroupTitle.setText(groupName + " (" + (participant + 1) + "/10)");

            LinearLayout callLayout = (LinearLayout) findViewById(R.id.onGoingCallLayout);
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
