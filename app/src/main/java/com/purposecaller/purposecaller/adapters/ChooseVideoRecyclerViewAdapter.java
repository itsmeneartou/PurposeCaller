package com.purposecaller.purposecaller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.purposecaller.purposecaller.R;

import java.util.List;


public class ChooseVideoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> mValues;
    public static final int VIEW_TYPE_VIDEO=1;
    public static final int VIEW_TYPE_PROGRESS=2;
    private OnVideoClickedListener onVideoClickedListener;
    private Context mContext;
    public ChooseVideoRecyclerViewAdapter(Context context,List<Object> videos,OnVideoClickedListener onVideoClickedListener) {
        this.mContext=context;
        this.onVideoClickedListener=onVideoClickedListener;
        this.mValues=videos;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_VIDEO: ViewHolder viewHolder=new ViewHolder( LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_item_layout, parent, false));
                setOnClickListener(viewHolder);
                return viewHolder;
            case VIEW_TYPE_PROGRESS:return new ProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progress_bar_item, parent, false));
        }
        return null;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holde, int position) {
       if(getItemViewType(position)==VIEW_TYPE_VIDEO){
           ViewHolder holder=(ViewHolder) holde;
           holder.searchResult = mValues.get(position);
           if(holder.searchResult instanceof Video){
               Glide.with(mContext).load(((Video)holder.searchResult).getSnippet().getThumbnails().getHigh().getUrl()).into(holder.thumbnail);

               holder.titleTextView.setText(((Video)holder.searchResult).getSnippet().getTitle());

           }
           else if(holder.searchResult instanceof SearchResult){

               Glide.with(mContext).load(((SearchResult)holder.searchResult).getSnippet().getThumbnails().getHigh().getUrl()).into(holder.thumbnail);

               holder.titleTextView.setText(((SearchResult)holder.searchResult).getSnippet().getTitle());
           }
       }



    }

    @Override
    public int getItemViewType(int position) {
        if(mValues.get(position)==null)return VIEW_TYPE_PROGRESS;
        return VIEW_TYPE_VIDEO;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView thumbnail;
        public final TextView titleTextView;
        public Object searchResult;

        public ViewHolder(View view) {
            super(view);
            mView = view;
           thumbnail =  view.findViewById(R.id.thumbnail);
            titleTextView =  view.findViewById(R.id.video_title);
        }

    }



    public void setOnClickListener(final ViewHolder viewHolder){
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(onVideoClickedListener!=null)
             {
                 if(viewHolder.searchResult instanceof Video){
                     onVideoClickedListener.onVideoClicked(((Video)viewHolder.searchResult).getId());
                 }
                 else if(viewHolder.searchResult instanceof SearchResult){
                     onVideoClickedListener.onVideoClicked(((SearchResult)viewHolder.searchResult).getId().getVideoId());
                 }

             }

            }
        });
    }

    public interface OnVideoClickedListener{
        void onVideoClicked(String videoId);
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progress_bar_1);
        }
    }

}
