package com.purposecaller.purposecaller.mutualwatch;


import android.support.annotation.Keep;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@Keep
public class LiveRoom {


    public String videoId,createdBy;
    @ServerTimestamp
    public Date createdAt;
    public int videoLength;
    public long userCount;


    public LiveRoom() {
    }

    public LiveRoom(String videoId, String createdBy, int videoLength, long userCount) {
        this.videoId = videoId;
        this.createdBy = createdBy;
        this.videoLength = videoLength;
        this.userCount = userCount;
    }
}




