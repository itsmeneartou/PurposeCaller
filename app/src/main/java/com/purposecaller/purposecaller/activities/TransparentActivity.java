package com.purposecaller.purposecaller.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TransparentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O_MR1)
            { setShowWhenLocked(true);}
        }
        catch(Exception e){}

    }
}
