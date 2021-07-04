package com.example.c_t_t_s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<FeedBackDB> mFeedbackDB;

    public FeedbackAdapter(Context c, ArrayList<FeedBackDB> feedbackDB){
        mContext = c;
        mFeedbackDB = feedbackDB;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.feedback_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FeedBackDB feedBackDB= mFeedbackDB.get(position);
        holder.em.setText(feedBackDB.getEmail().toString());
        holder.fb.setText(feedBackDB.getFeedback().toString());
    }

    @Override
    public int getItemCount() {
        return mFeedbackDB.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView em,fb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

             em = itemView.findViewById(R.id.feedbackEmail);
             fb = itemView.findViewById(R.id.feedbackView);

         }
    }
}
