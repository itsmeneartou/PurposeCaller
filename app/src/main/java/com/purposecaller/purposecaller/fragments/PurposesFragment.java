package com.purposecaller.purposecaller.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.activities.ChooseVideoActivity;
import com.purposecaller.purposecaller.activities.ContactsActivity;
import com.purposecaller.purposecaller.adapters.PurposesRecyclerViewAdapter;

import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.Constants.PURPOSE_QUIZ;
import static com.purposecaller.purposecaller.Constants.REQUEST_CODE_CHOOSE_VIDEO;
import static com.purposecaller.purposecaller.Constants.RESULT_CODE_VIDEO_CHOSEN;


public class PurposesFragment extends Fragment {

    // TODO: Customize parameters
    private int mColumnCount = 2;

     public Context mContext;

    public PurposesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new PurposesRecyclerViewAdapter(getContext(), new PurposesRecyclerViewAdapter.OnPurposeClickedListener() {
                @Override
                public void onPurposeClicked(int purposeCode) {

                    if(purposeCode==PURPOSE_QUIZ){
                        showQuizCategories(purposeCode);

                    }
                    else if(purposeCode==PURPOSE_MUTUAL_WATCH){
                       startActivityForResult(new Intent(getContext(), ChooseVideoActivity.class),REQUEST_CODE_CHOOSE_VIDEO);
                    }
                    else{
                    startActivity(new Intent(mContext, ContactsActivity.class).putExtra("purposeCode",purposeCode));
                    }
                }
            }));
        }
        return view;
    }

    public void showQuizCategories(final int purposeCode){

        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
        b.setTitle("Quiz category");
        final String[] categories=getResources().getStringArray(R.array.topics);
        b.setItems(categories , new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                startActivity(new Intent(mContext, ContactsActivity.class).putExtra("purposeCode",purposeCode)
                        .putExtra("quizCategory",categories[which]));
                dialog.dismiss();
            }

        });

        b.show();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CODE_CHOOSE_VIDEO && resultCode==RESULT_CODE_VIDEO_CHOSEN && data!=null){
            startActivity(new Intent(mContext,ContactsActivity.class).putExtra("purposeCode",PURPOSE_MUTUAL_WATCH)
                    .putExtra("videoId",data.getStringExtra("videoId")));
        }
    }
}
