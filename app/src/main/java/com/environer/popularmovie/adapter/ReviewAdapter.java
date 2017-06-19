package com.environer.popularmovie.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.environer.popularmovie.R;

import java.util.ArrayList;

/**
 * Created by Mohammad Adil on 04-06-2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyReviewViewHolder> {
    ArrayList<String> reviewData;
    Context context;
    public ReviewAdapter(ArrayList<String> reviewD, Context con){
        reviewData = reviewD;
        context = con;
    }
    @Override
    public MyReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_layout,parent,false);
        MyReviewViewHolder viewHolder = new MyReviewViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(MyReviewViewHolder holder, int position) {
        String concatednatedString = reviewData.get(position);
        String content=null,reviewer=null;
        if(concatednatedString!=null && !concatednatedString.equals("")) {
            content = concatednatedString.substring(0, (concatednatedString.indexOf('`') - 1));
            reviewer = concatednatedString.substring((concatednatedString.indexOf('`')+1),concatednatedString.length()-1);
        }

        holder.reviewerTextView.setText(reviewer + ":    " + content);
//        holder.reviewTextView.setText(content);

    }

    @Override
    public int getItemCount() {
        return reviewData.size();
    }

    public class MyReviewViewHolder extends RecyclerView.ViewHolder{

        TextView reviewerTextView;

        public MyReviewViewHolder(View itemView) {
            super(itemView);
            reviewerTextView = (TextView)itemView.findViewById(R.id.textViewReviewLabel);
//            reviewTextView = (TextView)itemView.findViewById(R.id.textViewReviewData);
        }
    }
}
