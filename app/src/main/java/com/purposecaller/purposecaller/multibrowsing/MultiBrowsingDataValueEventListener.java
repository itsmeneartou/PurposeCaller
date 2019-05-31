package com.purposecaller.purposecaller.multibrowsing;


import android.webkit.WebView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.purposecaller.purposecaller.activities.MultiBrowsingActivity;

public class MultiBrowsingDataValueEventListener implements ValueEventListener {



  WebView webView;
   MultiBrowsingActivity activity;
    public MultiBrowsingDataValueEventListener(WebView webView, MultiBrowsingActivity activity) {
        this.webView = webView;
        this.activity=activity;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        if (dataSnapshot.exists()) {
              MultiBrowsingData data=dataSnapshot.getValue(MultiBrowsingData.class);
              if(data!=null && (activity.data==null || !data.url.equals(activity.data.url))){
                  activity.data=data;
                  webView.loadUrl(data.url);

              }
        }

    }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }







}




