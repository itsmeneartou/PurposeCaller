package com.purposecaller.purposecaller.utils;


import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdViewUtils  {
AdView adView;
    public AdViewUtils(AdView adView) {
        this.adView=adView;
    }

    public void loadAd(){
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int i) {

            }
        });
        adView.loadAd(new AdRequest.Builder().addTestDevice("57ABEB0A2B9EAD3825A72115D3895C0F")
                .addTestDevice("765664F944176BF16F1C5487DE0629C4").addTestDevice("391BBAB9063743EFCFA1AC2481929495")
                .addTestDevice("450DFE88B0DE29D445F8F542325CD100").build());
    }
}
