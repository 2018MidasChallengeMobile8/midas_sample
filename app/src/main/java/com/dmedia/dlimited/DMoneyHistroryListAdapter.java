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

public class DMoneyHistroryListAdapter extends RecyclerView.Adapter<DMoneyHistroryListAdapter.ListItemViewHolder> {
    private Context mContext = null;
    private ArrayList<DMoneyHistoryListData> mListData;

    private int visibleThreshold = 2;
    private int lastVisibleItem;
    private int totalItemCount;
    private int current_page = 0;

    private boolean loading;
    private DMoneyHistroryListAdapter.OnLoadMoreListener onLoadMoreListener;

    public DMoneyHistroryListAdapter(Context mContext, ArrayList<DMoneyHistoryListData> listData, RecyclerView recyclerView) {
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_dmoney_history_list_item, parent, false);
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {

        final DMoneyHistoryListData mData = mListData.get(position);

        holder.tvDate.setText(mData.getDate());
        holder.tvPlace.setText(mData.getPlace());
        holder.tvLabel.setText(mData.getLabel());
        holder.tvDMoney.setText(mData.getDmoney());

        holder.mContext = mContext;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCurrentPageToZero() {
        current_page = 0;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(DMoneyHistroryListAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvPlace;
        TextView tvLabel;
        TextView tvDMoney;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPlace = (TextView) itemView.findViewById(R.id.tv_place);
            tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
            tvDMoney = (TextView) itemView.findViewById(R.id.tv_dmoney);
        }
    }
}