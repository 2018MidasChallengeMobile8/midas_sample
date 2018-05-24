package com.dmedia.dlimited;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.DraggableRecyclerView.CallbackItemTouch;
import com.dmedia.dlimited.DraggableRecyclerView.MyItemTouchHelperCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

//Event 상세 - 정보 탭
public class ProductListDetailTabInformationFragment extends Fragment implements CallbackItemTouch {
    Context mContext;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private TextView mInformationTextView;
    private TextView mMissionTextView;
    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;


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

    private ArrayList<SubImageData> mSubImageDataList;
    private SubImageAdapter mSubImageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list_detail_tab_information, null);

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

            mSubImageDataList = b.getParcelableArrayList("sub_image_data");
        } else {
            mSubImageDataList = new ArrayList<>();
        }
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mInformationTextView = (TextView) view.findViewById(R.id.tv_information);
        mMissionTextView = (TextView) view.findViewById(R.id.tv_mission);
        mDateTextView = (TextView) view.findViewById(R.id.tv_date);
        mTimeTextView = (TextView) view.findViewById(R.id.tv_time);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mSubImageAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSubImageAdapter = new SubImageAdapter(mContext, mSubImageDataList, mRecyclerView, SubImageAdapter.MODE_DEFAULT);
        mRecyclerView.setAdapter(mSubImageAdapter);

        mTitleTextView.setText(title);
        mInformationTextView.setText(information);
        mMissionTextView.setText(mission);
        mDateTextView.setText(startDate.substring(0, 10));
        mTimeTextView.setText(startDate.substring(11, 16));

        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(this);// create MyItemTouchHelperCallback
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback); // Create ItemTouchHelper and pass with parameter the MyItemTouchHelperCallback
        touchHelper.attachToRecyclerView(mRecyclerView); // Attach ItemTouchHelper to RecyclerView


        return view;
    }

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(mSubImageDataList, oldPosition, newPosition); // change position
        mSubImageAdapter.notifyItemMoved(oldPosition, newPosition); //notifies changes in adapter, in this case use the notifyItemMoved
        //Log.d("po", oldPosition + "  " + newPosition);

        String paramStr = "?" + "dbox_id=" + id + "&userid=" + CommonData.LoginUserData.userId + "&session_token=" + CommonData.LoginUserData.loginToken;
        String imageParamStr = "";
        for (int i = 0; i < mSubImageDataList.size(); i++) {
            imageParamStr += "&img_id_list=" + mSubImageDataList.get(i).getId();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mContext, "http://www.dlimited.co.kr/api/dbox/image/sort" + paramStr + imageParamStr, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("response", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("error response", errorResponse.toString());
            }
        });

    }
}
