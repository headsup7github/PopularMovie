package com.environer.popularmovie;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.environer.popularmovie.adapter.CustomCursorAdapter;
import com.environer.popularmovie.adapter.ImageViewAdapter;
import com.environer.popularmovie.data.FavoriteContract;
import com.environer.popularmovie.utilis.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import android.support.design.widget.Snackbar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int TASK_LODER_ID = 22;
    public final String DEFAULT_QUERY = "http://api.themoviedb.org/3/movie/popular?api_key=f42c6d47004be444ed6b421491c794bc";
    public final String TOP_RATED_QUERY = "https://api.themoviedb.org/3/movie/top_rated?api_key=f42c6d47004be444ed6b421491c794bc&language=en-US";
    ProgressBar progressBar;
    ArrayList<MovieData> data;
    MovieData movieData;
    RecyclerView recyclerView;
    ImageViewAdapter adapter;
    URL url;
    CustomCursorAdapter customCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        //Setting up default url;
        url=null;
        try{
            url = new URL(DEFAULT_QUERY);
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        recyclerView = (RecyclerView)findViewById(R.id.recyView);
        FetchData fetchData = new FetchData();
        fetchData.execute(url);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            //The cursor that will hold all the task
            Cursor mFavoriteData = null;

            //On start loading is called when loader first starts loading the data
            @Override
            protected void onStartLoading() {
                if(mFavoriteData!= null){
                    //Deliver any previously loaded task immediately
                    deliverResult(mFavoriteData);
                }
                else{
                    //Force a new load
                    forceLoad();
                }
            }

            //Load in background performs asynchronusly loading of data
            @Override
            public Cursor loadInBackground() {
                //Query and load all the favorite data in the background;
                try{
                    return getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI
                    ,null,null,null,null);
                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            public void deliverResult(Cursor data) {
                mFavoriteData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update the data inside the adapter that shows all the favorite movies list
        recyclerView.setLayoutManager(new GridLayoutManager(this,numberofColumn()));

        //Pass the cursor to adapter
        customCursorAdapter = new CustomCursorAdapter(data,this);
        //set the adapter to the recyclerView
        recyclerView.setAdapter(customCursorAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FetchData extends AsyncTask<URL,Void,String>{
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String result=null;
            try {
                result = NetworkUtils.getResponseFromUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.INVISIBLE);
            if(s!=null && !s.equals(""))
                ParseJson(s);
            else{
                View parentLayout = MainActivity.this.findViewById(R.id.root);
                Snackbar.make(parentLayout,"Please check internet connection!!",Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FetchData fetchData = new FetchData();
                                fetchData.execute(url);
                            }
                        }).show();
            }
        }
    }

    private void ParseJson(String s) {
        try {
            data = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jArray = jsonObject.getJSONArray("results");
            for(int i=0;i<jArray.length();i++){
                JSONObject currentObject = jArray.getJSONObject(i);
                movieData = new MovieData();
                movieData.setTitle(currentObject.getString("title"));
                movieData.setPoster(currentObject.getString("poster_path"));
                movieData.setRelaeseData(currentObject.getString("release_date"));
                movieData.setPlot_synopsis(currentObject.getString("overview"));
                movieData.setVote_avarage(currentObject.getString("vote_average"));
                movieData.setMovieId(currentObject.getString("id"));
                data.add(movieData);
//                Toast.makeText(this, currentObject.getString("title"), Toast.LENGTH_LONG).show();
//                Toast.makeText(this, currentObject.getString("overview"), Toast.LENGTH_LONG).show();
            }

            adapter = new ImageViewAdapter(data,this);
            recyclerView.setLayoutManager(new GridLayoutManager(this,numberofColumn()));
            recyclerView.setAdapter(adapter);

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private int numberofColumn(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if(nColumns<2)return 2;
        return nColumns;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FetchData fetchData = new FetchData();
        URL url=null;
        if(item.getItemId() == R.id.popular){
            try {
                url = new URL(DEFAULT_QUERY);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if(item.getItemId() == R.id.rated){
            try {
                url = new URL(TOP_RATED_QUERY);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if(item.getItemId() == R.id.favorite){
            //Retrieve all data from the content provider in background thread using AsyncTask Loader
            //Use custom cursor adapter to show the images in imageview
            getSupportLoaderManager().initLoader(TASK_LODER_ID,null,this);

        }
        if(url!=null){
            fetchData.execute(url);
            return true;
        }
        return  false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
}
