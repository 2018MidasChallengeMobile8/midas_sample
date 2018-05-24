package com.dmedia.dlimited.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xema0 on 2016-10-21.
 */

public class SettingsFaqActivity extends AppCompatActivity {
    Context mContext;

    private ExpandableListView mNoticeListView;
    private ExpandableListAdapter mNoticeListAdapter;
    private List<String> mNoticeTitleList;
    private HashMap<String, List<String>> mNoticeContentList;

    private EditText mSearchEditText;
    private ImageView mSearchImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_faq);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("도움말");

        mSearchEditText = (EditText) findViewById(R.id.edt_search);
        mSearchImageView = (ImageView) findViewById(R.id.iv_search);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.search || id == EditorInfo.IME_NULL) {
                    attemptSearch(mSearchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSearch(mSearchEditText.getText().toString());
            }
        });


        mNoticeListView = (ExpandableListView) findViewById(R.id.elv_qna);
        mNoticeListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        mNoticeListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
            }
        });

        mNoticeListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });

        HashMap<String, Object> params = new HashMap<>();
        NetData netData = new NetData(NetData.ProtocolType.USER_FAQ, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, this);
        netManager.setCallback(mNetManagerUserFaqCallback);
        netManager.execute((Void) null);
    }

    // TODO: 2016-11-13 타이틀 1개에 여러개가 들어갈수 있어야하는데 최대 1개로 제한되어있음 
    // TODO: 2016-11-13 나중에 수정
    public void attemptSearch(String keyword) {
        //타이틀 검색
        for (int i = 0; i < mNoticeTitleList.size(); i++) {
            if (mNoticeTitleList.get(i).contains(keyword)) {
                mNoticeListView.expandGroup(i);
                mNoticeListView.setSelection(i);
                mNoticeListView.requestFocus();
            }
        }
        Toast.makeText(mContext, "검색이 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private NetManager.Callbacks mNetManagerUserFaqCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("faq");
                    if (jsonArray.length() == 0) {
                        return;
                    }
                    mNoticeContentList = new HashMap<String, List<String>>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        List<String> noticeContent = new ArrayList<String>();
                        noticeContent.add(object.getString("text"));
                        mNoticeContentList.put(object.getString("title"), noticeContent);
                    }
                } else {
                    Toast.makeText(mContext, "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mNoticeTitleList = new ArrayList<>(mNoticeContentList.keySet());
            mNoticeListAdapter = new CustomExpandableListAdapter(mContext, mNoticeTitleList, mNoticeContentList);
            mNoticeListView.setAdapter(mNoticeListAdapter);
        }
    };

    public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> expandableListTitle;
        private HashMap<String, List<String>> expandableListDetail;

        public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                           HashMap<String, List<String>> expandableListDetail) {
            this.context = context;
            this.expandableListTitle = expandableListTitle;
            this.expandableListDetail = expandableListDetail;
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                    .get(expandedListPosition);
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = (String) getChild(listPosition, expandedListPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expandablelist_item, null);
            }
            TextView expandedListTextView = (TextView) convertView.findViewById(R.id.tv_content);
            expandedListTextView.setText(expandedListText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                    .size();
        }

        @Override
        public Object getGroup(int listPosition) {
            return this.expandableListTitle.get(listPosition);
        }

        @Override
        public int getGroupCount() {
            return this.expandableListTitle.size();
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expandablelist_group, null);
            }
            TextView listTitleTextView = (TextView) convertView
                    .findViewById(R.id.tv_title);
            //listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }
    }
}
