package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatdraw.R;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class GroupCallActivity extends AppCompatActivity {
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    Button btnStartClient;
    Button btnCall;
    Button btnCancel;
    private String userID;
    private String participant;
    private Chronometer chronometer;
    private String groupID;
    TextView tvCallStatus;
    protected Call call;
    SinchClient sinchClient;
    String authorization = "";
    private boolean isNewCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        // setAppUser = (EditText)findViewById(R.id.appUser);

        Intent intent = getIntent();

        participant = intent.getStringExtra("participant");
        userID = intent.getStringExtra("userID");
        groupID = intent.getStringExtra("groupID");

        Toast.makeText(this, "" + userID, Toast.LENGTH_SHORT).show();

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

        tvCallStatus = (TextView) findViewById(R.id.callStatus);

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("Time: %s");
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCall = (Button) findViewById(R.id.btnCall);
        btnCall.setOnClickListener(new View.OnClickListener() {
            String userToCall;

            @Override
            public void onClick(View v) {
                if (call == null) {
                    userToCall = groupID;
                    Log.d("CallListener", "Calling user: " + userToCall);
                    call = sinchClient.getCallClient().callConference(userToCall);
                    call.addCallListener(new CallListener() {
                        @Override
                        public void onCallProgressing(Call call) {
                            Log.d("CallListener", "Call progressing");
                        }

                        @Override
                        public void onCallEstablished(Call call) {
                            Log.d("CallListener", "Call established");
                            tvCallStatus.setText("connected");

                            LinearLayout textbox = (LinearLayout) findViewById(R.id.confirmLayout);
                            textbox.setAlpha(0.5f);

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

                    btnCall.setText("HANG UP");
                } else {
                    call.hangup();
                    call = null;
                    btnCall.setText("CALL");
                }
            }
        });
    }
}
