package com.purposecaller.purposecaller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.purposecaller.purposecaller.activities.TransparentActivity;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Room;
import com.purposecaller.purposecaller.utils.SIMCallUtils;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import static android.view.View.VISIBLE;
import static com.purposecaller.purposecaller.Constants.CALL_TYPE_INCOMING;
import static com.purposecaller.purposecaller.Constants.CALL_TYPE_OUTGOING;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_SIM_CALL;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;
import static com.purposecaller.purposecaller.MainPhoneCallWindow.PURPOSE_CHOOSER;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.PhonecallReceiver.mCurrentCallType;
import static com.purposecaller.purposecaller.Constants.mCurrentPlayer;
import static com.purposecaller.purposecaller.Constants.mOpponent;
import static com.purposecaller.purposecaller.models.Room.ROOM_CREATOR;
import static com.purposecaller.purposecaller.models.Room.ROOM_JOINER;

public class PhoneCallService extends Service{
    private WindowManager mWindowManager;
    ValueEventListener mValueEventListener;


    String phonenumber;
   public static WindowManager.LayoutParams params;
    MainPhoneCallWindow mpcw;

    //Variable to check if the Floating widget view is on left side or in right side
    // initially we are displaying Floating widget view to Left side so set it to true
   public final String TAG="PhoneCallReceiver";

    public PhoneCallService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


       if(mWindowManager==null){
           mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
       }


        addFloatingWidgetView();
        addRemoveView();

        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //init WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O_MR1){
            startActivity(new Intent(this,TransparentActivity.class));
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))||Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            addFloatingWidgetView();
            addRemoveView();
        }



    }

    private View addRemoveView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //Inflate the removing view layout we created
        mpcw.removeFloatingWidgetView = inflater.inflate(R.layout.close_chat_head, null);

        //Add the view to the window.
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

        //Initially the Removing widget view is not visible, so set visibility to GONE
        mpcw.removeFloatingWidgetView.setVisibility(View.GONE);
        mpcw.removeImageView = mpcw.removeFloatingWidgetView.findViewById(R.id.remove_img);

        //Add the view to the window
        mWindowManager.addView(mpcw.removeFloatingWidgetView, paramRemove);
        return mpcw.removeImageView;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
           mUser = FirebaseAuth.getInstance().getCurrentUser();

            if (mUser != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    int readPhoneStatePermissionCheck = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);

                    if (readPhoneStatePermissionCheck == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, getResources().getString(R.string.sufficient_permission_read), Toast.LENGTH_LONG).show();
                        stopSelf();
                        return START_STICKY;
                    }
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, getResources().getString(R.string.overdraw_read), Toast.LENGTH_LONG).show();
                        stopSelf();
                        return START_STICKY;
                    }
                }
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(getApplicationContext());
               mpcw.contact=intent.getParcelableExtra("contact");
                mpcw.room=intent.getParcelableExtra("room");
                phonenumber = phoneUtil.format(phoneUtil.parse(mpcw.contact.phoneNumber,
                        getCountryCode()),
                        PhoneNumberUtil.PhoneNumberFormat.E164);
                mpcw.mDatabaseReference = SIMCallUtils.getNumberComboRef(FirebaseDatabase.getInstance().getReference().child("private_rooms"),
                        new Contact(null,phonenumber));

                if (mCurrentCallType == CALL_TYPE_INCOMING) {

                    mCurrentPlayer = ROOM_JOINER;
                    mOpponent = ROOM_CREATOR;
                    if(mpcw.room!=null){

                        mpcw.mDatabaseReference=mpcw.mDatabaseReference.child(mpcw.room.roomName);
                        changeCollapsedViewVisibility((int)mpcw.room.purposeCode,null,VISIBLE);
                    }


                } else if (mCurrentCallType == CALL_TYPE_OUTGOING) {

                      SharedPreferences pref=getSharedPreferences("my_pref", Context.MODE_PRIVATE);
                    mCurrentPlayer = ROOM_CREATOR;
                    mOpponent = ROOM_JOINER;
                    int purposeCode = pref.getInt("purposeCode", -1);

                    if (purposeCode >= 0) {

                        String key=pref.getString("roomKey_"+phonenumber,null);
                        if(key!=null){
                            mpcw.room=new Room(key,purposeCode,INTERACT_VIA_SIM_CALL);

                            mpcw.mDatabaseReference=mpcw.mDatabaseReference.child(key);
                        }

                        changeCollapsedViewVisibility(purposeCode,null,VISIBLE);
                    }
                    else if(phoneNumber.equals(phonenumber)){

                        changeCollapsedViewVisibility(PURPOSE_TEXT_MESSAGE,getString(R.string.pc_call_yourself),VISIBLE);
                    }
                    else {

                        changeCollapsedViewVisibility(PURPOSE_TEXT_MESSAGE,getString(R.string.data_downloading),VISIBLE);
                        DatabaseReference ref = mpcw.mDatabaseReference.getRoot().child(getString(R.string.membership_check_ref)).child(phonenumber);

                        ref.keepSynced(true);

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    mpcw.contact=dataSnapshot.getValue(Contact.class);
                                    mpcw.contact.phoneNumber=dataSnapshot.getKey();
                                    changeCollapsedViewVisibility(PURPOSE_CHOOSER,null,VISIBLE);



                                } else {

                                    changeCollapsedViewVisibility(PURPOSE_TEXT_MESSAGE,getString(R.string.not_on_pc_invite,"calling"),VISIBLE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }

            }
            else {
                changeCollapsedViewVisibility(PURPOSE_TEXT_MESSAGE,getString(R.string.not_logged_in),View.VISIBLE);
            }
        }
        catch(Exception e){
                  Log.e(TAG,e.getMessage());
        }

        return START_STICKY;
    }

  public void  changeCollapsedViewVisibility(int purpose,@Nullable String message,int visibility){
        mpcw.setmPurposeType(purpose);
        mpcw.setTextWindowMessage(message);
        mpcw.collapsedView.setVisibility(visibility);
    }

    public  String getCountryCode(){

        TelephonyManager telephonyMngr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        return telephonyMngr.getSimCountryIso().toUpperCase();

    }
    /*  Add Remove View to Window Manager  */


    /*  Add Floating Widget View to Window Manager  */
    private void addFloatingWidgetView() {
        //Inflate the floating view layout we created
        mpcw =new MainPhoneCallWindow(this);

        //Add the view to the window.
       params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN
                ,PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;

        //Initially view will be added to top-left corner, you change x-y coordinates according to your need
        params.x = 0;
        params.y = 200;

        //Add the view to the window
        mWindowManager.addView(mpcw, params);





    }

    public float[] getScreenDimensions(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
       return new float[]{displayMetrics.heightPixels,displayMetrics.widthPixels};
    }

    public void attachValueEventListener(){
        if(mValueEventListener==null){
            mValueEventListener=new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try{

                    if(dataSnapshot.exists() && dataSnapshot.hasChildren()){

                          for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){

                            mpcw.room =childSnapshot.getValue(Room.class);


                                  mpcw.mDatabaseReference=childSnapshot.getRef();

                              mpcw.setmPurposeType((int)mpcw.room.purposeCode);
                              changeCollapsedViewVisibility((int)mpcw.room.purposeCode,null,VISIBLE);
                              mpcw.mDatabaseReference.getParent().removeEventListener(this);

                          }
                        }
                        else if(mCurrentCallType==CALL_TYPE_INCOMING){

                        DatabaseReference ref = mpcw.mDatabaseReference.getRoot().child(getString(R.string.membership_check_ref)).child(phonenumber);

                        ref.keepSynced(true);

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                   changeCollapsedViewVisibility(PURPOSE_CHOOSER,null,VISIBLE);


                                } else {
                                    changeCollapsedViewVisibility(PURPOSE_TEXT_MESSAGE,getString(R.string.not_on_pc_invite,"receiving a call from"),VISIBLE);
                                }
                                mpcw.mDatabaseReference.getParent().removeEventListener(this);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }



                        }
                    catch (Exception e){
                        Log.e("e",e.getMessage());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

        }
        mpcw.mDatabaseReference.orderByKey().limitToLast(1).addValueEventListener(mValueEventListener);
    }

    public void dettachValueEventListener(){
        if(mValueEventListener!=null){
            mpcw.mDatabaseReference.removeEventListener(mValueEventListener);
           mValueEventListener=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();



        if (mpcw != null && mpcw.getWindowToken()!=null)
        {
            dettachValueEventListener();
            mWindowManager.removeView(mpcw);
        }
        if (mpcw.removeFloatingWidgetView != null && mpcw.removeFloatingWidgetView.getWindowToken()!=null){
            mWindowManager.removeView(mpcw.removeFloatingWidgetView);
        }



    }

    public class LocalBinder extends Binder {
        PhoneCallService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PhoneCallService.this;
        }
    }


}