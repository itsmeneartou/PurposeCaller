package com.purposecaller.purposecaller.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.purposecaller.purposecaller.PhoneCallService;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.activities.MultiBrowsingActivity;
import com.purposecaller.purposecaller.activities.WaitingActivity;
import com.purposecaller.purposecaller.activities.WatchVideoActivity;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Room;

import java.util.Map;

import static com.purposecaller.purposecaller.Constants.CALL_TYPE_INCOMING;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_CHAT;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_SIM_CALL;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VIDEO_CALL;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VOIP_CALL;
import static com.purposecaller.purposecaller.Constants.PURPOSE_BROWSING;
import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

     private String TAG=getClass().getName();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {




                Map<String,String> map=remoteMessage.getData();
           Bundle  bundle = getBundleFromMap(map);
        int interactVia=Integer.parseInt(map.get("interactVia"));

                 if(interactVia==INTERACT_VIA_CHAT||interactVia==INTERACT_VIA_VOIP_CALL||interactVia==INTERACT_VIA_VIDEO_CALL){
                     showNotification(bundle);
                 }
                 else if(interactVia==INTERACT_VIA_SIM_CALL) {
                     startService(new Intent(this, PhoneCallService.class).putExtra("contact", bundle.getParcelable("contact"))
                             .putExtra("room", bundle.getParcelable("room"))
                             .putExtra("callType", CALL_TYPE_INCOMING));
                 }





    }
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return phoneNumber;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onNewToken(String s) {
        if(currentUser!=null){
            FirebaseDatabase.getInstance().getReference("Users").child(uid).child("token")
                    .setValue(s);
        }
    }

    public Bundle getBundleFromMap(Map<String,String> map){

        String receiverUid = map.get("receiverUid");
        String creatorUid = map.get("creatorUid");
        String creatorPhoneNumber = map.get("creatorPhoneNumber");
        Log.e("creatorPhoneNumber",creatorPhoneNumber);
        String roomName=map.get("roomName");
        int interactVia=Integer.parseInt(map.get("interactVia"));
        int purposeCode = Integer.parseInt(map.get("purposeCode"));
        Bundle bundle=new Bundle();
        bundle.putParcelable("contact",new Contact(getContactName(getApplicationContext(),creatorPhoneNumber),creatorPhoneNumber,creatorUid,CONTACT_TYPE_NORMAL));
        bundle.putParcelable("room",new Room(roomName,purposeCode,interactVia));
        bundle.putInt("callType",CALL_TYPE_INCOMING);
        bundle.putString("receiverUid",receiverUid);

        return bundle;
    }

    public void showNotification(Bundle bundle){

           Contact contact=bundle.getParcelable("contact");
           Room room=bundle.getParcelable("room");
        if (currentUser != null && bundle.getString("receiverUid").equals(currentUser.getUid()) ) {
            uid=currentUser.getUid();
            NotificationManager nManager = (NotificationManager) getApplication().getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder ntbuilder = new NotificationCompat.Builder(getApplicationContext());
            Intent intent = new Intent(this, WaitingActivity.class);
            intent.putExtra("contact",contact)
                    .putExtra("room",room)
                    .putExtra("callType",CALL_TYPE_INCOMING);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            int color = 0xff2196f3;
            nManager.notify(1, ntbuilder.setContentTitle("Purpose caller").setSound(RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.drawable.ic_call_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).setColor(color).setContentText("You have received a new message.").build());

        }

    }

    public static Class<?> getPurposeClassName(int purposeCode){
        switch (purposeCode){
            case PURPOSE_BROWSING:return MultiBrowsingActivity.class;
            case PURPOSE_MUTUAL_WATCH:return WatchVideoActivity.class;
        }
        return null;
    }





}
