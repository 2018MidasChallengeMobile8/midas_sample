package com.dmedia.dlimited;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_UDID;

/**
 * Created by xema0 on 2016-10-05.
 */
//Event 상세 - 승인탭의 신청 대기 리스트 액티비티
// TODO: 2016-10-17 gone된 people list item 의 승인버튼 다시 보이도록 띄우고 x표는 gone시키기
public class EventListDetailApplyListActivity extends AppCompatActivity {
    Context mContext;
    protected Handler handler;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ApplyListAdapter mAdapter;
    private ArrayList<ApplyListData> mDataList; // TODO: 2016-10-05 신청 리스트 멤버들에 따로 필요한 데이터가 있을경우 클래스 만들어서 교체
    private ImageView mEmptyImageView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    int mCount = Const.PAGE_SIZE_SMALL_ITEMS;

    private int eventId = 0;

    @Override
    protected void onResume() {
        super.onResume();
        getList();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_detail_apply_list);

        mContext = this;
        handler = new Handler();

        if (getIntent() != null) {
            eventId = getIntent().getIntExtra("event_id", 0);
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
        mAdapter.setActivityMode(ApplyListAdapter.ACTIVITY_MODE_EVENT);
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
                        params.put("event_id", eventId);
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserPendingListCallback);
                        netManager.execute((Void) null);
                    }
                }, 500);
            }
        });

        //getList();
    }

    private void getList(){
        HashMap<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("page", 1);
        params.put("count", mCount);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
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
        params.put("page", 1);
        params.put("event_id", eventId);
        params.put("count", mCount);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_PENDING_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
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
                    mDataList.clear();

                    JSONArray jsonArray = jsonObject.getJSONArray("return_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id"); //event_user table id
                        int eventId = object.getInt("event_id");//event table id
                        int userId = object.getInt("user_id");//user table id
                        String userName = object.getString("username");//유저 닉네임
                        String instagram = object.getString("instagram");//인스타 아이디
                        String instagramName = object.getString("instagram_name");
                        String phone = object.getString("phonenumber");//전화번호
                        String comment = object.getString("comment");//참가신청시 작성한 글
                        String status = object.getString("status");//'request','complete'
                        String cDate = object.getString("cdate");//참가신청일자
                        String profileImgUrl = object.getString("profile_img_url");


                        addList(id, eventId, userId, userName, instagram,instagramName, phone, comment, status, cDate,profileImgUrl);
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
