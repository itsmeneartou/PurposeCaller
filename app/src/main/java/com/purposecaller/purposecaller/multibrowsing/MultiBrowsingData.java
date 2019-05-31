package com.purposecaller.purposecaller.multibrowsing;


import android.support.annotation.Keep;

@Keep
public class MultiBrowsingData {

 public String url,uid;


    public MultiBrowsingData(String url,String uid) {
        this.url = url;
        this.uid=uid;
    }
    public MultiBrowsingData() {

    }

    public void setData(String url,String uid) {
        this.url = url;
        this.uid=uid;
    }
}




