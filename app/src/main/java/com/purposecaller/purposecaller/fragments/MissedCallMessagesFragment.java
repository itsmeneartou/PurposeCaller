package com.purposecaller.purposecaller.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.utils.PermissionUtils;

import static com.purposecaller.purposecaller.activities.MainActivity.REQUEST_ID_MULTIPLE_PERMISSIONS;


public class MissedCallMessagesFragment extends Fragment implements View.OnClickListener {

    EditText message;
    Button set;
    SharedPreferences pref;
    boolean isAutomaticMessageStarted;
    String currentMessage;

    public MissedCallMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_missed_call_messages, container, false);
        message = v.findViewById(R.id.not_pick_up_message);
        set = v.findViewById(R.id.set_auto_message_button);
        set.setOnClickListener(this);
        message.setOnClickListener(this);
        message.setText(currentMessage);
        if (isAutomaticMessageStarted) {
            set.setText("PAUSE");
        } else {
            set.setText("START");
        }
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                pref.edit().putString("auto_message", editable.toString()).apply();
            }
        });
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getActivity().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
        currentMessage = pref.getString("auto_message", "");
        isAutomaticMessageStarted = pref.getBoolean("auto_message_state", false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.set_auto_message_button) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                new PermissionUtils().checkAndRequestPermissions(getActivity(), REQUEST_ID_MULTIPLE_PERMISSIONS, this, android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.SEND_SMS);
            } else {
                toggleAutoSMS();
            }
        }

    }

    public void toggleAutoSMS() {
        if (isAutomaticMessageStarted) {
            pref.edit().putBoolean("auto_message_state", false).apply();
            isAutomaticMessageStarted = false;
            Toast.makeText(getContext(), "Automatic messages are disabled", Toast.LENGTH_SHORT).show();
            set.setText("START");
        } else {
            currentMessage = message.getText().toString();
            isAutomaticMessageStarted = true;
            Toast.makeText(getContext(), "Automatic messages are enabled", Toast.LENGTH_SHORT).show();
            pref.edit().putBoolean("auto_message_state", true).apply();
            set.setText("PAUSE");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) continue;
                else return;
            }
            toggleAutoSMS();
        }
    }

}
