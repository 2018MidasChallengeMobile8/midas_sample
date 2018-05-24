package com.dmedia.dlimited.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.util.BackPressCloseHandler;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.adapter.HomeTabContentsListAdapter;
import com.dmedia.dlimited.model.HomeTabContentsListData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by min on 2016-09-18.
 */
// TODO: 2016-11-05 레벨에 따라 기능 분할.
// TODO: 2016-10-28 메인이미지 url없음 -> 수정요청!

// TODO: 2016-09-22 호스트, 게스트에 따라 더보기 버튼 구현..
// TODO: 2016-09-23 home gallery ... 이 4개 탭레이아웃-뷰페이저로 구성할지 아니면 액티비티로 구성할지

public class HomeTabActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeTabActivity";
    Context mContext;
    protected Handler handler;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<HomeTabContentsListData> mDataList;
    private HomeTabContentsListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private TextView mHomeTabTextView;
    private TextView mGalleryTabTextView;
    private TextView mDBoxTabTextView;
    private TextView mMyPageTabTextView;

    private BackPressCloseHandler backPressCloseHandler;
    //private InstagramApp mApp;

    int mCount = Const.PAGE_SIZE_BIG_ITEMS;

    private FloatingActionButton mSearchFloationgActionButton;


    public static final String PREFERENCE_COACH_MARK = "preference_coach_mark";
    public static final String PREFERENCE_COACH_MARK_HOME = "preference_coach_mark_home";
    public static final String PREFERENCE_COACH_MARK_DBOX = "preference_coach_mark_dbox";
    public static final String PREFERENCE_COACH_MARK_MY_PAGE = "preference_coach_mark_my_page";
    public static final String PREFERENCE_COACH_MARK_EVENT = "preference_coach_mark_event";
    public static final String PREFERENCE_COACH_MARK_PRODUCT = "preference_coach_mark_product";

    private void showCoachMark() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_coach_mark_home);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        View masterView = dialog.findViewById(R.id.rl_layout);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                SharedPreferences prefs = getSharedPreferences(PREFERENCE_COACH_MARK, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PREFERENCE_COACH_MARK_HOME, "checked");
                editor.commit();
            }
        });
        dialog.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tab);

        mContext = this;
        handler = new Handler();

        //mApp = new InstagramApp(mContext, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        //mApp.setListener(instagramListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setLogo(R.drawable.p06_img_toplogo);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_COACH_MARK, MODE_PRIVATE);
        String check = prefs.getString(PREFERENCE_COACH_MARK_HOME, "");
        if (check.equals("")) {
            showCoachMark();
        }


        /*
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams instaParams = new RequestParams();
        if (!mApp.hasAccessToken())
            mApp.setAccessToken(CommonData.LoginUserData.instaToken);
        instaParams.put("access_token", mApp.getAccessToken());
        client.get(mContext, "https://api.instagram.com/v1/users/self/", instaParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if (errorResponse.getJSONObject("meta").getString("error_type").equals("OAuthAccessTokenException")) {
                        Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        mApp.authorize();
                    } else {
                        Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        //Log.d("ca", errorResponse.toString());
                        //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                        mApp.authorize();
                    }
                } catch (JSONException e) {
                    Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    mApp.authorize();
                }
            }
        });
        */


        mHomeTabTextView = (TextView) findViewById(R.id.tv_home);
        mGalleryTabTextView = (TextView) findViewById(R.id.tv_gallery);
        mDBoxTabTextView = (TextView) findViewById(R.id.tv_dbox);
        mMyPageTabTextView = (TextView) findViewById(R.id.tv_mypage);
        mSearchFloationgActionButton = (FloatingActionButton) findViewById(R.id.fab_search);

        mHomeTabTextView.setOnClickListener(this);
        mGalleryTabTextView.setOnClickListener(this);
        mDBoxTabTextView.setOnClickListener(this);
        mMyPageTabTextView.setOnClickListener(this);
        mSearchFloationgActionButton.setOnClickListener(this);

        backPressCloseHandler = new BackPressCloseHandler(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();

        mAdapter = new HomeTabContentsListAdapter(this, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mAdapter.setOnLoadMoreListener(new HomeTabContentsListAdapter.OnLoadMoreListener() {
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
                        NetData netData = new NetData(NetData.ProtocolType.EVENT_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerEventListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventListCallback);
        netManager.execute((Void) null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utils.isLevelValid(Const.LEVEL_HOST)) {
            getMenuInflater().inflate(R.menu.menu_home_tab_host, menu);
        } else if (Utils.isLevelValid(Const.LEVEL_PEEKER)) {
            getMenuInflater().inflate(R.menu.menu_home_tab_guest, menu);
        }
        return true;
    }


    /*
    InstagramApp.OAuthAuthenticationListener instagramListener = new InstagramApp.OAuthAuthenticationListener() {

        @Override
        public void onSuccess() {
            Toast.makeText(mContext, "연결되었습니다", Toast.LENGTH_SHORT).show();
            CommonData.LoginUserData.instaToken = mApp.getAccessToken();
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(mContext, "인스타그램 서버와 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    };
    */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!Utils.isLevelValid(Const.LEVEL_PEEKER)) {
            Utils.showInvalidLevelDialog(mContext);
        } else {
            //문의하기
            if (id == R.id.menu_inquiry) {
                Intent intent = new Intent(HomeTabActivity.this, SettingsInquiryActivity.class);
                startActivity(intent);
            }
            //이벤트 개설 내역
            else if (id == R.id.menu_event_list) {
                Intent intent = new Intent(HomeTabActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void refreshItems() {
        // Load items
        mDataList.clear();
        mAdapter.setLoadListenerBlock(true);
        mAdapter.setCurrentPageToZero();

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventListCallback);
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
                //intent = new Intent(getApplicationContext(),HomeTabActivity.class);
                //startActivity(intent);
                //finish();
                break;
            case R.id.tv_gallery:
                intent = new Intent(this, GalleryTabActivity.class);
                intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
            case R.id.fab_search:
                if (!Utils.isLevelValid(Const.LEVEL_PEEKER)) {
                    Utils.showInvalidLevelDialog(mContext);
                } else {
                    intent = new Intent(this, EventSearchActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    // TODO: 2016-10-28 HomeTabContentsListData 수정하기 -> 서버에서 넘어온것들 저장할 용도! 특히 인덱스
    private void addList(String title, String date, String place, int capacity, String url, int id) {
        String tmp[] = date.split("-");
        date = tmp[1] + "/" + tmp[2].substring(0, 2); // 2016-10-19을 10/19형식으로
        // TODO: 2016-10-28 메인이미지 url없음 -> 수정요청!
        //String place = CommonData.mPlaceList.get(placeId - 1).name;
        mDataList.add(new HomeTabContentsListData(title, date, place, capacity + "", url, id));
    }

    private NetManager.Callbacks mNetManagerEventListCallback = new NetManager.Callbacks() {
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
                        String title = object.getString("title");
                        String startDate = object.getString("start");
                        //int placeId = object.getInt("place_id");
                        String place = object.getString("place");
                        int capacity = object.getInt("capacity");
                        String url = object.getString("main_img");

                        if (object.has("deleted")) {
                            int deleted = object.getInt("deleted");
                            if (deleted == 0) {
                                //삭제되지 않았을 경우
                                addList(title, startDate, place, capacity, url, id);
                            }
                        }
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

    private String getLevel() {
        return CommonData.LoginUserData.level;
    }
}
