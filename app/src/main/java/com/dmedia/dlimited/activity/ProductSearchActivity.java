package com.dmedia.dlimited.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.adapter.DBoxTabContentsListAdapter;
import com.dmedia.dlimited.model.DBoxTabContentsListData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-07.
 */

// TODO: 2016-10-13 검색기능 구현, dbox 프로덕트의 모든 아이템 표시
public class ProductSearchActivity extends AppCompatActivity {
    Context mContext;
    private ImageView mBackImageView;
    private ImageView mEmptyImageView;

    private ImageView mSearchImageView;
    private EditText mSearchEditText;
    protected Handler handler;
    private String keyword = "";

    //test
    private DBoxTabContentsListAdapter mAdapter;
    private ArrayList<DBoxTabContentsListData> mDataList;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    int mCount = Const.PAGE_SIZE_BIG_ITEMS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);

        mContext = this;
        handler = new Handler();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mEmptyImageView = (ImageView) findViewById(R.id.iv_empty);

        mBackImageView = (ImageView) findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSearchEditText = (EditText) findViewById(R.id.edt_search);
        mSearchImageView = (ImageView) findViewById(R.id.iv_search);


        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.search || id == EditorInfo.IME_NULL) {
                    keyword = mSearchEditText.getText().toString();
                    attemptSearch(keyword);
                    return true;
                }
                return false;
            }
        });

        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyword = mSearchEditText.getText().toString();
                attemptSearch(keyword);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
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
        mAdapter = new DBoxTabContentsListAdapter(this, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        mAdapter.setOnLoadMoreListener(new DBoxTabContentsListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                //add progress item
                //mAdapter.notifyItemInserted(mDataList.size());

                //TODO: 2016-10-28 에러뜨면 이방식으로
                //Handler handler = new Handler();

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
                        params.put("keyword", keyword);
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        NetData netData = new NetData(NetData.ProtocolType.DBOX_SEARCH, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerDBoxSearchCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });

    }


    private void attemptSearch(String keyword) {
        mDataList.clear();
        mAdapter.notifyDataSetChanged();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_SEARCH, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxSearchCallback);
        netManager.execute((Void) null);
    }

    private void addList(int id, int categoryId, String phone, String title, String date, String location, String limit, String url) {
        String tmp[] = date.split("-");
        date = tmp[1] + "/" + tmp[2].substring(0, 2); // 2016-10-19을 10/19형식으로

        mDataList.add(new DBoxTabContentsListData(id, categoryId, phone, title, date, location, limit, url));
    }

    private NetManager.Callbacks mNetManagerDBoxSearchCallback = new NetManager.Callbacks() {
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
                    JSONArray jsonArray = jsonObject.getJSONArray("dbox_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        int categoryId = object.getInt("dbox_category_id");
                        int userId = object.getInt("user_id");
                        String userPhone = object.getString("userid");
                        String url = object.getString("main_img");
                        String title = object.getString("title");
                        String startDate = object.getString("start");
                        int placeId = object.getInt("place_id");
                        String place = object.getString("place");
                        int capacity = object.getInt("capacity");
                        String status = object.getString("status");
                        String information = object.getString("information");
                        String mission = object.getString("mission");


                        addList(id, categoryId, userPhone, title, startDate, place, capacity + "", url);

                        /*
                        if (object.has("deleted")) {
                            int deleted = object.getInt("deleted");
                            if (deleted == 0) {
                                //삭제되지 않았을 경우
                                addList(title, startDate, place, capacity, url, id);
                            }
                        }
                        */
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

    void refreshItems() {
        // Load items
        mDataList.clear();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_SEARCH, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxSearchCallback);
        netManager.execute((Void) null);

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }



}
