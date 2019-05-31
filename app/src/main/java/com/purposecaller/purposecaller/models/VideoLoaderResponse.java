package com.purposecaller.purposecaller.models;

import android.support.annotation.Keep;

import java.util.ArrayList;

@Keep
public class VideoLoaderResponse {
      public ArrayList<Object> mValues;
     public String  nextPageToken;

    public VideoLoaderResponse(ArrayList<Object> mValues, String nextPageToken) {
        this.mValues = mValues;
        this.nextPageToken = nextPageToken;
    }
}
