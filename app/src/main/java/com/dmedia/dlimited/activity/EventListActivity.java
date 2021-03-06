package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.adapter.EventListAdapter;
import com.dmedia.dlimited.model.EventListData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by min on 2016-09-22.
 */
public class EventListActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;
    protected Handler handler;

    private RecyclerView mRecyclerView;
    private EventListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private ArrayList<EventListData> mDataList;

    private ImageView mEventStateImageView;

    int mCount = Const.PAGE_SIZE_SMALL_ITEMS;

    private LinearLayout mEmptyLayout;

    @Override
    protected void onResume() {
        super.onResume();
        getEventList();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("Event 개설 내역");
        //a.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        mContext = this;
        handler = new Handler();

        mEventStateImageView = (ImageView) findViewById(R.id.iv_event_state);
        mEventStateImageView.setOnClickListener(this);

        mEmptyLayout = (LinearLayout) findViewById(R.id.ll_empty);

        mLayoutManager = new LinearLayoutManager(EventListActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();
        mAdapter = new EventListAdapter(EventListActivity.this, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnLoadMoreListener(new EventListAdapter.OnLoadMoreListener() {
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
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        params.put("userid", CommonData.LoginUserData.userId);
                        NetData netData = new NetData(NetData.ProtocolType.USER_EVENT_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserEventListCallback);
                        netManager.execute((Void) null);
                        //or you can add all at once but do not forget to call mAdapter.notifyDataSetChanged();
                    }
                }, 500);
            }
        });


        //getEventList();
    }

    private void getEventList() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("page", 1);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        params.put("count", mCount);
        NetData netData = new NetData(NetData.ProtocolType.USER_EVENT_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerUserEventListCallback);
        netManager.execute((Void) null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_make_event) {
            Intent intent = new Intent(EventListActivity.this, EventMakeActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_event_list, menu);
        return true;
    }

    private void addList(int id, String title, String date, String state) {
        String tmpState = "";
        date = date.substring(0, 10);//시간까지 나오는것 방지
        if (state.equals("request")) {
            tmpState = "승인대기";
        } else if (state.equals("ready")) {
            tmpState = "진행대기";
        } else if (state.equals("open")) {
            tmpState = "진행중";
        } else if (state.equals("complete")) {
            tmpState = "완료";
        } else if (state.equals("cancel")) {
            tmpState = "취소";
        }
        mDataList.add(new EventListData(id, title, date, tmpState));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_event_state:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.event_list_dialog_state_title));
                builder.setMessage(getString(R.string.event_list_dialog_state_wait_permit) + "\n"
                        + getString(R.string.event_list_dialog_state_wait_progress) + "\n"
                        + getString(R.string.event_list_dialog_state_ongoing_progress) + "\n"
                        + getString(R.string.event_list_dialog_state_finish) + "\n"
                        + getString(R.string.event_list_dialog_state_cancel));
                builder.setPositiveButton("확인", null);
                builder.show();
                break;
            default:
                break;
        }
    }


    private NetManager.Callbacks mNetManagerUserEventListCallback = new NetManager.Callbacks() {
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
                    //성공
                    JSONArray jsonArray = jsonObject.getJSONArray("return_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String title = object.getString("title");
                        String startDate = object.getString("start");
                        String state = object.getString("status");
                        addList(id, title, startDate, state);
                    }

                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() == 0) {
                        mRecyclerView.setVisibility(RecyclerView.GONE);
                        mEmptyLayout.setVisibility(LinearLayout.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(RecyclerView.VISIBLE);
                        mEmptyLayout.setVisibility(LinearLayout.GONE);
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
