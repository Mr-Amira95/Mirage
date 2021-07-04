package com.example.c_t_t_s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Alphabets> mAlphabets;

    public ImageAdapter(Context context, List<Alphabets> alphabets){
        mContext=context;
        mAlphabets=alphabets;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(mContext).inflate(R.layout.image_item, viewGroup,false);
        return  new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        Alphabets uploadCur=mAlphabets.get(i);
        imageViewHolder.img_description.setText(uploadCur.getAlphabetName());
        Picasso.get()
                .load(uploadCur.getAlphabetUrl())
                .placeholder(R.drawable.imagepreview)
                .fit()
                .centerCrop()
                .into(imageViewHolder.image_view);
    }

    @Override
    public int getItemCount() {
        return mAlphabets.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView img_description;
        public ImageView image_view;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img_description=itemView.findViewById(R.id.img_description);
            image_view=itemView.findViewById(R.id.image_view);
        }
    }
}
