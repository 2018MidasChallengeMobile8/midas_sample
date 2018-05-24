package com.dmedia.dlimited;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-05.
 */
public class ProductListDetailApplyListActivity extends AppCompatActivity {
    Context mContext;
    protected Handler handler;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ApplyListAdapter mAdapter;
    private ArrayList<ApplyListData> mDataList;
    private ImageView mEmptyImageView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    int mCount = Const.PAGE_SIZE_SMALL_ITEMS;

    private int dboxId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_detail_apply_list);

        mContext = this;
        handler = new Handler();

        if (getIntent() != null) {
            dboxId = getIntent().getIntExtra("dbox_id", 0);
        }

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("신청 목록");

        mEmptyImageView = (ImageView) findViewById(R.id.iv_empty);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_list);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();
        mAdapter = new ApplyListAdapter(this, mDataList, mRecyclerView);
        mAdapter.setMode(ApplyListAdapter.MODE_PERMIT);
        mAdapter.setActivityMode(ApplyListAdapter.ACTIVITY_MODE_DBOX);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new ApplyListAdapter.OnLoadMoreListener() {
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
                        mAdapter.notifyItemRemoved(mDataList.size() - 1);
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("dbox_id", dboxId);
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        //params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserPendingListCallback);
                        netManager.execute((Void) null);
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("dbox_id", dboxId);
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerUserPendingListCallback);
        netManager.execute((Void) null);
    }

    void refreshItems() {
        // Load items
        mDataList.clear();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("dbox_id", dboxId);
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerUserPendingListCallback);
        netManager.execute((Void) null);

        // Load complete
        onItemsLoadComplete();
    }


    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void addList(int id, int eventId, int userId, String userName, String instagram,String instagramName, String phoneNumber, String applyComment, String status, String cDate,String profileUrl) {
        mDataList.add(new ApplyListData(id, eventId, userId, userName, instagram,instagramName, phoneNumber, applyComment, status, cDate,profileUrl));
    }

    private NetManager.Callbacks mNetManagerUserPendingListCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("return_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id"); //event_user table id
                        int eventId = object.getInt("dbox_id");//event table id
                        int userId = object.getInt("user_id");//user table id
                        String userName = object.getString("username");//유저 닉네임
                        String instagram = object.getString("instagram");//인스타 아이디
                        String instagramName = object.getString("instagram_name");
                        String phone = object.getString("userid");//전화번호
                        //String comment = object.getString("comment");//참가신청시 작성한 글
                        String status = object.getString("status");//'request','complete'
                        String cDate = object.getString("cdate");//참가신청일자
                        String profileUrl = object.getString("profile_img_url");

                        //comment 필요 없음 -> ""로 처리
                        addList(id, eventId, userId, userName, instagram,instagramName, phone, "", status, cDate,profileUrl);
                    }
                    if (mAdapter.isLoadListenerBlock()) {
                        mAdapter.setLoadListenerBlock(false);
                    }
                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();

                    if (mAdapter.getItemCount() == 0){
                        mEmptyImageView.setVisibility(ImageView.VISIBLE);
                    }else{
                        mEmptyImageView.setVisibility(ImageView.GONE);
                    }
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
