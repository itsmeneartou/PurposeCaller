package com.purposecaller.purposecaller.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.purposecaller.purposecaller.MyAuthStateListener;
import com.purposecaller.purposecaller.PurposeMessageDialog;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.adapters.ContactsRecyclerViewAdapter;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Room;
import com.purposecaller.purposecaller.utils.PermissionUtils;
import com.purposecaller.purposecaller.utils.SIMCallUtils;

import java.util.ArrayList;
import java.util.HashMap;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import static com.purposecaller.purposecaller.Constants.CALL_TYPE_OUTGOING;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_CHAT;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_SIM_CALL;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VIDEO_CALL;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_VOIP_CALL;
import static com.purposecaller.purposecaller.Constants.PURPOSE_BROWSING;
import static com.purposecaller.purposecaller.Constants.PURPOSE_LUDO;
import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.Constants.PURPOSE_QUIZ;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TIC_TAC_TOE;
import static com.purposecaller.purposecaller.Constants.RC_SIGN_IN;
import static com.purposecaller.purposecaller.Constants.RC_WAIT_FOR_PLAYER;
import static com.purposecaller.purposecaller.Constants.REQUEST_CODE_CONTACTS;
import static com.purposecaller.purposecaller.Constants.REQUEST_CODE_PHONE_STATE_OUTGOING_CALLS;
import static com.purposecaller.purposecaller.MyApplication.mAuth;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_INVITE_FRIEND;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_LIVE_ROOM;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NEW_CONTACT;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_UNKNOWN;

public class ContactsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>
     {
      public String TAG=getClass().getName();
         FirebaseAuth.AuthStateListener mAuthStateListener=new FirebaseAuth.AuthStateListener() {

             @Override
             public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 if(firebaseAuth.getCurrentUser()==null) {
                     mUser=null;
                     uid=null;
                     phoneNumber=null;
                 }
                 else{
                     mUser=firebaseAuth.getCurrentUser();
                     uid=mUser.getUid();
                     phoneNumber=mUser.getPhoneNumber();

                     FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                         @Override
                         public void onSuccess(InstanceIdResult instanceIdResult) {
                             FirebaseDatabase.getInstance().getReference("Users").child(uid).child("token")
                                     .setValue(instanceIdResult.getToken());
                         }
                     });


                 }



             }
         };


     DatabaseReference contactsRef;
    Contact mContact;
    RecyclerView mContactsList;
         ContactsRecyclerViewAdapter mAdapter;
    ArrayList<Contact> contacts;
         ProgressBar progressBar;
         HashMap<String,Boolean> map=new HashMap<>();
         String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
                 + ("1") + "'";
         String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                 + " ASC";
    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER

            };
    // The column index for the _ID column
    private static final int CONTACT_NAME_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int CONTACT_NUMBER_INDEX = 1;

         public int purposeCode;
         String quizCategory,videoId;
         Intent intentForPurpose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(savedInstanceState!=null){
            uid=savedInstanceState.getString("uid");
            phoneNumber=savedInstanceState.getString("phoneNumber");
        }
        contacts=new ArrayList<>();
        progressBar=findViewById(R.id.progress_bar);
        purposeCode=getIntent().getIntExtra("purposeCode",-1);

            contacts.add(new Contact("Invite Friend",null,CONTACT_TYPE_INVITE_FRIEND));
            contacts.add(new Contact("New Contact",null,CONTACT_TYPE_NEW_CONTACT));
            if(purposeCode==PURPOSE_MUTUAL_WATCH){
                contacts.add(new Contact("Live Room",null, CONTACT_TYPE_LIVE_ROOM));
            }
            else if(purposeCode==PURPOSE_TIC_TAC_TOE || purposeCode==PURPOSE_LUDO||purposeCode==PURPOSE_QUIZ){
                contacts.add(new Contact("Find a player",null, CONTACT_TYPE_UNKNOWN));
            }
                    if(purposeCode==PURPOSE_QUIZ){
                        quizCategory=getIntent().getStringExtra("quizCategory");
                    }
                   else if(purposeCode==PURPOSE_MUTUAL_WATCH){
                        videoId=getIntent().getStringExtra("videoId");
                    }

        mAdapter=new ContactsRecyclerViewAdapter(new OnContactClickedListener() {
            @Override
            public void onContactClicked(Contact contact) {
                 mContact=contact;

                if(contact.contactType!=null){
                    if(contact.contactType==CONTACT_TYPE_INVITE_FRIEND){
                        MainActivity.inviteFriends(ContactsActivity.this);
                    }
                    else if(contact.contactType==CONTACT_TYPE_NEW_CONTACT){
                        addNewContact();
                    }
                    else{
                        intentForPurpose=new Intent();
                        intentForPurpose.putExtra("contact",contact);
                        intentForPurpose.putExtra("callType",CALL_TYPE_OUTGOING);




                         if(purposeCode==PURPOSE_MUTUAL_WATCH){

                            intentForPurpose.setClass(ContactsActivity.this,WatchVideoActivity.class).putExtra("videoId",videoId);
                        }
                        else if(purposeCode==PURPOSE_BROWSING){
                            intentForPurpose.setClass(ContactsActivity.this,MultiBrowsingActivity.class);
                        }


                     if(purposeCode!=PURPOSE_TEXT_MESSAGE){
                         if( contact.contactType == CONTACT_TYPE_UNKNOWN||contact.contactType==CONTACT_TYPE_LIVE_ROOM){
                                 startActivity(intentForPurpose.putExtra("room",new Room(null,purposeCode,INTERACT_VIA_CHAT)));

                         }
                         else {
                             showCommunicationOptions(contact);
                         }

                     }
                     else if(new PermissionUtils().checkAndRequestPermissions(ContactsActivity.this,REQUEST_CODE_PHONE_STATE_OUTGOING_CALLS, Manifest.permission.READ_PHONE_STATE,
                                 Manifest.permission.PROCESS_OUTGOING_CALLS)){
                             new PurposeMessageDialog(ContactsActivity.this,contact).showPurposeMessageDialog();
                         }



                    }
                }


            }
        }, contacts);
        mContactsList = findViewById(R.id.list);

       mContactsList.setAdapter(mAdapter);
        if(mUser==null){
            mUser= FirebaseAuth.getInstance().getCurrentUser();
            if(mUser==null){
                MyAuthStateListener.login(this);
            }
            else{
                contactsRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("contacts");
            }
        }
      else if(new PermissionUtils().checkAndRequestPermissions(this,REQUEST_CODE_CONTACTS,Manifest.permission.READ_CONTACTS)){
            contactsRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("contacts");
            getSupportLoaderManager().initLoader(0, null, this);
        }
      addChildEventListener();


    }

         public void showCommunicationOptions( final Contact contact){
             AlertDialog.Builder b = new AlertDialog.Builder(this);
             b.setTitle("Interact via");
             final HashMap<String,Integer> interactVia=new HashMap<>();

             if(contact.contactType ==CONTACT_TYPE_NORMAL){
                 if(purposeCode!=PURPOSE_MUTUAL_WATCH){
                     interactVia.put("SIM Call",INTERACT_VIA_SIM_CALL);
                 }
                 interactVia.put("Text Messages",INTERACT_VIA_CHAT);
                 interactVia.put("VOIP Call",INTERACT_VIA_VOIP_CALL);
                 interactVia.put("Video Call",INTERACT_VIA_VIDEO_CALL);
             }

             final Bundle args=new Bundle();
             args.putParcelable("contact",contact);
             args.putInt("purposeCode",purposeCode);
             args.putString("videoId",videoId);
             final String[] array= interactVia.keySet().toArray(new String[interactVia.size()]);
             b.setItems(array, new DialogInterface.OnClickListener() {

                 @Override
                 public void onClick(DialogInterface dialog, int which) {

                     switch(interactVia.get(array[which])){
                         case INTERACT_VIA_SIM_CALL:
                             if(new PermissionUtils().checkAndRequestPermissions(ContactsActivity.this
                                     ,REQUEST_CODE_PHONE_STATE_OUTGOING_CALLS, Manifest.permission.READ_PHONE_STATE,
                                     Manifest.permission.PROCESS_OUTGOING_CALLS)){
                                 new SIMCallUtils(ContactsActivity.this).startCall(args);
                             }


                             break;
                         case INTERACT_VIA_CHAT:

                             startWaitingActivity(INTERACT_VIA_CHAT);
                             break;

                         case INTERACT_VIA_VOIP_CALL:
                             startWaitingActivity(INTERACT_VIA_VOIP_CALL);
                             break;
                         case INTERACT_VIA_VIDEO_CALL:
                             startWaitingActivity(INTERACT_VIA_VIDEO_CALL);

                     }

                     dialog.dismiss();
                 }

             });

             b.show();
         }


         public void startWaitingActivity(int interactVia){
             startActivityForResult(new Intent(this,WaitingActivity.class)
                     .putExtra("contact",mContact).putExtra("room",new Room(null,purposeCode,interactVia))
                     .putExtra("callType",CALL_TYPE_OUTGOING).putExtra("videoId",videoId)
                             .putExtra("quiz_category",quizCategory)
                     ,RC_WAIT_FOR_PLAYER);
         }

    public void addChildEventListener(){
        if(contactsRef!=null){
            contactsRef.keepSynced(true);
            contactsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    progressBar.setVisibility(View.GONE);
                    Contact contact=dataSnapshot.getValue(Contact.class);
                    if(contact!=null){
                        contact.contactType =CONTACT_TYPE_NORMAL;
                        contacts.add(contact);
                        mAdapter.notifyItemInserted(contacts.size()-1);
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    contacts.remove(dataSnapshot.getValue(Contact.class));
                    mAdapter.notifyItemInserted(contacts.size()-1);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }



         @Override
         protected void onActivityResult(int requestCode, int resultCode, Intent data) {
             if(requestCode==RC_SIGN_IN && resultCode==RESULT_OK){
                 if(mUser==null){
                     mUser=FirebaseAuth.getInstance().getCurrentUser();
                     uid=mUser.getUid();
                 }
                 contactsRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("contacts");
                 if(new PermissionUtils().checkAndRequestPermissions(this,REQUEST_CODE_CONTACTS,Manifest.permission.READ_CONTACTS)){

                     getSupportLoaderManager().initLoader(0, null, this);
                 }
             }

         }

         public void addNewContact(){


                 Intent intent = new Intent(Intent.ACTION_INSERT);
                 intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                startActivity(intent);


         }

         @Override
         public void onResume() {
             super.onResume();
             // add auth state listener to Firebase Auth
             mAuth.addAuthStateListener(mAuthStateListener);
         }

         // remove auth state listener
         @Override
         public void onPause() {
             super.onPause();
             // add auth state listener to Firebase Auth
             mAuth.removeAuthStateListener(mAuthStateListener);

         }

         @Override
         public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
             if(requestCode==REQUEST_CODE_CONTACTS && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                 contactsRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("contacts");
                 getSupportLoaderManager().initLoader(0, null, this);
                 addChildEventListener();
             }
             else if(requestCode==REQUEST_CODE_PHONE_STATE_OUTGOING_CALLS){
                   if(purposeCode==PURPOSE_TEXT_MESSAGE){
                       new PurposeMessageDialog(this,mContact).showPurposeMessageDialog();
                   }
             }
         }

         @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        // Starts the query
        return new CursorLoader(
               this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
              PROJECTION,
                selection
                        + " AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                null,
                sortOrder
        );
    }



    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        DatabaseReference mbaseRef= FirebaseDatabase.getInstance().getReference();
         String name,number;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(this);
        DatabaseReference membershipRef=mbaseRef.child("membership_check_2");
        while (cursor.moveToNext()) {
            try{
             name=cursor.getString(CONTACT_NAME_INDEX);
               number= phoneUtil.format(phoneUtil.parse(cursor.getString(CONTACT_NUMBER_INDEX),
                       getCountryCode()), PhoneNumberUtil.PhoneNumberFormat.E164);


                if(map.get(number)==null){
                    map.put(number,true);
                    membershipRef.child(number).addListenerForSingleValueEvent(new ValueEventListener() {
                      String name;
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            if(dataSnapshot.exists()){
                                Contact contact=dataSnapshot.getValue(Contact.class);
                                if(contact!=null){
                                    contact.name=name;
                                    contact.phoneNumber=dataSnapshot.getKey();
                                    contactsRef.child(contact.phoneNumber).setValue(contact);
                                    contact.contactType =CONTACT_TYPE_NORMAL;
                                }


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                        public ValueEventListener init(String name){
                            this.name=name;
                            return this;
                        }

                    }.init(name));
                }



            }
            catch(NumberParseException e){
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }




        }
    }
    public  String getCountryCode(){

        TelephonyManager telephonyMngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        return telephonyMngr.getSimCountryIso().toUpperCase();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public interface OnContactClickedListener{
        void onContactClicked(Contact contact);
    }


         @Override
         protected void onSaveInstanceState(Bundle outState) {
             super.onSaveInstanceState(outState);
             outState.putString("uid",uid);
             outState.putString("phoneNumber",phoneNumber);
         }
     }
