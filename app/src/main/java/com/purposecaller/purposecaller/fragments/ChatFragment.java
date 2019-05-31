package com.purposecaller.purposecaller.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.adapters.MessageAdapter;
import com.purposecaller.purposecaller.models.Message;

import java.util.ArrayList;
import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.MyAuthStateListener.getPhotoUrl;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;


public class ChatFragment extends Fragment {
    private Button sendButton;
    private DatabaseReference messageDatabaseReference;
    RecyclerView recyclerView;
    private EditText editText;
    public MessageAdapter messageListAdapter;
    ChildEventListener mChildEventListener;

    private List<Message> messages;
    public String TAG="ChatFragment";
    String lastKey;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_chat,container,false);

        //get a reference to views in the layout

        recyclerView=  v.findViewById(R.id.messageRecyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        messages=new ArrayList<>();

        sendButton =  v.findViewById(R.id.sendButton);
        editText =  v.findViewById(R.id.messageEditText);

        sendButton.setEnabled(false);


        messageListAdapter = new MessageAdapter(getApplicationContext(),
                messages);


        recyclerView.setAdapter(messageListAdapter);


       editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                  sendButton.callOnClick();



                    return true;
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //if the text in EditText is not empty  enable the skip button else disable it
                if (charSequence.toString().trim().length() > 0 && messageDatabaseReference!=null && mChildEventListener!=null) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //onclick listener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Message message = new Message(editText.getText().toString(),uid,getPhotoUrl());

                messageDatabaseReference.push().setValue(message);
                editText.setText("");
            }
        });
        return v;

    }



    public void setMessageDatabaseReference(DatabaseReference messageDatabaseReference) {
        this.messageDatabaseReference = messageDatabaseReference;
        initChildEventListener();
        if(mChildEventListener!=null){

            messageDatabaseReference.limitToLast(50).addChildEventListener(mChildEventListener);
        }


    }

    public void sendAMessage(Message message){
        messageDatabaseReference.push().setValue(message);
    }

    public void  initChildEventListener(){
        mChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                int size=messages.size();
                messages.add(dataSnapshot.getValue(Message.class));
                messageListAdapter.notifyItemInserted(size);
                recyclerView.scrollToPosition(messages.size() - 1);
               if(s==null)lastKey=dataSnapshot.getKey();
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
        };
    }







}

