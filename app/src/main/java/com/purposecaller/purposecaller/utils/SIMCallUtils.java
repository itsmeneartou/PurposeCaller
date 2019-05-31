package com.purposecaller.purposecaller.utils;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.purposecaller.purposecaller.activities.WaitingActivity;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Message;
import com.purposecaller.purposecaller.models.Player;
import com.purposecaller.purposecaller.models.Room;

import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_SIM_CALL;
import static com.purposecaller.purposecaller.Constants.PURPOSE_QUIZ;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.MyAuthStateListener.getPhotoUrl;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;

public class SIMCallUtils {

    public int purposeCode;
    public Contact contact;
    public Context mContext;
    public SIMCallUtils(Context context) {
        this.mContext=context;
    }


    public  void writeToPreferences(String key, Object value){
        SharedPreferences.Editor editor= mContext.getSharedPreferences("my_pref",Context.MODE_PRIVATE).edit();

        if(value instanceof Integer){
            editor.putInt(key,((Integer) value)).apply();
        }
        else if(value instanceof String){
            editor.putString(key, ((String) value)).apply();
        }




    }


    public  void writePrivateRoomToDatabase(final Bundle args, final boolean shouldCall){
        contact=args.getParcelable("contact");


      final  DatabaseReference roomsRef=getNumberComboRef(FirebaseDatabase.getInstance().getReference().child("private_rooms"),contact);
        final String key=roomsRef.push().getKey();
        Room room =new Room(key,purposeCode,INTERACT_VIA_SIM_CALL);
        room.players.put(phoneNumber,new Player(Player.JOINED,new Contact(null, phoneNumber,uid)));
        room.players.put(contact.phoneNumber,new Player(Player.NOT_JOINED,contact));
       WaitingActivity.setGameData(room,args.getString("videoId"));
        if(purposeCode==PURPOSE_TEXT_MESSAGE){
            String message=args.getString("message");
            room.GameData=new Message(message,uid,getPhotoUrl());
        }

        roomsRef.child(key).setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                roomsRef.child(key).child("players").child(phoneNumber).child("currentConnectivityStatus").onDisconnect().setValue(Player.LEFT);
                writeToPreferences("roomKey_"+contact.phoneNumber,key);
                writeToPreferences("videoId",args.getString("videoId"));
                   if(shouldCall){
                       Intent i=new Intent();
                       i.setAction(Intent.ACTION_CALL);
                       i.setData(Uri.parse("tel:" + contact.phoneNumber));
                       mContext.startActivity(i);
                   }



            }
        });
    }

    public static DatabaseReference getNumberComboRef(DatabaseReference roomsRef,Contact contact){
        if(mUser!=null && contact!=null && contact.phoneNumber!=null){
            try{
                long p1=Long.parseLong(phoneNumber.substring(1));
                long p2=Long.parseLong(contact.phoneNumber.substring(1));
                if(p1>p2)return roomsRef.child(""+p1+p2);
                else return roomsRef.child(""+p2+p1);

            }
            catch(Exception e){

            }
        }
        return roomsRef;
    }



    public void startCall(Bundle args){
        purposeCode=args.getInt("purposeCode");
        writeToPreferences("purposeCode",purposeCode);
        if(purposeCode==PURPOSE_QUIZ){
            writeToPreferences("quiz_tag",args.getString("quiz_category"));
        }
        writePrivateRoomToDatabase(args,true);

    }









}
