package com.purposecaller.purposecaller.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private String token;



    @Override
    public void onTokenRefresh() {
        token = FirebaseInstanceId.getInstance().getToken();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("token");
           databaseReference.setValue(token);


        }

    }


}
