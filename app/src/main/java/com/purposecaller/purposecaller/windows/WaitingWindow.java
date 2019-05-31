package com.purposecaller.purposecaller.windows;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.WaveDrawable;
import com.purposecaller.purposecaller.utils.AdViewUtils;

import static com.purposecaller.purposecaller.adapters.ContactsRecyclerViewAdapter.density;

/**
 * TODO: document your custom view class.
 */
public class WaitingWindow extends RelativeLayout {




  AdView adView;

ImageView imageView;
    TextView textView;

    private WaveDrawable waveDrawable;

    public WaitingWindow(Context context, DatabaseReference reference) {
        super(context);
        init(null, 0);
    }

    public WaitingWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WaitingWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View v=inflate(getContext(), R.layout.activity_waiting,null);
        addView(v);

      adView=v.findViewById(R.id.ad_view);
       new AdViewUtils(adView).loadAd();

        ImageView imageView=findViewById(R.id.image);

        textView=findViewById(R.id.text_view);

        int dp=(int)(56*density);


        if(Build.VERSION.SDK_INT>=23){

            waveDrawable = new WaveDrawable(getResources().getColor(R.color.greyish,null),500);
        }
        else{


            waveDrawable = new WaveDrawable(getResources().getColor(R.color.greyish),500);
        }

        waveDrawable.setWaveInterpolator(new LinearInterpolator(getContext(),null));

        waveDrawable.startAnimation();

        imageView.setBackground(waveDrawable);



    }

    public int[] getScreenDimensions(DisplayMetrics displayMetrics){
        int width=displayMetrics.widthPixels;
        int height=displayMetrics.heightPixels;

        return new int[]{width,height};
    }








    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        if (result < desiredSize){
            Log.e("ChartView", "The view is too small, the content might get cut");
        }
        return result;
    }


}
