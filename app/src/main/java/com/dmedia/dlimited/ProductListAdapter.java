package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jeonghoy on 2016. 6. 16..
 */

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ListItemViewHolder> {

    private static final String TAG = "EventListAdapter";

    private Context mContext = null;
    private LayoutInflater inflater;
    private ArrayList<ProductListData> mListData;
    private int visibleThreshold = 2;
    private int lastVisibleItem;
    private int totalItemCount;

    private int current_page = 0;

    private boolean loading;
    private ProductListAdapter.OnLoadMoreListener onLoadMoreListener;

    public ProductListAdapter(Context mContext, ArrayList<ProductListData> listData, RecyclerView recyclerView) {
        super();
        this.mContext = mContext;
        this.mListData = listData;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        current_page++;
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_event_list_item, parent, false);
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {

        final ProductListData mData = mListData.get(position);

        holder.tvTitle.setText(mData.getTitle());
        holder.tvDate.setText(mData.getDate());
        holder.tvStatus.setText(mData.getState());
        holder.llListItemArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProductListDetailActivity.class);
                intent.putExtra("product_id", mData.getId());
                intent.putExtra("product_title", mData.getTitle());
                mContext.startActivity(intent);
            }
        });

        holder.mContext = mContext;

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(ProductListAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvStatus;

        LinearLayout llListItemArea;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_state);
            llListItemArea = (LinearLayout) itemView.findViewById(R.id.ll_list_item_area);
        }
    }
}