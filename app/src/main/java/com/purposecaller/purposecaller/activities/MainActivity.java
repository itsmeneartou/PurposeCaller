package com.purposecaller.purposecaller.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.purposecaller.purposecaller.MyAuthStateListener;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.fragments.AutomaticMessagesFragment;
import com.purposecaller.purposecaller.fragments.ProfileFragment;
import com.purposecaller.purposecaller.fragments.PurposesFragment;

import static com.purposecaller.purposecaller.MyApplication.mAuth;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MenuItem loginMenuItem;
    public static String uid;
    FirebaseAuth.AuthStateListener mAuthStateListener=new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
           mUser=firebaseAuth.getCurrentUser();
            if(mUser!=null){
                uid=mUser.getUid();
                phoneNumber=mUser.getPhoneNumber();
               TextView name=headerView.findViewById(R.id.name);
                TextView number=headerView.findViewById(R.id.phoneNumber);
                name.setText(mUser.getDisplayName()!=null?mUser.getDisplayName():"Name not set");
                number.setText(phoneNumber);
                ImageView imageView=headerView.findViewById(R.id.imageView);
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Log.e("instanceid",instanceIdResult.getToken());
                        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("token")
                                .setValue(instanceIdResult.getToken());
                    }
                });
                if(mUser.getPhotoUrl()!=null){

                   StorageReference ref= FirebaseStorage.getInstance().getReference().child("Users").child(uid).child("profile_image");
                    Glide.with(imageView).load(ref).apply(new RequestOptions()
                            .circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).fallback(R.drawable.user)).into(imageView);
                }
            }
        }
    };
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 5;
    View headerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView=navigationView.getHeaderView(0);
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new PurposesFragment(),null).disallowAddToBackStack().commit();
        navigationView.setCheckedItem(R.id.purpose_calling);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        loginMenuItem=   menu.getItem(0);

        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                if(uid!=null)AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) Toast.makeText(getApplicationContext(),"Successfully signed out.",Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getApplicationContext(),"Failed to sign out.",Toast.LENGTH_SHORT).show();
                    }
                });
                else MyAuthStateListener.login(this);
                return true;
            case R.id.rate_the_app:
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            case R.id.invite_your_friends:
                          inviteFriends(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public static void inviteFriends(Activity activity){
        String message = "Hey! Download the Purposecaller app and know why someone is calling you! Download it from: https://ma9c4.app.goo.gl/rniX";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        activity.startActivity(Intent.createChooser(share, "Invite Friends via..."));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id==R.id.profile){
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment(),null).disallowAddToBackStack().commit();
        }
       else if (id == R.id.purpose_calling) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new PurposesFragment(),null).disallowAddToBackStack().commit();
        } else if (id == R.id.auto_messages) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container,new AutomaticMessagesFragment(),null).disallowAddToBackStack().commit();
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(featureId == AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR && menu != null){
            if(loginMenuItem!=null) {
                if( uid!=null){
                    loginMenuItem.setTitle("Sign out");
                }
                else{
                    loginMenuItem.setTitle("Sign in");
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

}
