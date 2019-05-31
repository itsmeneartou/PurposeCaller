package com.purposecaller.purposecaller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.utils.SIMCallUtils;

import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;
import static com.purposecaller.purposecaller.adapters.ContactsRecyclerViewAdapter.density;

@Keep
public class PurposeMessageDialog extends AlertDialog.Builder{

    public Contact contact;
    public Activity activity;
   public PurposeMessageDialog(@NonNull Context context,Contact contact) {
        super(context);
       this.contact=contact;
      activity=(Activity) context;
    }

    public void showPurposeMessageDialog(){

        final EditText input = new EditText(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        int margin=(int)(density*16);
         lp.setMargins(margin,margin,margin,margin);
        input.setLayoutParams(lp);
        setView(input);
   setTitle("Type your message");
   setPositiveButton("Call",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                     String message = input.getText().toString();
                        Bundle args=new Bundle();
                        args.putParcelable("contact",contact);
                        args.putInt("purposeCode",PURPOSE_TEXT_MESSAGE);
                        args.putString("message",message);

                        new SIMCallUtils(getContext()).startCall(args);
                    }
                });

     setNegativeButton("Dismiss",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

       show();
    }


}
