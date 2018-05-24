package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by min on 2016-09-23.
 */
public class HomeTabContentsListAdapter extends RecyclerView.Adapter<HomeTabContentsListAdapter.ListItemViewHolder> {
    private static final String TAG = "";

    private Context mContext = null;
    private List<HomeTabContentsListData> mListData;

    private int visibleThreshold = 1;
    private int lastVisibleItem;
    private int totalItemCount;
    private int current_page = 0;

    private boolean loading;
    private boolean loadListenerBlock = false;
    private HomeTabContentsListAdapter.OnLoadMoreListener onLoadMoreListener;

    public static final int MODE_MYPAGE = 200;//마이페이지에서 접근할 경우 뒤로가도 마이페이지로.
    public static final int MODE_DEFAULT = 100;
    private int mode = MODE_DEFAULT;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public HomeTabContentsListAdapter(Context mContext, List<HomeTabContentsListData> listData, RecyclerView recyclerView) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_contents_item, parent, false);
        return new ListItemViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, int position) {
        holder.mContext = mContext;
        final HomeTabContentsListData mData = (HomeTabContentsListData) mListData.get(position);

        if (mData.getImageUrl() == null) {
            // TODO: 2016-10-30 이미지url이 없을경우 나오는 이미지설정
        } else {
            Glide.with(mContext).load(mData.getImageUrl()).into(holder.ivMainImage);
        }
        holder.tvTitle.setText(mData.getTitle());
        holder.tvDate.setText(mData.getDate());
        holder.tvLocation.setText(mData.getLocation());
        holder.tvLimit.setText(mData.getLimit());

        holder.rvListItemArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isLevelValid(Const.LEVEL_PEEKER)) {
                    Utils.showInvalidLevelDialog(mContext);
                } else {
                    Intent intent = new Intent(mContext, EventInformationActivity.class);
                    intent.putExtra("event_id", mData.getId());
                    intent.putExtra("event_title", mData.getTitle());
                    intent.putExtra("mode", mode);
                    mContext.startActivity(intent);
                }
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

    public void setLoaded() {
        loading = false;
    }

    public void setCurrentPageToZero() {
        current_page = 0;
    }

    public void setOnLoadMoreListener(HomeTabContentsListAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMainImage;
        TextView tvTitle;
        TextView tvDate;
        TextView tvLocation;
        TextView tvLimit;

        RelativeLayout rvListItemArea;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            ivMainImage = (ImageView) itemView.findViewById(R.id.iv_main_image);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvLocation = (TextView) itemView.findViewById(R.id.tv_location);
            tvLimit = (TextView) itemView.findViewById(R.id.tv_limit);
            rvListItemArea = (RelativeLayout) itemView.findViewById(R.id.cv_list_item_area);
        }

    }
}
