package com.purposecaller.purposecaller.models;

import android.support.annotation.Keep;

@Keep
public class MessagePurpose extends BasePurpose{

  public String mMessage;


    public MessagePurpose() {
    }

    public MessagePurpose(String mMessage,int mPurposeType) {
        this.mPurposeType=mPurposeType;
        this.mMessage = mMessage;
    }
}
