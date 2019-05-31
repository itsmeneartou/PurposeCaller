package com.purposecaller.purposecaller.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.utils.PermissionUtils;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.Constants.RC_AUDIO_PERMISSION;
import static io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION;


public class VoiceFragment extends Fragment implements View.OnClickListener{
    private  final String LOG_TAG = getClass().getName();

    private Handler mHandler = new Handler(Looper.getMainLooper());
     private String roomName;
    private TextView timeElapsed,contactName,contactNumber;
    private Contact contact;
    private RtcEngine mRtcEngine;
    private class UpdateElapsedTimeTask extends TimerTask {
        long elapsedSeconds;




        private UpdateElapsedTimeTask(int elapsedSeconds) {
            this.elapsedSeconds = elapsedSeconds;

        }

        @Override
        public void run() {

            elapsedSeconds =elapsedSeconds+1L;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    timeElapsed.setText(DateUtils.formatElapsedTime(elapsedSeconds));
                }
            });

        }
    }
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1


        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new UpdateElapsedTimeTask(elapsed/1000),0,1000);

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {

        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {

        }

        @Override
        public void onLeaveChannel(RtcStats stats) {

        }

        @Override
        public void onUserOffline(final int uid, final int reason) {

            // Tutorial Step 4
           getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }
    };


    public VoiceFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_voice, container, false);
        v.findViewById(R.id.mute).setOnClickListener(this);
        v.findViewById(R.id.speaker).setOnClickListener(this);
        v.findViewById(R.id.speaker).performClick();
        v.findViewById(R.id.end).setOnClickListener(this);
        timeElapsed=v.findViewById(R.id.timeElapsed);
        contactName=v.findViewById(R.id.caller_name);
        contactNumber=v.findViewById(R.id.phone_number);
        if(contact!=null){
            contactName.setText(contact.name);
            contactNumber.setText(contact.phoneNumber);
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("roomName",roomName);
        outState.putParcelable("contact",contact);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args=getArguments();
         if(args!=null){
             roomName=args.getString("roomName");
             contact=args.getParcelable("contact");
         }
         else if(savedInstanceState!=null){
             contact=savedInstanceState.getParcelable("contact");
             roomName=savedInstanceState.getString("roomName");
         }
        if(new PermissionUtils().checkAndRequestPermissions(getActivity(),RC_AUDIO_PERMISSION,this, Manifest.permission.RECORD_AUDIO)){
            initAgoraEngineAndJoinChannel();
        }
    }

    public final void showLongToast(final String msg) {
      getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel();               // Tutorial Step 2
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case RC_AUDIO_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);

                }
                break;
            }
        }
    }


    @Override
   public void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }





    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setChannelProfile(CHANNEL_PROFILE_COMMUNICATION);
        } catch (Exception e) {


            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void joinChannel() {
        if(roomName!=null){

            mRtcEngine.joinChannel(null, roomName, "Extra Optional Data",0);
        }
        // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));

    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.mute:  ImageView iv = (ImageView) view;
                if (iv.isSelected()) {
                    iv.setSelected(false);
                    iv.clearColorFilter();
                } else {
                    iv.setSelected(true);
                    iv.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                }

                mRtcEngine.muteLocalAudioStream(iv.isSelected());
                break;
            case R.id.speaker:
                ImageView speaker = (ImageView) view;
                if (speaker.isSelected()) {
                    speaker.setSelected(false);
                    speaker.clearColorFilter();
                } else {
                    speaker.setSelected(true);
                    speaker.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                }

                mRtcEngine.setEnableSpeakerphone(view.isSelected());

                break;
            case R.id.end:
        }
    }
}
