package com.dmedia.dlimited.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dmedia.dlimited.model.ContentsImageData;
import com.dmedia.dlimited.R;

import java.util.ArrayList;

/**
 * Created by min on 2016-09-23.
 */
public class ContentsImageAdapter extends RecyclerView.Adapter<ContentsImageAdapter.ListItemViewHolder> {
    private Context mContext = null;
    private ArrayList<ContentsImageData> mListData;

    public ContentsImageAdapter(Context mContext, ArrayList<ContentsImageData> listData, RecyclerView recyclerView) {
        super();
        this.mContext = mContext;
        this.mListData = listData;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_image_list_item, parent, false);

        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {
        final ContentsImageData mData = mListData.get(position);
        holder.mContext = mContext;
        Glide.with(mContext).load(mData.getUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivUploadImage);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }


    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUploadImage;
        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            ivUploadImage = (ImageView) itemView.findViewById(R.id.iv_upload_image);
        }
    }
}
