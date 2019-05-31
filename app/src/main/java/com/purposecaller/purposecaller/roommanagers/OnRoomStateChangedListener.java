package com.purposecaller.purposecaller.roommanagers;

import android.support.annotation.Keep;

@Keep
public interface OnRoomStateChangedListener {

    void onRoomJoined(String key);
    void onRoomCreated(String key);

}
