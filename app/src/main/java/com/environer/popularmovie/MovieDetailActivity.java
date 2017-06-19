package com.environer.popularmovie;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.environer.popularmovie.adapter.ReviewAdapter;
import com.environer.popularmovie.adapter.TrailerAdapter;
import com.environer.popularmovie.data.FavoriteContract;
import com.environer.popularmovie.data.FavoriteDbHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mohammad Adil on 31-05-2017.
 */

public class MovieDetailActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int EXTERNAL_PERMISSION_CODE = 2;
    String title,release,rating,overview,poster,movieId;
    ArrayList<String> trailerDetials,reviewDetails;
    android.support.v7.widget.Toolbar toolbar;
    TrailerAdapter trailerAdapter;
    ReviewAdapter reviewAdapter;
    ProgressBar progressBar;
    @BindView(R.id.textViewDescription)TextView overviewTv;
    @BindView(R.id.textViewTitle)TextView titleTv;
    @BindView(R.id.textViewYearoflaunch)TextView releaseTv;
    @BindView(R.id.textViewRate)TextView ratingTv;
    @BindView(R.id.imageViewMovieposter)ImageView posterImageView;
    @BindView(R.id.trailersRecyclerView)RecyclerView trailerRv;
    @BindView(R.id.reviewRecyclerView)RecyclerView reviewRv;
    @BindView(R.id.detailRoot)RelativeLayout relativeLayout;
    @BindView(R.id.textViewReview)TextView textViewReview;
    @BindView(R.id.textViewTrailers)TextView trailerTv;
    @BindView(R.id.button_favorite)Button favoriteBtn;
    String trailer_query,review_query;
    RequestQueue queue;

    private SQLiteDatabase myDatabase;
    String imageLocation;
    private long insertion;
    private long deletion;
    boolean favoriteFactor;
    private String delImageLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        ButterKnife.bind(this);

        FavoriteDbHelper dbHelper = new FavoriteDbHelper(this);
        myDatabase = dbHelper.getWritableDatabase();


        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(progressBar,params);


        progressBar.setVisibility(View.VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        if(getIntent().hasExtra("title")){
            Bundle bundle = getIntent().getExtras();
            title = String.valueOf(bundle.get("title"));
            release = String.valueOf(bundle.get("release"));
            rating = String.valueOf(bundle.get("rating"));
            overview = String.valueOf(bundle.get("overview"));
            poster = String.valueOf(bundle.get("poster"));
            movieId = String.valueOf(bundle.get("id"));
        }

        //On start of the detail activity, check that the particular movie is favorite or not
        //on the basis of result show the button text according to that
        favoriteFactor = isFavorite(title);
        if(favoriteFactor){
            favoriteBtn.setText(getString(R.string.unfavoriteButtonText));
        }
        else {
            favoriteBtn.setText(getString(R.string.favoriteButtonText));
        }

        //When we access the favorite data, the movie id will be null due to which
        //we getTrailer and getReviews wouldn't be called because we might be offline at that moment
        if(!movieId.equals("null")) {
            getTrailers(movieId);
            getReviews(movieId);
        }
        titleTv.setText(title);
        releaseTv.setText(release);
        ratingTv.setText(rating);
        overviewTv.setText(overview);

        //If user want to access from favorite database then there is no need to download the image from internet
        //since we have already saved the imageLocation in database and when user come to this activity via
        //favorite list then the poster variable contains the complete path of image inside of smart phone
        if(!movieId.equals("null"))
            Picasso.with(this).load(getString(R.string.image_url)+poster).error(R.drawable.error).into(posterImageView);
        else {
            posterImageView.setImageURI(Uri.parse(poster));
            progressBar.setVisibility(View.INVISIBLE);
        }
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Movie Details");//Its not showing ??
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getReviews(String movieId) {
        review_query = "https://api.themoviedb.org/3/movie/"+movieId +"/reviews?api_key=f42c6d47004be444ed6b421491c794bc";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, review_query, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response!=null&&!response.equals("")){
                    ParseReviewJson(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MovieDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        queue.add(stringRequest);
    }

    private void ParseReviewJson(String response) {
        try {
            reviewDetails = new ArrayList<>();
            JSONObject root = new JSONObject(response);
            JSONArray resultArray = root.getJSONArray("results");
            for(int i = 0;i<resultArray.length();i++){
                JSONObject currentObject = resultArray.getJSONObject(i);
                String content = currentObject.getString("content");
                String reviewer = currentObject.getString("author");
                //Since I have to store both information
                String concatednatedString = content + "`" + reviewer;
                reviewDetails.add(concatednatedString);

            }
            if(resultArray.length()==0){
                textViewReview.setText(getString(R.string.noReviewAvailable));
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
            reviewAdapter = new ReviewAdapter(reviewDetails,this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            reviewRv.setLayoutManager(linearLayoutManager);
            reviewRv.setAdapter(reviewAdapter);
            progressBar.setVisibility(View.INVISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getTrailers(String movieId) {
//        progressBar.setVisibility(View.VISIBLE);
        trailer_query = "https://api.themoviedb.org/3/movie/" +movieId+ "/videos?api_key=f42c6d47004be444ed6b421491c794bc";
        queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, trailer_query, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response!=null && !response.equals("")){
                    ParseJson(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(MovieDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void ParseJson(String res) {
        try {
            trailerDetials = new ArrayList<>();
            JSONObject root = new JSONObject(res);
            JSONArray resultArray = root.getJSONArray("results");
            for(int i = 0;i<resultArray.length();i++){
                JSONObject currentObject = resultArray.getJSONObject(i);
                String youtubeKeyLink = currentObject.getString("key");
                trailerDetials.add(youtubeKeyLink);
            }
            if(resultArray.length()==0){
                trailerTv.setText(getString(R.string.noTrailerAvailable));
                return;
            }
//            progressBar.setVisibility(View.INVISIBLE);
            trailerAdapter = new TrailerAdapter(trailerDetials,this);
            LinearLayoutManager layoutManager  = new LinearLayoutManager(this);
            trailerRv.setLayoutManager(layoutManager);
            trailerRv.setAdapter(trailerAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void askDownloadingPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},EXTERNAL_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == EXTERNAL_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            downloadImage();
        }
        else{
            Toast.makeText(this, "Please give permission to make it favorite", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadImage(){
        Picasso.with(this).load(getString(R.string.image_url)+ poster).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                File file = new File(Environment.getExternalStorageDirectory().getPath()
                        +"/" + title + ".jpg");
                imageLocation = file.getPath();
                try {
                    file.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,75,outputStream);
                    outputStream.close();
//                    Toast.makeText(MovieDetailActivity.this, "Downloaded", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Toast.makeText(MovieDetailActivity.this, "Error in downloading the image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        //Store the details to view it offline
        if(imageLocation!=null && !imageLocation.equals(""))
            insertion = AddToFavorite(title,release,rating,imageLocation,overview);
        if(insertion != -1){
            //insertion has been successfully done
            Toast.makeText(this, "Marked as favorite", Toast.LENGTH_SHORT).show();
            //change the text to unfavoriteText
            favoriteBtn.setText(getString(R.string.unfavoriteButtonText));
        }
        else{
            Toast.makeText(this, "There is some problem, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_favorite)
    @Override
    public void onClick(View view) {

        if(view == favoriteBtn){
            if(favoriteBtn.getText() == getString(R.string.favoriteButtonText)) {
                //download the image into smartphone and save the location in database to view it offline
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                    askDownloadingPermissions();
                else {
                    downloadImage();
                }
            }
            else{
                deletion = 0;
                //remove the detail of that particular movie
                try{
                    deletion = RemoveFromFavorite(title);
                }
                catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if(deletion>0){
                    //deleted successfully
                    Toast.makeText(this, "Remove from your favorite list", Toast.LENGTH_SHORT).show();

                    //check weather we are at favorite list or some other list
                    if(movieId.equals("null")){
                        //we are in favorite view
                        startActivity(new Intent(this,MainActivity.class));
                        finish();
                    }
                    //change the text to favoriteText
                    favoriteBtn.setText(getString(R.string.favoriteButtonText));
                }
                //delete it's image from smartphone
                File file = new File(Environment.getExternalStorageDirectory().getPath()
                        +"/" + title + ".jpg");
                if(file.exists())
                    file.delete();
            }

        }
    }

    private long RemoveFromFavorite(String title) {
//        return myDatabase.delete(FavoriteContract.FavoriteEntry.TABLE_NAME
//        , FavoriteContract.FavoriteEntry.MOVIENAME_COLUMN + "='" + title + "'",null);
        return getContentResolver().delete(FavoriteContract.FavoriteEntry.CONTENT_URI,
                FavoriteContract.FavoriteEntry.MOVIENAME_COLUMN + "='" + title + "'",null);
    }

    public boolean isFavorite(String title){
//        Cursor cursor = myDatabase.query(FavoriteContract.FavoriteEntry.TABLE_NAME,
//                null,null,null,null,null,null);

        Cursor cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.CONTENT_URI
        ,null,null,null,null);
        String factor = null;
        while(cursor.moveToNext()){
            factor = cursor.getString(1);
            if(factor.equals(title)){
                return true;
            }
        }
        return false;
    }

    public long AddToFavorite(String mName,String rDate,String rate,String imgLocation, String overV){
        ContentValues cv = new ContentValues();
        cv.put(FavoriteContract.FavoriteEntry.MOVIENAME_COLUMN,mName);
        cv.put(FavoriteContract.FavoriteEntry.RELEASEDATE_COLUMN,rDate);
        cv.put(FavoriteContract.FavoriteEntry.RATING_COLUMN,rate);
        cv.put(FavoriteContract.FavoriteEntry.IMAGESTORAGELOCATION_COLUMN,imgLocation);
        cv.put(FavoriteContract.FavoriteEntry.OVERVIEW_COLUMN,overV);

//        return myDatabase.insert(FavoriteContract.FavoriteEntry.TABLE_NAME,null,cv);
        Uri uri = getContentResolver().insert(FavoriteContract.FavoriteEntry.CONTENT_URI,cv);
        if(uri!=null) {
            return 0;
        }
        return -1;
    }
}
