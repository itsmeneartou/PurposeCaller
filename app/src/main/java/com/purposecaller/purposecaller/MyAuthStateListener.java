package com.purposecaller.purposecaller;

import android.app.Activity;
import android.support.annotation.Keep;

import android.support.v4.app.Fragment;

import com.firebase.ui.auth.AuthUI;


import java.util.Arrays;

import static com.purposecaller.purposecaller.Constants.RC_SIGN_IN;
import static com.purposecaller.purposecaller.MyApplication.mUser;




@Keep
public class MyAuthStateListener {


    public static void login(Activity activity){
        activity.startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.PhoneBuilder().build()
                )).build(), RC_SIGN_IN);
    }

    public static void login(Fragment fragment){
        fragment.startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.PhoneBuilder().build()
                )).build(), RC_SIGN_IN);
    }

    public static String getPhotoUrl(){
        if(mUser!=null){
            return mUser.getPhotoUrl()!=null?mUser.getPhotoUrl().toString():null;
        }
        return null;

    }

}
