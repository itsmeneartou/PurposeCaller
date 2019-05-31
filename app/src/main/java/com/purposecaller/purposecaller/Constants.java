package com.purposecaller.purposecaller;

import android.support.annotation.Keep;

@Keep
public class Constants {

    //purposes
    public static final int PURPOSE_TIC_TAC_TOE=2;
    public static final int PURPOSE_TEXT_MESSAGE=1;
    public static final int PURPOSE_LUDO=3;
    public static final int PURPOSE_QUIZ=4;
    public static final int PURPOSE_BROWSING=5;
    public static final int PURPOSE_MUTUAL_WATCH=6;
    public static final int PURPOSE_STATE_WAITING=7;

    // way of communication
    public static final int INTERACT_VIA_CHAT =1;
    public static final int INTERACT_VIA_SIM_CALL =2;
    public static final int INTERACT_VIA_VOIP_CALL =3;
    public static final int INTERACT_VIA_VIDEO_CALL =4;

    public static final int  CALL_TYPE_INCOMING=1;
    public static final int  CALL_TYPE_OUTGOING=2;

    //permission request codes

    public static final int  REQUEST_CODE_CONTACTS=1;
    public static final int  REQUEST_CODE_PHONE_STATE_OUTGOING_CALLS=2;
    public static final int  REQUEST_CODE_CHOOSE_VIDEO=3;
    public static final int RC_SIGN_IN=4;
    public static final int RC_CHOOSE_IMAGE=5;
    public static final int RC_WRITE_EXTERNAL_STORAGE=6;
    public static final int RC_WAIT_FOR_PLAYER=7;
    public static final int  RC_AUDIO_PERMISSION=8;

    //OnActivityResult codes
    public static final int  RESULT_CODE_VIDEO_CHOSEN=1;
    public static final int  RESULT_CODE_ROOM_WORK_DONE=2;

    public static final int VIDEO_QUERY_TYPE_POPULAR=1;
    public static final int VIDEO_QUERY_TYPE_SEARCH=2;



    public static long mCurrentPlayer;
    public static long mOpponent;

}
