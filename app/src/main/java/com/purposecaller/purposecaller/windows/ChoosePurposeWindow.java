package com.purposecaller.purposecaller.windows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.utils.AdViewUtils;

import static com.purposecaller.purposecaller.Constants.PURPOSE_TIC_TAC_TOE;


public class ChoosePurposeWindow extends RelativeLayout implements View.OnClickListener{
    AdView adView;
   ChangeViewListener changeViewListener;
    public interface ChangeViewListener{
        void changeViewTo(int view);

    }
    public ChoosePurposeWindow(Context context,ChangeViewListener changeViewListener) {
        super(context);
        this.changeViewListener=changeViewListener;
        init(null, 0);
    }

    public ChoosePurposeWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ChoosePurposeWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setChangeViewListener(ChangeViewListener changeViewListener) {
        this.changeViewListener = changeViewListener;
    }

    private void init(AttributeSet attrs, int defStyle) {
        View view = inflate(getContext(), R.layout.window_choose_purpose, null);
        addView(view);
        adView=view.findViewById(R.id.ad_view);
        new AdViewUtils(adView).loadAd();
        view.findViewById(R.id.tic_tac_toe).setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        if (changeViewListener != null) {
            switch (view.getId()) {

                case R.id.tic_tac_toe:changeViewListener.changeViewTo(PURPOSE_TIC_TAC_TOE);
                    break;


            }
        }
    }





}
