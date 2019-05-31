package com.purposecaller.purposecaller.roommanagers.publicrooms;

import android.support.annotation.Keep;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.purposecaller.purposecaller.activities.WaitingActivity;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Player;
import com.purposecaller.purposecaller.models.Room;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.models.Room.JOINED_ROOM_STATE;
import static com.purposecaller.purposecaller.models.Room.OPEN_ROOM_STATE;
import static com.purposecaller.purposecaller.models.Room.ROOM_CREATOR;
import static com.purposecaller.purposecaller.models.Room.ROOM_JOINER;
import static com.purposecaller.purposecaller.Constants.mCurrentPlayer;
import static com.purposecaller.purposecaller.Constants.mOpponent;
@Keep
public class JoinRoomListener implements ChildEventListener {

    public DatabaseReference roomsRef;
    public double purposeCode;
    public boolean isTransactionRunning;
    public WaitingActivity activity;
    public JoinRoomListener(WaitingActivity activity,double purposeCode) {
        this.purposeCode = purposeCode;
        this.activity=activity;
        roomsRef=FirebaseDatabase.getInstance().getReference().child("public_rooms").child(String.valueOf((int)purposeCode));
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
        if(!isTransactionRunning) {

            if (!dataSnapshot.child("players").child(uid).exists()) {
                roomsRef.child(dataSnapshot.getKey()).runTransaction(new Transaction.Handler() {

                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        isTransactionRunning = true;
                        Room room = mutableData.getValue(Room.class);
                        if (room != null && room.currentRoomState == OPEN_ROOM_STATE) {

                            room.currentRoomState = JOINED_ROOM_STATE;

                            room.players.put(uid, new Player(Player.JOINED, new Contact(null, phoneNumber, uid)));
                            mutableData.setValue(room);


                            return Transaction.success(mutableData);
                        } else if (room != null && room.currentRoomState == JOINED_ROOM_STATE) {
                            return Transaction.abort();
                        } else {
                            return Transaction.success(mutableData);
                        }
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        isTransactionRunning = false;
                        if (databaseError != null) {

                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        if (b) {
                            dataSnapshot.child("players").child(uid).child("currentConnectivityStatus").getRef().onDisconnect().setValue(Player.LEFT);
                            mCurrentPlayer=ROOM_JOINER;
                           mOpponent = ROOM_CREATOR;
                            activity.setResultAndFinish(dataSnapshot.getKey());

                        }
                    }
                });
            } else {
                dataSnapshot.child("players").getRef().addValueEventListener(activity.connectionValueEventListener);
            }
        }

    }


    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
