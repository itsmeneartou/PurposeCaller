package com.purposecaller.purposecaller.mutualwatch;


import android.support.annotation.Keep;

@Keep
public class MutualWatchData {

    public static  final int VIDEO_STATE_PLAYING=1;
    public static  final int VIDEO_STATE_PAUSED=2;
    public static  final int VIDEO_STATE_BUFFERING=3;
    public static  final int VIDEO_STATE_STOPPED=4;

    public static  final int VIDEO_STATE_LOADING=5;
    public static  final int VIDEO_STATE_LOADED=6;
    public static  final int VIDEO_STATE_ADSTARTED=7;
    public static  final int VIDEO_STATE_ENDED=8;
    public String videoId;
    public long currentPlaybackTime;
    public long videoLength;
    public long videoState;
    public String lastUpdatedBy;


    public MutualWatchData() {
    }

    public MutualWatchData(String videoId,String lastUpdatedBy) {
        this.videoId = videoId;
        this.lastUpdatedBy=lastUpdatedBy;
    }
}




