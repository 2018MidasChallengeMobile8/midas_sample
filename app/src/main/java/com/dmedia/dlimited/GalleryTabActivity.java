package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by min on 2016-09-28.
 */
// TODO: 2016-10-04 서버 나오면 glide 써서 구현.
// TODO: 2016-10-13 성능이 너무 안좋을거 같은데(이미지 로딩 속도) volley나 UIL쓰는것도 고려해보자
public class GalleryTabActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GalleryTabActivity";
    Context mContext;
    protected Handler handler;

    private BackPressCloseHandler backPressCloseHandler;
    private TextView mHomeTabTextView;
    private TextView mGalleryTabTextView;
    private TextView mDBoxTabTextView;
    private TextView mMyPageTabTextView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    ArrayList<GalleryTabContentData> mDataList;
    public GalleryTabContentAdapter mAdapter;

    int mCount = Const.PAGE_SIZE_GALLERY_ITEMS;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_tab);

        mContext = this;
        handler = new Handler();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        backPressCloseHandler = new BackPressCloseHandler(this);

        mHomeTabTextView = (TextView) findViewById(R.id.tv_home);
        mGalleryTabTextView = (TextView) findViewById(R.id.tv_gallery);
        mDBoxTabTextView = (TextView) findViewById(R.id.tv_dbox);
        mMyPageTabTextView = (TextView) findViewById(R.id.tv_mypage);

        mHomeTabTextView.setOnClickListener(this);
        mGalleryTabTextView.setOnClickListener(this);
        mDBoxTabTextView.setOnClickListener(this);
        mMyPageTabTextView.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mLayoutManager = new GridLayoutManager(this, 3);//한 행에 3개씩
        mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true); // Helps improve performance

        mDataList = new ArrayList<>();

        mAdapter = new GalleryTabContentAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(GalleryTabContentAdapter.MODE_GALLERY);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mAdapter.setOnLoadMoreListener(new GalleryTabContentAdapter.OnLoadMoreListener() {
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
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        NetData netData = new NetData(NetData.ProtocolType.GALLERY_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerGalleryListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.GALLERY_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerGalleryListCallback);
        netManager.execute((Void) null);
    }


    void refreshItems() {
        // Load items
        mDataList.clear();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.GALLERY_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerGalleryListCallback);
        netManager.execute((Void) null);

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    protected void onResume() {
        overridePendingTransition(0, 0);
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_home:
                intent = new Intent(this, HomeTabActivity.class);
                intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.tv_gallery:
                //intent= new Intent(getApplicationContext(),GalleryTabActivity.class);
                //startActivity(intent);
                //finish();
                break;
            case R.id.tv_dbox:
                if (!Utils.isLevelValid(Const.LEVEL_PEEKER)) {
                    Utils.showInvalidLevelDialog(mContext);
                } else {
                    intent = new Intent(this, DBoxTabActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                break;
            case R.id.tv_mypage:
                if (!Utils.isLevelValid(Const.LEVEL_PEEKER)) {
                    Utils.showInvalidLevelDialog(mContext);
                } else {
                    intent = new Intent(this, MyPageTabActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                break;
        }
    }

    private NetManager.Callbacks mNetManagerGalleryListCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                // TODO: 2016-11-02 page_cnt 처리? - 추가 로드
                if (resultCode == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("gallery_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String url = object.getString("img_url");
                        String type = object.getString("type");
                        int sort = object.getInt("sort");
                        String cDate = object.getString("cdate");
                        GalleryTabContentData item = new GalleryTabContentData(id, url, type, sort, cDate);
                        mDataList.add(item);
                    }
                    if (mAdapter.isLoadListenerBlock()) {
                        mAdapter.setLoadListenerBlock(false);
                    }
                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };

    private String getLevel() {
        return CommonData.LoginUserData.level;
    }
}
