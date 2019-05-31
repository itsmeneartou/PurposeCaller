package com.purposecaller.purposecaller.loaders;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.models.VideoLoaderResponse;

import java.io.IOException;
import java.util.ArrayList;

import static com.purposecaller.purposecaller.Constants.VIDEO_QUERY_TYPE_POPULAR;
import static com.purposecaller.purposecaller.Constants.VIDEO_QUERY_TYPE_SEARCH;


public class VideoLoader extends android.support.v4.content.AsyncTaskLoader<VideoLoaderResponse> {

    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();

    private static final String POPULAR_VIDEOS_FIELDS = "items(id,snippet/title,snippet/thumbnails/high/url),nextPageToken";
    private static final String SEARCH_VIDEOS_FIELDS = "items(id/videoId,snippet/title,snippet/thumbnails/high/url),nextPageToken";
    private Context mContext;
    private int queryType;
    private String query,nextPageToken;
    public VideoLoader(Context context,int queryType,@Nullable String query,@Nullable String nextPageToken) {
        super(context);
        this.mContext=context;
        this.queryType=queryType;
        this.query=query;
        this.nextPageToken=nextPageToken;

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }



    @Override
    public VideoLoaderResponse loadInBackground() {
        try{
            mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                    .setApplicationName(mContext.getResources().getString(R.string.app_name))
                    .build();

            if(queryType==VIDEO_QUERY_TYPE_POPULAR){
                YouTube.Videos.List videoList=mYoutubeDataApi.videos().list("snippet").setFields(POPULAR_VIDEOS_FIELDS).setRegionCode("IN")
                        .setKey(mContext.getString(R.string.youtube_developer_key)).setChart("mostPopular").setMaxResults(10l);
               if(nextPageToken!=null){
                 videoList=  videoList.setPageToken(nextPageToken);
               }
               VideoListResponse videoListResponse=videoList.execute();

                return new VideoLoaderResponse(new ArrayList<Object>(videoListResponse.getItems()),videoListResponse.getNextPageToken());
            }
            else if(queryType==VIDEO_QUERY_TYPE_SEARCH){
                YouTube.Search.List searchList=mYoutubeDataApi.search().list("snippet").setType("video")
                        .setQ(query).setFields(SEARCH_VIDEOS_FIELDS).setRegionCode("IN").setMaxResults(10l)
                        .setKey(mContext.getString(R.string.youtube_developer_key));
                if(nextPageToken!=null){
                   searchList= searchList.setPageToken(nextPageToken);
                }
            SearchListResponse searchListResponse=searchList.execute();


                return new VideoLoaderResponse(new ArrayList<Object>(searchListResponse.getItems()),searchListResponse.getNextPageToken());
            }

           return null;
        }

        catch(IOException e){

            return null;
        }

    }
}