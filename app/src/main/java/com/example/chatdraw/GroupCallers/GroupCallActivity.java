package com.example.chatdraw.GroupCallers;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private String userID;
    private String groupID;
    TextView tvCallStatus;
    private Call call;
    SinchClient sinchClient;
    String authorization = "";
    private boolean isNewCall = false;
    TextView num_participant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        num_participant = (TextView) findViewById(R.id.num_participant);
        // setAppUser = (EditText)findViewById(R.id.appUser);

        Intent intent = getIntent();
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

        btnCall = (Button)findViewById(R.id.btnCall);
        btnCall.setOnClickListener(new View.OnClickListener() {
            String userToCall;
            @Override
            public void onClick(View v) {
                if(call == null){
                    userToCall = groupID;
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
                } else {
                    call.hangup();
                    call = null;
                    btnCall.setText("CALL");
                }
            }
        });


        String myURL = "https://callingapi.sinch.com/v1/conferences/id/" + groupID;

        RequestQueue requestQueue = Volley.newRequestQueue(GroupCallActivity.this);
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                myURL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("QPO", "" + response);
                        try {
                            num_participant.setText("Number of people in the conference call: " + response.getJSONArray("participants").length() + ". Join?");
                        } catch (JSONException e) {
                            Toast.makeText(GroupCallActivity.this, "Unknown error occurred [802]", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            if (error.networkResponse.statusCode == 404) {
                                num_participant.setText("There is no conference call currently. Start a new one?");
                                isNewCall = true;
                                Toast.makeText(GroupCallActivity.this, "No call before", Toast.LENGTH_SHORT).show();
                            }

                            Log.e("Rest Response error", "" + error.networkResponse.statusCode);
                        } catch (NullPointerException e) {

                        }
                    }
                }
        ){
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String namePassword = APP_KEY + ":" + APP_SECRET;
                String auth = Base64.encodeToString(namePassword.getBytes(), Base64.NO_WRAP);

                authorization = "Basic" + " " + auth;

                params.put("Authorization", authorization);
                return params;
            }
        };

        requestQueue.add(objectRequest);
    }
}

