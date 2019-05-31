package com.purposecaller.purposecaller.loaders;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.purposecaller.purposecaller.R;

import java.io.IOException;
import java.util.List;


public class SearchLoader extends android.support.v4.content.AsyncTaskLoader<List<SearchResult>> {

    private YouTube mYoutubeDataApi;
    private final GsonFactory mJsonFactory = new GsonFactory();
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private static final String YOUTUBE_VIDEOS_FIELDS = "items(id,snippet/title,snippet/thumbnails/high/url)";
    private Context mContext;
    private String query;
    public SearchLoader(Context context,String query) {
        super(context);
        this.query=query;
        this.mContext=context;

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }



    @Override
    public List<SearchResult> loadInBackground() {
        try{
            mYoutubeDataApi = new YouTube.Builder(mTransport, mJsonFactory, null)
                    .setApplicationName(mContext.getResources().getString(R.string.app_name))
                    .build();
            return mYoutubeDataApi.search().list("snippet").setQ(query).setFields(YOUTUBE_VIDEOS_FIELDS).setRegionCode("IN")
                    .setKey(mContext.getString(R.string.youtube_developer_key)).set("limit",10).execute().getItems();

        }

        catch(IOException e){

            return null;
        }

    }
}