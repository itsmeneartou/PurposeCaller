package com.purposecaller.purposecaller.adapters;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.purposecaller.purposecaller.R;

import java.util.ArrayList;
import java.util.List;

import static com.purposecaller.purposecaller.Constants.PURPOSE_BROWSING;
import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;


public class PurposesRecyclerViewAdapter extends RecyclerView.Adapter<PurposesRecyclerViewAdapter.ViewHolder> {

    private final List<Purpose> mValues;
    private OnPurposeClickedListener onPurposeClickedListener;
    private Context mContext;
    public PurposesRecyclerViewAdapter(Context context,OnPurposeClickedListener onPurposeClickedListener) {
        this.mContext=context;
        this.onPurposeClickedListener=onPurposeClickedListener;
        mValues=new ArrayList<>();
        mValues.add(new Purpose("Browse with Friend","browsing",PURPOSE_BROWSING));
        mValues.add(new Purpose("Purpose Call","purpose_call",PURPOSE_TEXT_MESSAGE));
        mValues.add(new Purpose("Watch videos together","mutual_watch",PURPOSE_MUTUAL_WATCH));

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder=new ViewHolder( LayoutInflater.from(parent.getContext())
                .inflate(R.layout.purpose_item_layout, parent, false));
        setOnClickListener(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPurpose = mValues.get(position);
        Glide.with(mContext).load(mContext.getResources().getIdentifier(mValues.get(position).resourceString, "drawable",
                mContext.getPackageName())).into(holder.imageView);

        holder.purposeTextView.setText(mValues.get(position).purpose);


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView purposeTextView;
        public Purpose mPurpose;

        public ViewHolder(View view) {
            super(view);
            mView = view;
           imageView =  view.findViewById(R.id.purpose_image_view);
            purposeTextView =  view.findViewById(R.id.purpose_text);
        }

    }
    @Keep
    public class Purpose{
        public String purpose,resourceString;
        public int purposeCode;

        public Purpose(String purpose, String resourceString,int purposeCode) {
            this.purpose = purpose;
            this.purposeCode=purposeCode;
            this.resourceString = resourceString;
        }
    }

    public void setOnClickListener(final ViewHolder viewHolder){
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(onPurposeClickedListener!=null)onPurposeClickedListener.onPurposeClicked(viewHolder.mPurpose.purposeCode);

            }
        });
    }



    public interface OnPurposeClickedListener{
        void onPurposeClicked(int purposeCode);
    }
}
