package com.purposecaller.purposecaller.models;

import android.support.annotation.Keep;

@Keep
public class Player {
public static final int NOT_JOINED=1;
    public static final int JOINED=2;
    public static final int LEFT=3;
    public int currentConnectivityStatus;
    public Contact contact;

    public Player() {

    }

    public Player(int currentConnectivityStatus, Contact contact ) {
        this.currentConnectivityStatus = currentConnectivityStatus;
        this.contact=contact;
    }
}
