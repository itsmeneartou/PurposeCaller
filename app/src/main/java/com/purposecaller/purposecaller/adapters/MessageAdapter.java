package com.purposecaller.purposecaller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.models.Message;

import java.util.List;

import static com.purposecaller.purposecaller.activities.MainActivity.uid;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
Context mContext;
   List<Message> mMessages;
StorageReference profileImageRef=FirebaseStorage.getInstance().getReference().child("Users");
RequestOptions requestOptions=new RequestOptions().circleCrop().placeholder(R.drawable.user).fallback(R.drawable.user);

    public MessageAdapter(Context mContext, List<Message> mMessages) {
        this.mContext=mContext;
      this.mMessages=mMessages;

    }



    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

      final  Message current=mMessages.get(position);



            holder.messageTextView.setVisibility(View.VISIBLE);
            String textString =current.text;
            if (textString != null) {
                holder.messageTextView.setText(textString);
            }


            if (uid.equals(current.uid)) {

                ((LinearLayout) holder.mView).setGravity(Gravity.RIGHT);
                holder.mView.setPadding(200, 0, 0, 0);

            } else  {
                ((LinearLayout) holder.mView).setGravity(Gravity.LEFT);
                holder.mView.setPadding(0, 0, 200, 0);
            }

             Glide.with(holder.profileImageView).load(profileImageRef.child(current.uid).child("profile_image"))
                     .apply(requestOptions)
                     .into(holder.profileImageView);





    }

    @Override
    public int getItemCount() {

        return mMessages.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
          TextView messageTextView;
        ImageView profileImageView;
        View mView;
        public MessageViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            profileImageView=mView.findViewById(R.id.profile_photo);

            messageTextView=mView.findViewById(R.id.messageTextView);

        }
    }
}
