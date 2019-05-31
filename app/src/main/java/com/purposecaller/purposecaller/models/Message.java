package com.purposecaller.purposecaller.models;

import android.support.annotation.Keep;

@Keep
public class Message {
public String text,uid,photoUrl;


    public Message() {

    }

    public Message(String mTextMessage,String uid,String url) {
        this.uid=uid;
        this.photoUrl=url;
        this.text = mTextMessage;

    }


}
