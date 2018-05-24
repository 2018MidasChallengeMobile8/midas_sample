package com.dmedia.dlimited;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyPageTabTicketFragment extends Fragment implements View.OnClickListener {
    private Button mRequestButton;
    private Button mCompleteButton;
    private Button mTerminateWaitButton;
    private String status = "request";

    Context mContext;
    protected Handler handler;

    private LinearLayoutManager mLayoutManager;
    private ArrayList<DBoxTabContentsListData> mDataList;
    private DBoxTabContentsListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    int mCount = Const.PAGE_SIZE_BIG_ITEMS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage_tab_ticket, null);

        handler = new Handler();

        mRequestButton = (Button) view.findViewById(R.id.btn_wait);
        mCompleteButton = (Button) view.findViewById(R.id.btn_permit);
        mTerminateWaitButton = (Button) view.findViewById(R.id.btn_finished);
        mRequestButton.setOnClickListener(this);
        mCompleteButton.setOnClickListener(this);
        mTerminateWaitButton.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();

        mAdapter = new DBoxTabContentsListAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(DBoxTabContentsListAdapter.MODE_MYPAGE);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new DBoxTabContentsListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                final Runnable r = new Runnable() {
                    public void run() {
                        mAdapter.notifyItemInserted(mDataList.size());
                    }
                };
                handler.post(r);

                if (mDataList.size() < mCount * current_page) {
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //remove progress item
                        mAdapter.notifyItemRemoved(mDataList.size() - 1);
                        //add items one by one
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userid", CommonData.LoginUserData.userId);
                        params.put("status", status);
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REQUEST_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerDBoxListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("status", status);
        params.put("page", 1);
        params.put("count", mCount);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REQUEST_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxListCallback);
        netManager.execute((Void) null);

        return view;
    }

    private NetManager.Callbacks mNetManagerDBoxListCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");

                if (resultCode == 1) {
                    //성공
                    JSONArray jsonArray = jsonObject.getJSONArray("return_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        int categoryId = object.getInt("dbox_category_id");
                        int userId = object.getInt("user_id");
                        //String userPhone = object.getString("userid");
                        String url = object.getString("main_img");
                        String title = object.getString("title");
                        String startDate = object.getString("start");
                        //int placeId = object.getInt("place_id");
                        String place = object.getString("place");
                        int capacity = object.getInt("capacity");
                        String status = object.getString("status");
                        String information = object.getString("information");
                        String mission = object.getString("mission");

                        //핸드폰 번호, place_id response하지 않음
                        addList(id, categoryId, "", title, startDate, place, capacity + "", url);
                    }

                    if (mAdapter.isLoadListenerBlock()) {
                        mAdapter.setLoadListenerBlock(false);
                    }
                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void addList(int id, int categoryId, String phone, String title, String date, String location, String limit, String url) {
        String tmp[] = date.split("-");
        date = tmp[1] + "/" + tmp[2].substring(0, 2); // 2016-10-19을 10/19형식으로

        mDataList.add(new DBoxTabContentsListData(id, categoryId, phone, title, date, location, limit, url));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wait:
                if (!status.equals("request")) {
                    status = "request";
                    mDataList.clear();
                    mAdapter.setLoadListenerBlock(true);
                    mAdapter.setCurrentPageToZero();

                    mRequestButton.setTextColor(Color.WHITE);
                    mRequestButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_fill_black));

                    mCompleteButton.setTextColor(Color.BLACK);
                    mCompleteButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));
                    mTerminateWaitButton.setTextColor(Color.BLACK);
                    mTerminateWaitButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("userid", CommonData.LoginUserData.userId);
                    params.put("status", status);
                    params.put("page", 1);
                    params.put("count", mCount);
                    params.put("session_token", CommonData.LoginUserData.loginToken);
                    NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REQUEST_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerDBoxListCallback);
                    netManager.execute((Void) null);

                }
                break;
            case R.id.btn_permit:
                if (!status.equals("complete")) {
                    status = "complete";
                    mDataList.clear();
                    mAdapter.setLoadListenerBlock(true);
                    mAdapter.setCurrentPageToZero();

                    mCompleteButton.setTextColor(Color.WHITE);
                    mCompleteButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_fill_black));

                    mRequestButton.setTextColor(Color.BLACK);
                    mRequestButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));
                    mTerminateWaitButton.setTextColor(Color.BLACK);
                    mTerminateWaitButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("userid", CommonData.LoginUserData.userId);
                    params.put("status", status);
                    params.put("page", 1);
                    params.put("count", mCount);
                    params.put("session_token", CommonData.LoginUserData.loginToken);
                    NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REQUEST_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerDBoxListCallback);
                    netManager.execute((Void) null);

                }
                break;
            case R.id.btn_finished:
                if (!status.equals("reviewed")) {
                    status = "reviewed";
                    mDataList.clear();
                    mAdapter.setLoadListenerBlock(true);
                    mAdapter.setCurrentPageToZero();

                    mTerminateWaitButton.setTextColor(Color.WHITE);
                    mTerminateWaitButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_fill_black));

                    mRequestButton.setTextColor(Color.BLACK);
                    mRequestButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));
                    mCompleteButton.setTextColor(Color.BLACK);
                    mCompleteButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("userid", CommonData.LoginUserData.userId);
                    params.put("status", status);
                    params.put("page", 1);
                    params.put("count", mCount);
                    params.put("session_token", CommonData.LoginUserData.loginToken);
                    NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REQUEST_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerDBoxListCallback);
                    netManager.execute((Void) null);

                }
                break;
            default:
                break;

        }
    }
}
