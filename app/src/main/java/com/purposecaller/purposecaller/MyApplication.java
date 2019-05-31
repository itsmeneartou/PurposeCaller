package com.purposecaller.purposecaller;

import android.support.annotation.Keep;
import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

@Keep
public class MyApplication extends MultiDexApplication {
    public static String uid,phoneNumber;
    public static FirebaseAuth mAuth;
    public static FirebaseUser mUser;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if(mUser!=null){
            uid=mUser.getUid();
            phoneNumber=mUser.getPhoneNumber();
        }
    }


}
