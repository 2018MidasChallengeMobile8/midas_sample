package com.dmedia.dlimited.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dmedia.dlimited.adapter.GroupMemberAdapter;
import com.dmedia.dlimited.model.GroupMemberData;
import com.dmedia.dlimited.R;

import java.util.ArrayList;

public class ProductInformationTabGroupFragment extends Fragment {
    Context mContext;
    private ImageView mEmptyImageView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupMemberAdapter mAdapter;
    private ArrayList<GroupMemberData> mDataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_information_tab_group, null);

        mDataList = new ArrayList<>();
        if (getArguments() != null) {
            Bundle b = getArguments();
            mDataList = b.getParcelableArrayList("group_model_list");
        }

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mEmptyImageView = (ImageView) view.findViewById(R.id.iv_empty);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new GroupMemberAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(GroupMemberAdapter.MODE_NORMAL);
        mRecyclerView.setAdapter(mAdapter);

        if (mAdapter.getItemCount() == 0){
            mEmptyImageView.setVisibility(ImageView.VISIBLE);
        }else{
            mEmptyImageView.setVisibility(ImageView.GONE);
        }

        return view;
    }

}
