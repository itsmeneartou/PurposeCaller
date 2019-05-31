package com.purposecaller.purposecaller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.WaveDrawable;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Player;
import com.purposecaller.purposecaller.models.Room;
import com.purposecaller.purposecaller.multibrowsing.MultiBrowsingData;
import com.purposecaller.purposecaller.mutualwatch.MutualWatchData;
import com.purposecaller.purposecaller.roommanagers.ConnectionValueEventListener;
import com.purposecaller.purposecaller.roommanagers.publicrooms.JoinRoomListener;
import com.purposecaller.purposecaller.services.MyFirebaseMessagingService;

import static com.purposecaller.purposecaller.Constants.CALL_TYPE_INCOMING;
import static com.purposecaller.purposecaller.Constants.CALL_TYPE_OUTGOING;
import static com.purposecaller.purposecaller.Constants.PURPOSE_BROWSING;
import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.Constants.mCurrentPlayer;
import static com.purposecaller.purposecaller.Constants.mOpponent;
import static com.purposecaller.purposecaller.MyApplication.mAuth;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.adapters.ContactsRecyclerViewAdapter.density;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_UNKNOWN;
import static com.purposecaller.purposecaller.models.Room.OPEN_ROOM_STATE;
import static com.purposecaller.purposecaller.models.Room.ROOM_CREATOR;
import static com.purposecaller.purposecaller.models.Room.ROOM_JOINER;
import static com.purposecaller.purposecaller.utils.SIMCallUtils.getNumberComboRef;

public class WaitingActivity extends AppCompatActivity {
    private WaveDrawable waveDrawable;

  public Room room;

    DatabaseReference roomsRef,playersRef,rootRef= FirebaseDatabase.getInstance().getReference();

    public ValueEventListener checkRoomAvailabilityListener=new ValueEventListener() {
        @Override
        public void onDataChange(final DataSnapshot snapshot) {
            if (!snapshot.exists()) {
                mCurrentPlayer =ROOM_CREATOR;
                mOpponent = ROOM_JOINER;
               room.roomName = roomsRef.push().getKey();
                playersRef= roomsRef.child(room.roomName).child("players");

                room.players.put(uid, new Player(Player.JOINED, new Contact(null, phoneNumber, uid)));
               setGameData(room,getIntent().getStringExtra("videoId"));

                roomsRef.child(room.roomName).setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                              playersRef.child(uid).child("currentConnectivityStatus").onDisconnect().setValue(Player.LEFT);
                                playersRef.addValueEventListener(connectionValueEventListener);

                    }
                });


            }
            else{
                roomsRef.orderByChild("currentRoomState").equalTo(OPEN_ROOM_STATE)
                        .limitToFirst(1)
                        .addChildEventListener(joinRoomListener);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
        }
    };


    public ConnectionValueEventListener connectionValueEventListener=new ConnectionValueEventListener(
            new ConnectionValueEventListener.OnPlayerConnectionChangedListener() {
        @Override
        public void onOpponentLeft() {

        }



        @Override
        public void onOpponentJoined() {
            setResultAndFinish(room.roomName);
        }
    });
    int callType;
    JoinRoomListener joinRoomListener;
    TextView textView;
    Contact contact;
    public String TAG=getClass().getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        ImageView imageView=findViewById(R.id.image);

        textView=findViewById(R.id.text_view);

        if(mUser==null){
            mUser=mAuth.getCurrentUser();
            if(mUser!=null){
                uid=mUser.getUid();
                phoneNumber=mUser.getPhoneNumber();
            }
        }

        int dp=(int)(56*density);

        contact=getIntent().getParcelableExtra("contact");

        room =getIntent().getParcelableExtra("room");

        roomsRef=getNumberComboRef(rootRef
                .child(contact.contactType ==CONTACT_TYPE_NORMAL?"private_rooms":("public_rooms/"+(int)room.purposeCode)),contact);

        callType=getIntent().getIntExtra("callType",-1);

        joinRoomListener=new JoinRoomListener(this,room.purposeCode);

            StorageReference profileImageRef= FirebaseStorage.getInstance().getReference().child("Users");
        if(contact.uid!=null){
            Glide.with(imageView).load(profileImageRef.child(contact.uid).child("profile_image"))
                    .apply(new RequestOptions().circleCrop().override(dp,dp)
                            .placeholder(R.drawable.user).fallback(R.drawable.user))
                    .into(imageView);
        }





            waveDrawable = new WaveDrawable(ContextCompat.getColor(this,R.color.greyish),500);

        waveDrawable.setWaveInterpolator(new LinearInterpolator(this,null));

        waveDrawable.startAnimation();

        imageView.setBackground(waveDrawable);

        setRoomManagerListener();

    }

    public void setRoomManagerListener(){

        if(contact.contactType == CONTACT_TYPE_UNKNOWN){

            roomsRef.orderByChild("currentRoomState").equalTo(OPEN_ROOM_STATE).limitToFirst(1)
                    .addListenerForSingleValueEvent(checkRoomAvailabilityListener);


        }
        else if(contact.contactType ==CONTACT_TYPE_NORMAL ){

            if(callType==CALL_TYPE_OUTGOING){

                writePrivateRoomToDatabase();

            }
            else if(callType==CALL_TYPE_INCOMING){
                    textView.setText("Joining room.");
                 mCurrentPlayer=ROOM_JOINER;
                  mOpponent=ROOM_CREATOR;
                    roomsRef.child(room.roomName).child("players").child(phoneNumber).child("currentConnectivityStatus").setValue(Player.JOINED)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                         setResultAndFinish(room.roomName);
                        }
                    });

                }




        }


    }

    public   void writePrivateRoomToDatabase(){
        mCurrentPlayer=ROOM_CREATOR;
        mOpponent=ROOM_JOINER;
        textView.setText(getString(R.string.creating_a_room_with,contact.name));
        room.roomName=roomsRef.push().getKey();
        setGameData(room,getIntent().getStringExtra("videoId"));


        room.players.put(phoneNumber,new Player(Player.JOINED,new Contact(null, phoneNumber,uid)));
        room.players.put(contact.phoneNumber,new Player(Player.NOT_JOINED,contact));

        roomsRef.child(room.roomName).setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                textView.setText(getString(R.string.room_created_waiting_for,contact.name));
                roomsRef.child(room.roomName).child("players").addValueEventListener(connectionValueEventListener);

            }
        });
    }

    public static Room setGameData(Room room,@Nullable String videoId){
         switch ((int)room.purposeCode){
           //  case PURPOSE_TIC_TAC_TOE:room.GameData=new TicTacToeData(ROOM_JOINER);
             //    break;
             case PURPOSE_BROWSING:room.GameData=new MultiBrowsingData("https://www.google.com",uid);
                 break;
             case PURPOSE_MUTUAL_WATCH:

                 if(videoId!=null){

                     room.GameData=new MutualWatchData(videoId,uid);
                 }

         }
         return room;
    }

    public void setResultAndFinish(String key){
        if(room.roomName==null)room.roomName=key;
        finish();
        startActivity(new Intent(this,MyFirebaseMessagingService.getPurposeClassName((int)room.purposeCode)).putExtra("contact",contact)
                .putExtra("room",room));
    }

    @Override
   public void onBackPressed(){

        if(playersRef!=null){
            playersRef.child(uid).child("currentConnectivityStatus").setValue(Player.LEFT);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {


        if(room.roomName!=null &&connectionValueEventListener!=null){
            roomsRef.child(room.roomName).child("players").removeEventListener(connectionValueEventListener);
        }
        if(joinRoomListener!=null)roomsRef.removeEventListener(joinRoomListener);
        super.onDestroy();

    }
}
