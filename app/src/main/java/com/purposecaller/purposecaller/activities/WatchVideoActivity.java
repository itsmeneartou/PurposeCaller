package com.purposecaller.purposecaller.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.fragments.ChatFragment;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.mutualwatch.LiveRoom;
import com.purposecaller.purposecaller.mutualwatch.MutualWatchData;

import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_CHAT;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VIDEO_CALL;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_LIVE_ROOM;
import static com.purposecaller.purposecaller.mutualwatch.MutualWatchData.VIDEO_STATE_PAUSED;
import static com.purposecaller.purposecaller.mutualwatch.MutualWatchData.VIDEO_STATE_PLAYING;
import static com.purposecaller.purposecaller.mutualwatch.MutualWatchData.VIDEO_STATE_STOPPED;

public class WatchVideoActivity extends PurposeActivity {

    private String videoId;
    LiveRoom liveRoom;
    MutualWatchData mutualWatchData;
    YouTubePlayerView youtubePlayerView;
    YouTubePlayer youTubePlayer;
    float previousSecond=0;
    LinearLayout rootContainer;
    FrameLayout interactViaContainer;
    ViewGroup.LayoutParams interactViaParams;
    YouTubePlayerTracker tracker = new YouTubePlayerTracker();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        contact=getIntent().getParcelableExtra("contact");
        room=getIntent().getParcelableExtra("room");
       youtubePlayerView = findViewById(R.id.youtube_player_view);
        interactViaContainer=findViewById(R.id.interact_via_container);
       rootContainer=findViewById(R.id.root_container);
        videoId=getIntent().getStringExtra("videoId");

        gameDataValueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long previousState=-1;
                    if(mutualWatchData!=null) previousState=mutualWatchData.videoState;
                    mutualWatchData=dataSnapshot.getValue(MutualWatchData.class);

                    if(mutualWatchData!=null && !uid.equals(mutualWatchData.lastUpdatedBy)){

                        if(videoId==null){
                            videoId=mutualWatchData.videoId;
                           initializeYoutubePlayer();
                        }
                        if(youTubePlayer!=null){
                            if(mutualWatchData.videoState==VIDEO_STATE_PAUSED||mutualWatchData.videoState==VIDEO_STATE_STOPPED
                                   ){

                                youTubePlayer.pause();
                               showToast(contact.name+" paused the video");
                                if(Math.abs(tracker.getCurrentSecond()-mutualWatchData.currentPlaybackTime)>3000){
                                    youTubePlayer.seekTo(mutualWatchData.currentPlaybackTime/1000);
                                }

                            }
                            else if(mutualWatchData.videoState==VIDEO_STATE_PLAYING && previousState!=MutualWatchData.VIDEO_STATE_PLAYING){
                                showToast(contact.name+" played the video");
                                if(Math.abs(tracker.getCurrentSecond()-mutualWatchData.currentPlaybackTime)>3000){
                                    youTubePlayer.seekTo(mutualWatchData.currentPlaybackTime/1000);
                                }

                                youTubePlayer.play();
                            }
                        }



                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if(videoId!=null){
         initializeYoutubePlayer();

        }

        if(contact.contactType==CONTACT_TYPE_LIVE_ROOM){
            if(room.interactVia==INTERACT_VIA_CHAT){
                chatFragment=new ChatFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.interact_via_container,chatFragment).commit();
            }
            chatFragment.setMessageDatabaseReference(rootRef.child("live_video_rooms").child(videoId).child("messages"));
        }
        else{
          doCommonPurposeWork();
        }


    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }


    public void initializeYoutubePlayer(){
        getLifecycle().addObserver(youtubePlayerView);

        youtubePlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(@NonNull final YouTubePlayer initializedYouTubePlayer) {
                youTubePlayer=initializedYouTubePlayer;
                youTubePlayer.addListener(tracker);
                youtubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
                    @Override
                    public void onYouTubePlayerEnterFullScreen() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }

                    @Override
                    public void onYouTubePlayerExitFullScreen() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                });
                initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        initializedYouTubePlayer.loadVideo(videoId, 0);


                    }

                    @Override
                    public void onVideoDuration(final float duration) {
                        if(contact!=null && contact.contactType==CONTACT_TYPE_LIVE_ROOM && liveRoom==null){


                            final DocumentReference reference= FirebaseFirestore.getInstance().collection("live_video_rooms").document(videoId);
                            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        liveRoom=documentSnapshot.toObject(LiveRoom.class);
                                        if(youTubePlayer!=null&& liveRoom!=null){
                                            youTubePlayer.seekTo(getLiveVideoPosition(duration));
                                        }
                                    }
                                    else{
                                        liveRoom=new LiveRoom(videoId,uid,(int)duration*1000,1);
                                        reference.set(liveRoom);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
                        int stateInLong=getPlayerStateInLong(state);
                        if(mutualWatchData!=null && stateInLong!=MutualWatchData.VIDEO_STATE_BUFFERING){


                            mutualWatchData.videoState = stateInLong;
                            mutualWatchData.lastUpdatedBy = uid;

                            switch(stateInLong){
                                case MutualWatchData.VIDEO_STATE_PAUSED:
                                    mutualWatchData.currentPlaybackTime=(long)(tracker.getCurrentSecond()*1000);
                            }
                            gameDataRef.setValue(mutualWatchData);
                        }
                        else if(stateInLong==MutualWatchData.VIDEO_STATE_ENDED && contact.contactType== Contact.CONTACT_TYPE_LIVE_ROOM &&
                                youTubePlayer!=null){


                                youTubePlayer.seekTo(getLiveVideoPosition(tracker.getVideoDuration()));
                               youTubePlayer.play();

                        }


                    }

                    @Override
                    public void onCurrentSecond(float second) {
                        if(Math.abs(second-previousSecond)>2 && contact.contactType!=CONTACT_TYPE_LIVE_ROOM && mutualWatchData!=null){
                            mutualWatchData.currentPlaybackTime=(long)second*1000;
                            mutualWatchData.lastUpdatedBy = uid;
                            gameDataRef.setValue(mutualWatchData);
                        }
                        previousSecond=second;
                    }
                });
            }
        }, true);
    }

    public int getPlayerStateInLong(PlayerConstants.PlayerState state){
        switch(state.name()){
            case "PLAYING":return MutualWatchData.VIDEO_STATE_PLAYING;

            case "PAUSED":return MutualWatchData.VIDEO_STATE_PAUSED;

            case "BUFFERING":return MutualWatchData.VIDEO_STATE_BUFFERING;

            case "ENDED":return MutualWatchData.VIDEO_STATE_ENDED;


        }
        return -1;
    }





    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            youtubePlayerView.enterFullScreen();
            if( Build.VERSION.SDK_INT < 19) { //lower api
                View v = getWindow().getDecorView();
                v.setSystemUiVisibility(View.GONE);
            } else if(Build.VERSION.SDK_INT >= 19) {
                //for new api versions.
                View decorView =getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
            if(room.interactVia==INTERACT_VIA_VIDEO_CALL){
                interactViaParams= interactViaContainer.getLayoutParams();
                 rootContainer.removeView(interactViaContainer);
                 youtubePlayerView.addinteractionView(interactViaContainer);
            }
            else{
                findViewById(R.id.interact_via_container).setVisibility(View.GONE);
            }



        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            youtubePlayerView.exitFullScreen();
            if( Build.VERSION.SDK_INT < 19) { // lower api
                View v = getWindow().getDecorView();
                v.setSystemUiVisibility(View.VISIBLE);
            } else if(Build.VERSION.SDK_INT >= 19) {
                //for new api versions.
                View decorView =getWindow().getDecorView();
                decorView.setSystemUiVisibility(0);
            }

            if(room.interactVia==INTERACT_VIA_VIDEO_CALL){
                youtubePlayerView.removeView(interactViaContainer);
                rootContainer.addView(interactViaContainer,interactViaParams);

            }
            else{
                findViewById(R.id.interact_via_container).setVisibility(View.VISIBLE);
            }



        }
    }



    public int getLiveVideoPosition(float videoDuration){
        if(liveRoom!=null){
            int position=(int)((((System.currentTimeMillis()-liveRoom.createdAt.getTime())/1000)%videoDuration));
            return  position;
        }

        else return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        youtubePlayerView.release();
    }
}
