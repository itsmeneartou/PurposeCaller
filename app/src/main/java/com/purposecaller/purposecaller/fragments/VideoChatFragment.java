package com.purposecaller.purposecaller.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.utils.PermissionUtils;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.purposecaller.purposecaller.activities.MainActivity.REQUEST_ID_MULTIPLE_PERMISSIONS;
import static io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION;

public class VideoChatFragment extends Fragment {




    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private FrameLayout primaryVideoView;







    private AudioManager audioManager;

    private static final String TAG = VideoChatFragment.class.getName();
    /*
    * Android application UI elements
    */

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }
    };


    private Context mContext;
    String roomName;
    boolean cameraAndMicPermissionGranted;



    private RtcEngine mRtcEngine;// Tutorial Step 1

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_video_chat,container,false);

        primaryVideoView = v.findViewById(R.id.remote_video_view_container);




        return v;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();         // Tutorial Step 2
        joinChannel();               // Tutorial Step 4
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setChannelProfile(CHANNEL_PROFILE_COMMUNICATION);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoProfile() {
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_320x180,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_7,VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE));
        mRtcEngine.enableVideo();
    }



    // Tutorial Step 4
    private void joinChannel() {

        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    private void setupRemoteVideo(int uid) {


        if (primaryVideoView.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getContext());
       primaryVideoView.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        surfaceView.setTag(uid); // for mark purpose

    }



    // Tutorial Step 6
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {

       primaryVideoView.removeAllViews();



    }



    private void onRemoteUserVideoMuted(int uid, boolean muted) {


        SurfaceView surfaceView = (SurfaceView) primaryVideoView.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }


    // Tutorial Step 6
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext = getContext();
        Bundle args=getArguments();
        if(args!=null){

            roomName=args.getString("roomName");
        }
       if(savedInstanceState!=null){

           roomName=savedInstanceState.getString("roomName");
       }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (checkAndRequestPermission()) {

            cameraAndMicPermissionGranted=true;
            initAgoraEngineAndJoinChannel();
        }
    }






    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode ==REQUEST_ID_MULTIPLE_PERMISSIONS) {
           cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {
                cameraAndMicPermissionGranted=true;
                initAgoraEngineAndJoinChannel();

            } else {
                Toast.makeText(mContext,
                       "Camera and mic permissions are needed.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }




    private boolean checkAndRequestPermission(){
       return new PermissionUtils()
               .checkAndRequestPermissions(getActivity(),REQUEST_ID_MULTIPLE_PERMISSIONS,this,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO);
    }







}
