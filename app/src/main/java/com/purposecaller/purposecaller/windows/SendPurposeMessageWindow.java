package com.purposecaller.purposecaller.windows;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.purposecaller.purposecaller.models.MessagePurpose;
import com.purposecaller.purposecaller.R;

import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;


/**
 * TODO: document your custom view class.
 */
public class SendPurposeMessageWindow extends CardView implements View.OnClickListener{

    EditText mEditText;
    AdView adView;
    DatabaseReference ref;
    public String TAG="TextViewWindow";

    public SendPurposeMessageWindow(Context context, DatabaseReference ref) {
        super(context);

        this.ref=ref;
        init(null, 0);
    }

    public SendPurposeMessageWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SendPurposeMessageWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View v=inflate(getContext(), R.layout.window_purpose_message,null);
        addView(v);
      mEditText=v.findViewById(R.id.purpose_edittext);
      adView=v.findViewById(R.id.ad_view);
        v.findViewById(R.id.send).setOnClickListener(this);




    }



    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.send:    ref.setValue(new MessagePurpose(mEditText.getText().toString()
                    ,PURPOSE_TEXT_MESSAGE));
                getContext().getSharedPreferences("my_pref",Context.MODE_PRIVATE)
                        .edit()
                        .putInt("purposeCode",PURPOSE_TEXT_MESSAGE)
                        .apply();
        }
    }
}
