package com.purposecaller.purposecaller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.purposecaller.purposecaller.R;
import com.purposecaller.purposecaller.loaders.VideoLoader;
import com.purposecaller.purposecaller.adapters.ChooseVideoRecyclerViewAdapter;
import com.purposecaller.purposecaller.models.VideoLoaderResponse;

import java.util.ArrayList;
import java.util.List;

import static com.purposecaller.purposecaller.Constants.RESULT_CODE_VIDEO_CHOSEN;
import static com.purposecaller.purposecaller.Constants.VIDEO_QUERY_TYPE_POPULAR;
import static com.purposecaller.purposecaller.Constants.VIDEO_QUERY_TYPE_SEARCH;

public class ChooseVideoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<VideoLoaderResponse>{
    public boolean loading,hasMore=true;
    public int visibleThreshold = 2,lastVisibleItem, totalItemCount,NOE_in_one_fetch=7,queryType;
    SearchView searchView;
    MenuItem searchItem;
    String nextPageToken,query;

    List<Object> mValues=new ArrayList<>();
    ChooseVideoRecyclerViewAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        queryType=VIDEO_QUERY_TYPE_POPULAR;
        nextPageToken=null;
        query=null;
        getSupportLoaderManager().initLoader(1,null,this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAdapter=new ChooseVideoRecyclerViewAdapter(this,mValues, new ChooseVideoRecyclerViewAdapter.OnVideoClickedListener() {
            @Override
            public void onVideoClicked(String videoId) {
                setResult(RESULT_CODE_VIDEO_CHOSEN,new Intent().putExtra("videoId",videoId));
                finish();
            }
        });
        RecyclerView recyclerView=findViewById(R.id.list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(mAdapter);
        setRecyclerViewListener(recyclerView);
    }

    public void setRecyclerViewListener(RecyclerView recyclerView){
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager lm=(LinearLayoutManager) recyclerView.getLayoutManager();
               totalItemCount = lm.getItemCount();
              lastVisibleItem = lm.findLastVisibleItemPosition();

                if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)
                        &&mValues.size()>=NOE_in_one_fetch &&hasMore) {

                   mValues.add(null);
                   mAdapter.notifyItemInserted(mValues.size()-1);
                   loading=true;
                  if(nextPageToken!=null){
                      getSupportLoaderManager().restartLoader(1,null,ChooseVideoActivity.this);
                  }



                }
            }
        });
    }

    @NonNull
    @Override
    public Loader<VideoLoaderResponse> onCreateLoader(int id, @Nullable Bundle args) {

          return new VideoLoader(this,queryType,query,nextPageToken);
    }



    @Override
    public void onLoadFinished(@NonNull Loader<VideoLoaderResponse> loader, VideoLoaderResponse data) {
        if(nextPageToken!=null){
            mValues.remove(mValues.size()-1);
        }
        nextPageToken=data.nextPageToken;
        loading=false;

        mValues.addAll(mValues.size(),data.mValues);
      mAdapter.notifyDataSetChanged();



    }

    @Override
    public void onLoaderReset(@NonNull Loader<VideoLoaderResponse> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_video, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();


        searchView.clearFocus(); // close the keyboard on load
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String q) {
                searchView.clearFocus();
                queryType=VIDEO_QUERY_TYPE_SEARCH;
                mValues.clear();
                mAdapter.notifyDataSetChanged();
                query=q;
                nextPageToken=null;
                getSupportLoaderManager().restartLoader(1,null,ChooseVideoActivity.this);



                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Configure the searchUsingTerm info and add any event listeners...

        return super.onCreateOptionsMenu(menu);
    }


}
