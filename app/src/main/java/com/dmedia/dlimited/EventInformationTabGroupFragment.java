package com.dmedia.dlimited;

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
import android.widget.Toast;

import java.util.ArrayList;

//Event 상세 - 구성 탭
public class EventInformationTabGroupFragment extends Fragment {
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
        View view = inflater.inflate(R.layout.fragment_event_information_tab_group, null);

        mDataList = new ArrayList<>();
        if (getArguments() != null) {
            Bundle b = getArguments();
            mDataList = b.getParcelableArrayList("group_model_list");
        }

        mEmptyImageView = (ImageView) view.findViewById(R.id.iv_empty);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
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
