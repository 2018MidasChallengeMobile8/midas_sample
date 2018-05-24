package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

// TODO: 2016-10-04 신청 대기 리스트 옆에 숫자 뜨도록 구현!
public class EventListDetailTabPermitFragment extends Fragment implements View.OnClickListener {
    Context mContext;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ApplyListAdapter mAdapter;
    private ArrayList<ApplyListData> mDataList;

    private LinearLayout mWaitingListLinearLayout;

    private int applyUserCurrent = 0;
    private int eventId;

    private TextView mApplyUserCountTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list_detail_tab_permit, null);

        mDataList = new ArrayList<>();
        if (getArguments() != null) {
            Bundle b = getArguments();
            applyUserCurrent = b.getInt("apply_user_cnt", 0);
            mDataList = b.getParcelableArrayList("permit_user_list");
            eventId = b.getInt("info_id", 0);
        }

        mWaitingListLinearLayout = (LinearLayout) view.findViewById(R.id.ll_waiting_list);
        mWaitingListLinearLayout.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new ApplyListAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(ApplyListAdapter.MODE_DELETE);
        mAdapter.setActivityMode(ApplyListAdapter.ACTIVITY_MODE_EVENT);
        mRecyclerView.setAdapter(mAdapter);

        mApplyUserCountTextView = (TextView) view.findViewById(R.id.tv_apply_user_count);
        mApplyUserCountTextView.setText(applyUserCurrent + "");

        mAdapter.setOnCustomDeleteListener(new ApplyListAdapter.ICustomDeleteListener() {
            @Override
            public void delete() {
                applyUserCurrent++;
                mApplyUserCountTextView.setText(applyUserCurrent + "");
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        // TODO: 2016-10-04 신청 대기리스트 액티비티로 넘어가도록 구현
        Intent intent = new Intent(mContext, EventListDetailApplyListActivity.class);
        intent.putExtra("event_id",eventId);
        startActivity(intent);
    }
}
