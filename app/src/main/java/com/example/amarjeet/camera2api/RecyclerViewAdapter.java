package com.example.amarjeet.camera2api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import java.util.ArrayList;

/**
 * Created by Amarjeet on 04-06-2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> imgUrls = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<String> imgUrls, Context mContext) {
        this.imgUrls = imgUrls;
        this.mContext = mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;
        RelativeLayout mRelativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.img_recycle);
            mRelativeLayout = itemView.findViewById(R.id.rellayout_recycle);

            int width = mRelativeLayout.getWidth();
            mImageView.setMinimumWidth(width);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_whatsapp_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mImageView.setImageBitmap(BitmapFactory.decodeFile(imgUrls.get(position)));
    }

    @Override
    public int getItemCount() {
        return imgUrls.size();
    }

}
