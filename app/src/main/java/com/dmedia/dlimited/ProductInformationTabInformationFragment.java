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
import android.widget.TextView;

import java.util.ArrayList;

//Event 상세 - 정보 탭
public class ProductInformationTabInformationFragment extends Fragment {
    Context mContext;

    private TextView mInformationTextView;
    private TextView mMissionTextView;
    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    private ImageView mEmptyImageView;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private ContentsImageAdapter mAdapter;
    private ArrayList<ContentsImageData> mDataList;

    private int id;
    private int placeId;
    //private int privateMode;
    private int viewCnt;
    private int deleted;
    private String title = "";
    private String information = "";
    private String mission = "";
    private String startDate = "";
    private String status = "";
    private String cDate = "";
    private ArrayList<String> imageUrlList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_information_tab_information, null);

        if (getArguments() != null) {
            Bundle b = getArguments();
            id = b.getInt("info_id");
            placeId = b.getInt("info_place_id");
            viewCnt = b.getInt("info_view_cnt");
            deleted = b.getInt("info_deleted");
            title = b.getString("info_title");
            information = b.getString("info_info");
            mission = b.getString("info_mission");
            startDate = b.getString("info_start_date");
            status = b.getString("info_status");
            cDate = b.getString("info_cdate");

            imageUrlList = b.getStringArrayList("info_image_url");
        }

        mEmptyImageView = (ImageView) view.findViewById(R.id.iv_empty);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();
        mAdapter = new ContentsImageAdapter(mContext, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mInformationTextView = (TextView) view.findViewById(R.id.tv_information);
        mMissionTextView = (TextView) view.findViewById(R.id.tv_mission);
        mDateTextView = (TextView) view.findViewById(R.id.tv_date);
        mTimeTextView = (TextView) view.findViewById(R.id.tv_time);

        mTitleTextView.setText(title);
        mInformationTextView.setText(information);
        mMissionTextView.setText(mission);
        mDateTextView.setText(startDate.substring(0, 10));
        mTimeTextView.setText(startDate.substring(11, 16));

        for (int i = 0; i < imageUrlList.size(); i++) {
            mDataList.add(new ContentsImageData(imageUrlList.get(i)));
        }
        mAdapter.notifyDataSetChanged();

        if (mAdapter.getItemCount() == 0){
            mEmptyImageView.setVisibility(ImageView.VISIBLE);
        }else{
            mEmptyImageView.setVisibility(ImageView.GONE);
        }
        return view;
    }
}
