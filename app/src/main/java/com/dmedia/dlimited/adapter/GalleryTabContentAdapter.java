package com.dmedia.dlimited.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.GalleryTabContentData;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.activity.GalleryImageDetailActivity;

import java.util.ArrayList;

/*
 * Created by min on 2016-09-25.
 */

public class GalleryTabContentAdapter extends RecyclerView.Adapter<GalleryTabContentAdapter.ListItemViewHolder> {


    private Context mContext = null;
    private LayoutInflater inflater;
    private ArrayList<GalleryTabContentData> mListData;
    private int visibleThreshold = 2;
    private int lastVisibleItem;
    private int totalItemCount;

    private int current_page = 0;

    private boolean loadListenerBlock = false;

    private boolean loading;
    private GalleryTabContentAdapter.OnLoadMoreListener onLoadMoreListener;

    public static final int MODE_GALLERY = 0;//갤러리 사진
    public static final int MODE_MEMBER = 1;//멤버뷰 액티비티(인스타그램 사진들)
    private int mode = MODE_MEMBER;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public GalleryTabContentAdapter(Context mContext, ArrayList<GalleryTabContentData> listData, RecyclerView recyclerView) {
        super();
        this.mContext = mContext;
        this.mListData = listData;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (!loadListenerBlock) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            // End has been reached
                            current_page++;
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore(current_page);
                            }
                            loading = true;
                        }
                    }
                }
            });
        }

    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_gallery_image_item, parent, false);
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {
        final GalleryTabContentData mData = mListData.get(position);
        holder.mContext = mContext;
        Glide.with(mContext).load(mData.getUrl())
                .thumbnail(0.2f)
                .crossFade(0)
                .into(holder.ivImage);
        holder.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GalleryImageDetailActivity.class);
                intent.putExtra("data_list", mListData);
                intent.putExtra("position", position);
                intent.putExtra("page", current_page);
                intent.putExtra("mode", mode);
                //Toast.makeText(mContext, position + "", Toast.LENGTH_SHORT).show();
                //intent.putExtra("pos", position);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }


    public boolean isLoadListenerBlock() {
        return loadListenerBlock;
    }

    public void setLoadListenerBlock(boolean loadListenerBlock) {
        this.loadListenerBlock = loadListenerBlock;
    }

    public void setCurrentPageToZero() {
        current_page = 0;
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(GalleryTabContentAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_gallery);
        }
    }
}