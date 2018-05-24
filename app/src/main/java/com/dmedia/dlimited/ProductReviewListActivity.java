package com.dmedia.dlimited;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
 * Created by xema0 on 2016-10-18.
 */

public class ProductReviewListActivity extends AppCompatActivity {
    Context mContext;
    protected Handler handler;

    private ImageView mEmptyImageView;

    private RecyclerView mRecyclerView;
    private ProductReviewListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private ArrayList<ProductReviewListData> mDataList;

    int mCount = Const.PAGE_SIZE_SMALL_ITEMS;

    private int dboxId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review_list);
        mContext = this;
        handler = new Handler();

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("PRODUCT 리뷰");

        mEmptyImageView = (ImageView) findViewById(R.id.iv_empty);

        dboxId = getIntent().getIntExtra("dbox_id", -1);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();
        mAdapter = new ProductReviewListAdapter(mContext, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new ProductReviewListAdapter.OnLoadMoreListener() {
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
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        params.put("dbox_id", dboxId);
                        NetData netData = new NetData(NetData.ProtocolType.DBOX_REVIEW_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerDBoxReviewListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        params.put("dbox_id", dboxId);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_REVIEW_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxReviewListCallback);
        netManager.execute((Void) null);
    }

    private void addList(int id, int dboxId, int userId, String userNickname, String phoneNumber, String missionUrl, String reviewText) {
        mDataList.add(new ProductReviewListData(id, dboxId, userId, userNickname, phoneNumber, missionUrl, reviewText));
    }


    private NetManager.Callbacks mNetManagerDBoxReviewListCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("review_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        int dboxId = object.getInt("dbox_id");
                        int userId = object.getInt("user_id");
                        String nickName = object.getString("username");
                        String phone = object.getString("phonenumber");
                        String missionUrl = object.getString("mission_url");
                        String reviewText = object.getString("review");

                        addList(id, dboxId, userId, nickName, phone, missionUrl, reviewText);
                    }

                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();

                    if (mAdapter.getItemCount() == 0){
                        mEmptyImageView.setVisibility(ImageView.VISIBLE);
                    }else{
                        mEmptyImageView.setVisibility(ImageView.GONE);
                    }
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
