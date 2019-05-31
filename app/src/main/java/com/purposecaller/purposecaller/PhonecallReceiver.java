package com.purposecaller.purposecaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.purposecaller.purposecaller.models.Contact;

import java.util.Date;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.Constants.CALL_TYPE_INCOMING;
import static com.purposecaller.purposecaller.Constants.CALL_TYPE_OUTGOING;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;

public  class PhonecallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    String phonenumber;

    public static int mCurrentCallType;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing


    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected  void onIncomingCallReceived(Context ctx, String number, Date start){
        mCurrentCallType=CALL_TYPE_INCOMING;
     //   Intent i=new Intent(ctx,PhoneCallService.class);
      //  i.putExtra("phoneNumber",number);
    //   ctx.startService(i);
    }
    protected  void onIncomingCallAnswered(Context ctx, String number, Date start){

    }
    protected  void onIncomingCallEnded(Context ctx, String number, Date start, Date end){

    }

    protected  void onOutgoingCallStarted(Context ctx, String number, Date start){
        mCurrentCallType=CALL_TYPE_OUTGOING;
        Intent i = new Intent(ctx, PhoneCallService.class);

        i.putExtra("contact",new Contact(null,number,CONTACT_TYPE_NORMAL));


        ctx.startService(i);

    }
    protected  void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
                ctx
                .getSharedPreferences("my_pref",Context.MODE_PRIVATE)
                .edit().remove("purposeCode").apply();
        try{
            FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(getApplicationContext());
                phonenumber = phoneUtil.format(phoneUtil.parse(number,
                        getCountryCode()),
                        PhoneNumberUtil.PhoneNumberFormat.E164);



            }

        }
        catch (NumberParseException e){}


    }

    public  String getCountryCode(){

        TelephonyManager telephonyMngr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        return telephonyMngr.getSimCountryIso().toUpperCase();

    }

    protected  void onMissedCall(Context ctx, String number, Date start){
        SharedPreferences pref=ctx.getSharedPreferences("my_pref",Context.MODE_PRIVATE);
        String msg=pref.getString("auto_message",null);
        Boolean isEnabled=pref.getBoolean("auto_message_state",false);
        if(msg!=null && isEnabled) {
            sendSMS(number, msg);
        }


    };
    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                else
                {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }
}