package com.purposecaller.purposecaller.windows;

import android.content.Context;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.purposecaller.purposecaller.utils.AdViewUtils;
import com.purposecaller.purposecaller.R;

@Keep
public class TextViewWindow extends RelativeLayout implements View.OnClickListener{

    TextView textView;
    AdView adView;
    DatabaseReference ref;
    public String TAG="TextViewWindow";
    String text;

    public TextViewWindow(Context context,DatabaseReference ref,String text) {
        super(context);

        this.ref=ref;
        this.text=text;
        init(null, 0);
    }

    public TextViewWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TextViewWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View v=inflate(getContext(), R.layout.window_text_view,null);
        addView(v);

      adView=v.findViewById(R.id.ad_view);


        v.findViewById(R.id.close_button).setOnClickListener(this);

       textView=v.findViewById(R.id.text_view);
        if(text==null){
            ref.child("text").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    new AdViewUtils(adView).loadAd();
                    if(dataSnapshot.exists()){
                        String message=getResources().getString(R.string.the_caller_says,dataSnapshot.getValue());
                        textView.setText(message);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,databaseError.getMessage());
                }
            });
        }
        else{
            new AdViewUtils(adView).loadAd();
            textView.setText(text);
        }



    }

    public void setText(String text) {
        textView.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
        }
    }
}
