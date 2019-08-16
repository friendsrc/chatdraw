package com.example.chatdraw.GroupCallers;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class GroupCallActivity extends AppCompatActivity {
    private static final String APP_KEY = "9d0ed01f-2dc2-4c26-a683-9c7e93a90029";
    private static final String APP_SECRET = "awRjs8Mowkq63iR1iFGAgA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    Button btnStartClient;
    Button btnCall;
    EditText etAppUser, etCallUser;
    TextView tvCallStatus;
    private Call call;
    SinchClient sinchClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        // setAppUser = (EditText)findViewById(R.id.appUser);

        sinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext())
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .userId("momo")
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.addSinchClientListener(new SinchClientListener() {
            public void onClientStarted(SinchClient client) {
                Log.d("onClientStarted",client.toString());
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
        sinchClient.getCallClient().addCallClientListener(new CallClientListener() {
            @Override
            public void onIncomingCall(CallClient callClient, Call incomingCall) {
                call = incomingCall;
                call.answer();
                call.addCallListener(new CallListener() {
                    @Override
                    public void onCallProgressing(Call call) {
                        Log.d("CallListener", "Call progressing");
                        tvCallStatus.setText("connecting");
                    }

                    @Override
                    public void onCallEstablished(Call call) {
                        Log.d("CallListener", "Call established");
                        tvCallStatus.setText("connected");
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
            }
        });
        sinchClient.start();

        tvCallStatus = (TextView)findViewById(R.id.callStatus);
        etCallUser = (EditText)findViewById(R.id.callUser);

        btnCall = (Button)findViewById(R.id.btnCall);
        btnCall.setOnClickListener(new View.OnClickListener() {
            String userToCall;
            @Override
            public void onClick(View v) {
                if(call == null){
                    userToCall = etCallUser.getText().toString();
                    Log.d("CallListener", "Calling user: "+userToCall);
                    call = sinchClient.getCallClient().callConference(userToCall);
                    call.addCallListener(new CallListener() {
                        @Override
                        public void onCallProgressing(Call call) {
                            Log.d("CallListener", "Call progressing");
                            tvCallStatus.setText("connecting");
                        }

                        @Override
                        public void onCallEstablished(Call call) {
                            Log.d("CallListener", "Call established");
                            tvCallStatus.setText("connected");
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
                }else{
                    call.hangup();
                    call = null;
                    btnCall.setText("CALL");
                }
            }
        });
    }
}

