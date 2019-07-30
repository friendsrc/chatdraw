package com.example.chatdraw.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatdraw.R;

import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.Factory;

import java.text.ParseException;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    private final int REQUEST_USE_SIP = 109;
    private final String KEY = "password";
    private final String USERNAME = "sacchirro";
    private final String DOMAIN = "sip.linphone.org";
    private final String PASSWORD = "sacchirro11";

    private SipManager manager = null;
    private SipProfile profile = null;
    private SipAudioCall call = null;
    private SipAudioCall incCall = null;

    private static TextView status;
    private EditText adrToCall, authorization;
    private Button makeCallBtn, endCallBtn;
    private IncomingReceiver receiver;

    // private AuthInfo mAuthInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

//        mAuthInfo = Factory.instance().createAuthInfo(
//                "https://stackoverflow.com/questions/53616538/linphone-android-not-receiving-calls"
//                )
        // Attaches the view's button onto the private
        makeCallBtn = (Button) findViewById(R.id.makeCall);
        endCallBtn = (Button) findViewById(R.id.endCall);
        status = (TextView) findViewById(R.id.textView);

        // get address text and authorization fields
        adrToCall = (EditText) findViewById(R.id.adrToCall);
        authorization = (EditText) findViewById(R.id.passcode);

        // Creates a listener for the buttons
        makeCallBtn.setOnClickListener(this);
        endCallBtn.setOnClickListener(this);

        make();
    }

    @Override
    public void onResume() {
        super.onResume();
        make();
    }

    private void make() {
        // Determines if the device is capable of VoIP
        if (SipManager.isVoipSupported(getApplicationContext()) &&
            SipManager.isApiSupported(getApplicationContext())) {
            Log.e("$$", "Call Activity");

            // Creates a Sip Manager for the App
            makeSipManager();

            // Creates the user's Sip Profile
            makeSipProfile();

            Log.d("Test", adrToCall.getText().toString());

            call = new SipAudioCall(this, profile);  // setup your calling profile

            // Listener object to handle SIP functions
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    call.startAudio();
                    call.setSpeakerMode(true);
                    Log.d("call", "Call established");

                    if (call.isMuted()) {
                        call.toggleMute();
                    }
                }

                @Override
                public void onCallEnded(SipAudioCall endedCall) {
                    super.onCallEnded(endedCall);
                    Log.d("call", "Call ended");

                    try {
                        endedCall.endCall();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };

            call.setListener(listener);

            // Set up Intent filter to receive calls
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.Chatdraw.INCOMING_CALL");
            receiver = new IncomingReceiver();
            this.registerReceiver(receiver, filter);
        } else {
            Toast.makeText(this, "Your device does not support VoIP", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAll();
    }

    public void onPause() {
        super.onPause();
        closeAll();
    }

    // Closes any current calls and unregisters the receiver
    public void closeAll() {
        try {
            manager.close(profile.getUriString());
            call.close();
            this.unregisterReceiver(receiver);
        } catch (SipException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.makeCall) {
            // check if you are authorized to call
            if (!(authorization.getText().toString().equals(KEY))) {
                Toast.makeText(this, "Incorrect Authorization Code", Toast.LENGTH_SHORT).show();
                return;
            }

            // check for an address
            if (adrToCall.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please tell me who to call", Toast.LENGTH_SHORT).show();
                return;
            }

            // Make a Call
            Toast.makeText(this, "Call Made", Toast.LENGTH_SHORT).show();
            SipProfile.Builder builder;
            SipProfile toCall;

            try {
                builder = new SipProfile.Builder(adrToCall.getText().toString(), DOMAIN);
                toCall = builder.build();
                SipSession.Listener ssl = new SipSession.Listener() {
                    @Override
                    public void onCallEnded(SipSession session) {
                        super.onCallEnded(session);
                        try {
                            call.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                        session.endCall();
                    }
                };

                call.makeCall(toCall, manager.createSipSession(profile, ssl), 30);
            } catch (SipException e) {
                Toast.makeText(this, "Call Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ParseException e) {
                Toast.makeText(this, "Call Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Call Ended #1", Toast.LENGTH_SHORT).show();
            try {
                call.endCall();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    public void incomingCall(SipAudioCall c) {
        if (c == null) {
            return;
        }

        // if there is a call already, ignore new one
        if (call.isInCall()) {
            return;
        }

        // if there is an incoming call
        if (incCall != null) {
            return;
        }

        // else if call isn't null
        incCall = c;
        SipProfile caller = incCall.getPeerProfile();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Incoming call from:")
                .setMessage(caller.getUriString())
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Toast.makeText(CallActivity.this, "Incoming Call Accepted", Toast.LENGTH_SHORT).show();
                            incCall.answerCall(30);
                            incCall.startAudio();
                            incCall.setSpeakerMode(true);

                            if (incCall.isMuted()) {
                                incCall.toggleMute();
                            }
                        } catch (SipException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(CallActivity.this, "Incoming Call Declined", Toast.LENGTH_SHORT).show();
                        try {
                            incCall.endCall();
                        } catch (SipException e) {
                            e.printStackTrace();
                        }

                        incCall.close();
                        incCall = null;
                    }
                });
        builder.show();
    }

    private void makeSipManager() {
        // Creates a SipManager to enable calls
        if (manager == null) {
            manager = SipManager.newInstance(this);
            Log.e("$$", "Manager was instantiated");
        }
    }

    private void makeSipProfile() {

        if (manager != null) {
            // Creates a SipProfile for the User
            try {
                SipProfile.Builder builder = new SipProfile.Builder(USERNAME, DOMAIN);
                builder.setPassword(PASSWORD);
                profile = builder.build();
                Log.e("$$", "SipProfile was built");

                // Creates an intent to receive calls
                Intent intent = new Intent();
                intent.setAction("android.Chatdraw.INCOMING_CALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
                manager.open(profile, pendingIntent, null);

                // Determines if the SipProfile successfully registered
                manager.setRegistrationListener(profile.getUriString(),
                        new SipRegistrationListener() {
                            @Override
                            public void onRegistering(String s) {
                                Log.e("$$", "Sip Profile <" + s + "> is registering");
                            }

                            @Override
                            public void onRegistrationDone(String s, long l) {
                                Log.e("$$", "Sip Profile <" + s + "> successfully registered");
                            }

                            @Override
                            public void onRegistrationFailed(String s, int i, String s1) {
                                Log.e("$$", "Sip Profile failed to register <" + s + "> Error Message: " +s1);
                            }
                        });
            } catch (ParseException e) {
                Log.e("$$", "SipProfile was not built");
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    public class IncomingReceiver extends BroadcastReceiver {
        // Processing the incoming call and answer it
        @Override
        public void onReceive(Context context, Intent intent) {
            SipAudioCall incomingCall = null;
            try {
                SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                    @Override
                    public void onRinging(SipAudioCall call, SipProfile caller) {
                        Log.e("Call", "Call incoming!");
                    }
                };

                CallActivity ca = (CallActivity) context;
                incomingCall = ca.manager.takeAudioCall(intent, listener);

                ca.incomingCall(incomingCall);
            } catch (Exception e) {
                e.printStackTrace();
                if (incomingCall != null) {
                    incomingCall.close();
                }
            }
        }
    }
}
