package com.environer.popularmovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.environer.popularmovie.MainActivity;
import com.environer.popularmovie.MovieDetailActivity;
import com.environer.popularmovie.R;
import com.environer.popularmovie.data.FavoriteContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Mohammad Adil on 05-06-2017.
 */

public class CustomCursorAdapter extends RecyclerView.Adapter<CustomCursorAdapter.MyCursorViewHolder>{
    Cursor mData;
    Context context;
    public CustomCursorAdapter(Cursor data, Context con){
        mData = data;
        context = con;
    }

    @Override
    public MyCursorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagelayout,parent,false);
        MyCursorViewHolder viewHolder = new MyCursorViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyCursorViewHolder holder, int position) {
        mData.moveToPosition(position);
        String imageLocation = mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.IMAGESTORAGELOCATION_COLUMN));
        holder.imageView.setImageURI(Uri.parse(imageLocation));

        //Set click listner for the imageView in favorite
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                mData.moveToPosition(pos);
                Intent i = new Intent(context, MovieDetailActivity.class);
                i.putExtra("title",mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.MOVIENAME_COLUMN)));
                i.putExtra("release",mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.RELEASEDATE_COLUMN)));
                i.putExtra("rating",mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.RATING_COLUMN)));
                i.putExtra("poster",mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.IMAGESTORAGELOCATION_COLUMN)));
                i.putExtra("overview",mData.getString(mData.getColumnIndex(FavoriteContract.FavoriteEntry.OVERVIEW_COLUMN)));
                context.startActivity(i);
            }
        });
        //start details activity and show all the stored details there
    }

    @Override
    public int getItemCount() {
        return mData.getCount();
    }

    public class MyCursorViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        public MyCursorViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.imageView);
        }
    }
}
