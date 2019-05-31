package com.purposecaller.purposecaller.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.activities.ContactsActivity;
import com.purposecaller.purposecaller.models.Contact;

import java.util.List;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_INVITE_FRIEND;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_LIVE_ROOM;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NEW_CONTACT;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_NORMAL;
import static com.purposecaller.purposecaller.models.Contact.CONTACT_TYPE_UNKNOWN;

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mValues;
      public ContactsActivity.OnContactClickedListener mContactClickedListener;
    StorageReference profileImageRef= FirebaseStorage.getInstance().getReference().child("Users");
     public static float density=getApplicationContext().getResources().getDisplayMetrics().density;
    RequestOptions requestOptions=new RequestOptions().circleCrop().placeholder(R.drawable.user).fallback(R.drawable.user);
    int padding=(int)(density*6);
    public ContactsRecyclerViewAdapter(ContactsActivity.OnContactClickedListener onContactClickedListener, List<Contact> contacts) {
        this.mContactClickedListener=onContactClickedListener;
        this.mValues=contacts;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder=new ViewHolder( LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false));
        setOnClickListener(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mContact = mValues.get(position);
        holder.contactNameTextView.setText(mValues.get(position).name);

        if(holder.mContact.contactType==CONTACT_TYPE_NORMAL){
            if(holder.mContact.photoUrl!=null){
                Glide.with(holder.imageView).load(profileImageRef.child(holder.mContact.uid).child("profile_image"))
                        .apply(requestOptions)
                        .into(holder.imageView);
            }

        }
        else if(holder.mContact.contactType==CONTACT_TYPE_INVITE_FRIEND){
            setBackground(holder.imageView);
            Glide.with(holder.imageView).load(R.drawable.ic_share_black_24dp)
                    .apply(new RequestOptions().circleCrop()).into(holder.imageView);


        }
        else if(holder.mContact.contactType==CONTACT_TYPE_NEW_CONTACT){
          setBackground(holder.imageView);
            Glide.with(holder.imageView).load(R.drawable.ic_person_add_black_24dp)
                    .apply(new RequestOptions().circleCrop()).into(holder.imageView);

        }
        else if(holder.mContact.contactType==CONTACT_TYPE_UNKNOWN){
            setBackground(holder.imageView);
            Glide.with(holder.imageView).load(R.drawable.ic_search_black_24dp)
                    .apply(new RequestOptions().circleCrop()).into(holder.imageView);

        }
        else if(holder.mContact.contactType==CONTACT_TYPE_LIVE_ROOM){
            setBackground(holder.imageView);
            Glide.with(holder.imageView).load(R.drawable.ic_live_tv_black_24dp)
                    .apply(new RequestOptions().circleCrop()).into(holder.imageView);
        }

    }

    public void setBackground(ImageView iv){
        iv.setBackgroundResource(R.drawable.contact_circle_shape);

        iv.setPadding(padding,padding,padding,padding);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView contactNameTextView;
        public Contact mContact;

        public ViewHolder(View view) {
            super(view);
            mView = view;
           imageView =  view.findViewById(R.id.profile_thumbnail);
           contactNameTextView =  view.findViewById(R.id.displayname);
        }

    }


    public void setOnClickListener(final ViewHolder viewHolder){
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact clickedContact = viewHolder.mContact;
                if (mContactClickedListener != null)
                    mContactClickedListener.onContactClicked(clickedContact);

            }
        });
    }





}
