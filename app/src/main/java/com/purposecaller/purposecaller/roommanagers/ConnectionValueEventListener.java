package com.purposecaller.purposecaller.roommanagers;

import android.support.annotation.Keep;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.purposecaller.purposecaller.models.Player;

import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.MyApplication.uid;

@Keep
public class ConnectionValueEventListener implements ValueEventListener{

    public OnPlayerConnectionChangedListener listener;
    long   playerCount;
    public ConnectionValueEventListener(OnPlayerConnectionChangedListener onPlayerConnectionChangedListener){
        this.listener =onPlayerConnectionChangedListener;
    }
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        playerCount=dataSnapshot.getChildrenCount();
         int playersJoined=0;
         if(playerCount>=2){
            for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                if(!snapshot.getKey().equals(phoneNumber) && !snapshot.getKey().equals(uid)){
                    Player player= snapshot.getValue(Player.class);
                    if(player!=null && player.currentConnectivityStatus==Player.LEFT){
                       if(listener!=null)listener.onOpponentLeft();
                    }
                    else if(player!=null && player.currentConnectivityStatus==Player.JOINED){
                        playersJoined++;

                    }

                }
                else if(snapshot.getKey().equals(phoneNumber) || snapshot.getKey().equals(uid)){
                    playersJoined++;
                }
            }
            if(playersJoined>0 && playersJoined==playerCount && listener!=null){

                    listener.onOpponentJoined();

            }

        }

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    public interface OnPlayerConnectionChangedListener{
        void onOpponentLeft();

        void onOpponentJoined();
    }
}
