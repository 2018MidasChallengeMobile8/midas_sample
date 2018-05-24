package com.dmedia.dlimited;

import android.content.Context;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by min on 2016-09-29.
 */
public class DBoxTabContentFragment extends Fragment {
    private static final String TAG = "HomeTabFragment";
    //public static int count = 0;
    protected Handler handler;
    int mCount = Const.PAGE_SIZE_BIG_ITEMS;

    private Context mContext;
    private DBoxTabContentsListAdapter mAdapter;
    private ArrayList<DBoxTabContentsListData> mDataList;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mCategoryId = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dbox, null);

        handler = new Handler();

        if (getArguments() != null) {
            mCategoryId = getArguments().getInt("category_id");
        }

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();
        mAdapter = new DBoxTabContentsListAdapter(mContext, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

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
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        params.put("category_id", mCategoryId);
                        NetData netData = new NetData(NetData.ProtocolType.DBOX_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerDBoxListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        params.put("category_id", mCategoryId);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxListCallback);
        netManager.execute((Void) null);

        /*
        mDataList.add(new DBoxTabContentsListData(getResources().getDrawable(R.drawable.contents_test4), "D.MANSION 객실 이용권", "10/22~11/2", "D.MANSION", 30 + ""));
        mDataList.add(new DBoxTabContentsListData(getResources().getDrawable(R.drawable.p23_img_title), "Welcome Drink 1잔", "9/2", "청담 디브릿지", 40 + ""));
        mDataList.add(new DBoxTabContentsListData(getResources().getDrawable(R.drawable.p19_img_sub2), "객실 이용권", "9/2", "청담 디브릿지", 40 + ""));
        mDataList.add(new DBoxTabContentsListData(getResources().getDrawable(R.drawable.p19_img_sub1), "호텔 숙박권", "9/1~9/2", "청담 디브릿지", 40 + ""));
        mDataList.add(new DBoxTabContentsListData(getResources().getDrawable(R.drawable.p23_img_sub2), "Cocktail 무료 이용권", "9/2", "청담 디브릿지", 40 + ""));
        */

        return view;
    }


    void refreshItems() {
        // Load items
        mDataList.clear();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        params.put("category_id", mCategoryId);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxListCallback);
        netManager.execute((Void) null);

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void addList(int id, int categoryId, String phone, String title, String date, String location, String limit, String url) {
        String tmp[] = date.split("-");
        date = tmp[1] + "/" + tmp[2].substring(0, 2); // 2016-10-19을 10/19형식으로

        mDataList.add(new DBoxTabContentsListData(id, categoryId, phone, title, date, location, limit, url));
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

                        //All
                        //if (mCategoryId == 0) {
                        //    addList(id, categoryId, userPhone, title, startDate, place, capacity + "", url);
                        //}
                        //other category
                        //else if (categoryId == mCategoryId) {
                            addList(id, categoryId, userPhone, title, startDate, place, capacity + "", url);
                        //}
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

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
