package com.purposecaller.purposecaller.fragments;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.purposecaller.purposecaller.MyAuthStateListener;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.utils.PermissionUtils;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.Constants.RC_CHOOSE_IMAGE;
import static com.purposecaller.purposecaller.Constants.RC_WRITE_EXTERNAL_STORAGE;
import static com.purposecaller.purposecaller.MyApplication.mAuth;
import static com.purposecaller.purposecaller.MyApplication.mUser;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;


public class ProfileFragment extends Fragment implements View.OnClickListener{
    EditText name, phoneNumberEditText;
    ImageView profileImage;
    ProgressBar progressBar;
    StorageReference ref;
    boolean isImageBeingUploaded;
    String mPhotoUrl;
    public String TAG=getClass().getName();
    public FirebaseAuth.AuthStateListener mAuthStateListener=new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mUser=firebaseAuth.getCurrentUser();
            if(mUser==null){
                MyAuthStateListener.login(ProfileFragment.this);
            }
            else{
                setEditTextValues();
            }
        }
    };

    public OnCompleteListener onCompleteListener= new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {

                FirebaseDatabase.getInstance().getReference().child("membership_check_2").child(phoneNumber)
                        .child("photoUrl").setValue(mPhotoUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(profileImage, "Profile updated successfully.", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
            else{
                Snackbar.make(profileImage, "Failed to update Profile.", Snackbar.LENGTH_SHORT).show();


            }
        }
    };
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v= inflater.inflate(R.layout.fragment_profile, container, false);
        name=v.findViewById(R.id.display_name);
         profileImage=v.findViewById(R.id.profile_photo);
        phoneNumberEditText =v.findViewById(R.id.phoneNumber);
        progressBar=v.findViewById(R.id.progress_bar);
          v.findViewById(R.id.save).setOnClickListener(this);
          v.findViewById(R.id.changeProfilePhoto).setOnClickListener(this);
       setEditTextValues();



        return v;
    }

    public void dispatchChoosePictureIntent() {
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);


        if (intent.resolveActivity(getContext().getPackageManager()) != null) {

            startActivityForResult(Intent.createChooser(intent, "Select an image"),RC_CHOOSE_IMAGE);

        }

    }

    @Override
    public void onResume() {
       super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    public void setEditTextValues(){
        if(mUser!=null){

          String displayName=mUser.getDisplayName();
            Uri photoUrl=mUser.getPhotoUrl();

            if(phoneNumberEditText !=null && phoneNumberEditText !=null) phoneNumberEditText.setText(phoneNumber);

            if(displayName!=null && name!=null)name.setText(displayName);

            if(photoUrl!=null){

                ref= FirebaseStorage.getInstance().getReference().child("Users").child(uid).child("profile_image");
                Glide.with(profileImage).load(ref).apply(new RequestOptions()
                        .circleCrop().placeholder(R.drawable.user).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).fallback(R.drawable.user)).into(new ImageViewTarget<Drawable>(profileImage) {
                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        profileImage.setImageDrawable(resource);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if(!isImageBeingUploaded){
                    UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
                    if(name.getText().length()>0)profileUpdates.setDisplayName(name.getText().toString());


                    mUser.updateProfile( profileUpdates.build())
                            .addOnCompleteListener(onCompleteListener);
                }
                else {  Snackbar.make(profileImage, "Please wait while image is being uploaded.", Snackbar.LENGTH_SHORT).show();}

                break;
            case R.id.changeProfilePhoto:

                if(new PermissionUtils().checkAndRequestPermissions(getActivity(),RC_WRITE_EXTERNAL_STORAGE
                        ,this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                dispatchChoosePictureIntent();
            }
        }


    }

    public void onImageAdded(Uri uri) {
        Glide.with(getApplicationContext()).asBitmap().apply(new RequestOptions().circleCrop()).load(uri).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                profileImage.setImageDrawable(new BitmapDrawable(bitmap));
                byte[] data = baos.toByteArray();
               isImageBeingUploaded=true;
               ref= FirebaseStorage.getInstance().getReference().child("Users").child(uid).child("profile_image");
               ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                isImageBeingUploaded=false;
                               taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Uri> task) {
                                       if(task.isSuccessful()){
                                           mPhotoUrl=task.getResult().toString();
                                           UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
                                           mUser.updateProfile(profileUpdates.setPhotoUri(task.getResult()).build())
                                                   .addOnCompleteListener(onCompleteListener);
                                       }
                                   }
                               });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isImageBeingUploaded=false;
                        StorageException e2=  ((StorageException) e);
                        Snackbar.make(profileImage, "Failed to upload profile photo.", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==RC_CHOOSE_IMAGE && resultCode==RESULT_OK){
                onImageAdded(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(grantResults[0]== PackageManager.PERMISSION_GRANTED && requestCode==RC_WRITE_EXTERNAL_STORAGE) {
            dispatchChoosePictureIntent();

        }
    }
}
