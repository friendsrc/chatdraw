package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class GroupCallActivity extends AppCompatActivity {
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private Button btnCancel;
    private ImageButton btnSpeaker, btnMute, btnBack;
    private Chronometer chronometer;
    private String userID, groupID, groupName;
    private int participant;
    private TextView tvCallStatus, tvGroupTitle;
    protected Call call;
    private SinchClient sinchClient;

    private boolean isMute = false;
    private boolean isSpeaker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        Intent intent = getIntent();

        tvCallStatus = (TextView) findViewById(R.id.callStatus);
        tvGroupTitle = (TextView) findViewById(R.id.groupDetails);

        participant = intent.getIntExtra("participant", 0);
        userID = intent.getStringExtra("userID");
        groupID = intent.getStringExtra("groupID");
        groupName = intent.getStringExtra("groupName");

        sinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext())
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .userId(userID)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.addSinchClientListener(new SinchClientListener() {
            public void onClientStarted(SinchClient client) {
                Log.d("onClientStarted", client.toString());
            }

            public void onClientStopped(SinchClient client) {
                Log.d("onClientStopped", "");
            }

            public void onClientFailed(SinchClient client, SinchError error) {
                Log.d("onClientFailed", error.getMessage());
            }

            public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) {
            }

            public void onLogMessage(int level, String area, String message) {
                Log.d("onLogMessage", message);
            }
        });

        sinchClient.start();

        tvGroupTitle.setText(groupName + " (" + participant + "/10)");

        while (!sinchClient.isStarted()) {
            LinearLayout callLayout = (LinearLayout) findViewById(R.id.onGoingCallLayout);
            callLayout.setAlpha(0.25f);
            Log.v("heyyo", "poop");
        }

        Log.d("CallListener", "Calling user: " + groupID);
        call = sinchClient.getCallClient().callConference(groupID);
        call.addCallListener(new CallListener() {
            @Override
            public void onCallProgressing(Call call) {
                Log.d("CallListener", "Call progressing");
            }

            @Override
            public void onCallEstablished(Call call) {
                Log.d("CallListener", "Call established");
                tvCallStatus.setText("connected");
                tvGroupTitle.setText(groupName + " (" + (participant + 1) + "/10)");

                chronometer = findViewById(R.id.chronometer);
                chronometer.setFormat("Time: %s");
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();

                LinearLayout callLayout = (LinearLayout) findViewById(R.id.onGoingCallLayout);
                callLayout.setAlpha(1f);

                AudioController audioController = sinchClient.getAudioController();
                audioController.unmute();
                audioController.disableSpeaker();
                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            }

            @Override
            public void onCallEnded(Call call) {
                Log.d("CallListener", "Call ended");
                tvCallStatus.setText("disconnected");
                setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            }

            @Override
            public void onShouldSendPushNotification(Call call, List<PushPair> list) {

            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null) {
                    Toast.makeText(GroupCallActivity.this, "There is no ongoing call", Toast.LENGTH_SHORT).show();
                } else {
                    call.hangup();
                    call = null;
                }

                finish();
            }
        });

        btnSpeaker = (ImageButton) findViewById(R.id.btnSpeaker);
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioController audioController = sinchClient.getAudioController();

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
                AudioController audioController = sinchClient.getAudioController();

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
}
