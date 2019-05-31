package com.purposecaller.purposecaller.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.fragments.ChatFragment;
import com.purposecaller.purposecaller.fragments.VideoChatFragment;
import com.purposecaller.purposecaller.fragments.VoiceFragment;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Player;
import com.purposecaller.purposecaller.models.Room;
import com.purposecaller.purposecaller.roommanagers.ConnectionValueEventListener;

import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_CHAT;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VIDEO_CALL;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VOIP_CALL;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_UNKNOWN;
import static com.purposecaller.purposecaller.utils.SIMCallUtils.getNumberComboRef;

public class PurposeActivity extends AppCompatActivity {
    TextView gameStateTextView;
    ChatFragment chatFragment;
    VoiceFragment voiceFragment;
    VideoChatFragment videoChatFragment;
    DatabaseReference currentRoomRef,gameDataRef,playersRef,rootRef= FirebaseDatabase.getInstance().getReference();

    public com.google.firebase.database.ValueEventListener gameDataValueEventListener;


    int callType;
    Room room;
    Contact contact;
    public String TAG=getClass().getName();

    public ConnectionValueEventListener connectionValueEventListener=new ConnectionValueEventListener(new ConnectionValueEventListener.OnPlayerConnectionChangedListener() {
        @Override
        public void onOpponentLeft() {
            playersRef.child(uid).child("currentConnectivityStatus").setValue(Player.LEFT).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    if(contact.contactType == CONTACT_TYPE_UNKNOWN){
                        setTextViewText("Your opponent left.");
                    }
                    else if(contact.contactType ==CONTACT_TYPE_NORMAL)
                    {
                        setTextViewText((contact.name!=null?contact.name:"Your friend ")
                                +" left.");
                    }

                }
            });
        }



        @Override
        public void onOpponentJoined() {

        }
    });

    public void setTextViewText(String text){
        if(gameStateTextView!=null){
            gameStateTextView.setText(text);
        }
    }

    public boolean isPrivateRoom(){
        return contact.contactType ==CONTACT_TYPE_NORMAL;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void doCommonPurposeWork(){
        contact=getIntent().getParcelableExtra("contact");
        Log.e("docommonpurpose",contact.name);
        room=getIntent().getParcelableExtra("room");
        currentRoomRef=getNumberComboRef(rootRef
                .child(isPrivateRoom()?"private_rooms":("public_rooms/"+(int)room.purposeCode)),contact)
                .child(room.roomName);
        callType=getIntent().getIntExtra("callType",-1);

        if(room.interactVia==INTERACT_VIA_CHAT){
            chatFragment=new ChatFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.interact_via_container,chatFragment).commit();
        }
        else if(room.interactVia==INTERACT_VIA_VOIP_CALL){
            voiceFragment=new VoiceFragment();
            Bundle bundle=new Bundle();
            bundle.putString("roomName",room.roomName);
            bundle.putParcelable("contact",contact);
            voiceFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.interact_via_container,voiceFragment).commit();
        }
        else if(room.interactVia==INTERACT_VIA_VIDEO_CALL){
            videoChatFragment=new VideoChatFragment();
            Bundle bundle=new Bundle();
            bundle.putString("roomName",room.roomName);
            bundle.putParcelable("contact",contact);
           videoChatFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.interact_via_container,videoChatFragment).commit();
        }
        else{
            findViewById(R.id.interact_via_container).setVisibility(View.GONE);
        }

        setRefs(room.roomName);
        playersRef.child(isPrivateRoom()?phoneNumber:uid).child("currentConnectivityStatus").onDisconnect().setValue(Player.LEFT);
        playersRef.addValueEventListener(connectionValueEventListener);
        if(gameDataValueEventListener!=null){
            gameDataRef.addValueEventListener(gameDataValueEventListener);
        }
        if(chatFragment!=null){

            chatFragment.setMessageDatabaseReference(currentRoomRef.child("messages"));
        }
    }

    @Override
    protected void onDestroy() {

        if(playersRef!=null ){
            if(connectionValueEventListener!=null)playersRef.removeEventListener(connectionValueEventListener);
           if(uid!=null) playersRef.child(uid).child("currentConnectivityStatus").setValue(Player.LEFT);

        }


        if(gameDataRef!=null &&gameDataValueEventListener!=null){
            gameDataRef.removeEventListener(gameDataValueEventListener);
        }
        super.onDestroy();

    }

    public void setRefs(String key){
        gameDataRef=currentRoomRef.child("GameData");
        playersRef=currentRoomRef.child("players");
    }









}
