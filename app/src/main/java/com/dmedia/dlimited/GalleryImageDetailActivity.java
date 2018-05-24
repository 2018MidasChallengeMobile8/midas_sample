package com.dmedia.dlimited;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dmedia.dlimited.GalleryTabContentAdapter.MODE_GALLERY;
import static com.dmedia.dlimited.GalleryTabContentAdapter.MODE_MEMBER;

/**
 * Created by xema0 on 2016-11-02.
 */

public class GalleryImageDetailActivity extends AppCompatActivity {
    Context mContext;

    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;

    private ArrayList<GalleryTabContentData> mListData;
    private int position;
    private final int count = Const.PAGE_SIZE_GALLERY_ITEMS;
    private int page;

    private int mode = MODE_MEMBER;

    private boolean isPageFinished = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_image_detail);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("갤러리");

        if (getIntent() != null) {
            mListData = getIntent().getParcelableArrayListExtra("data_list");
            position = getIntent().getIntExtra("position", 0);
            page = getIntent().getIntExtra("page", 0) + 1;
            mode = getIntent().getIntExtra("mode", MODE_MEMBER);

            mViewPager = (ViewPager) findViewById(R.id.vp_image);
            mPagerAdapter = new MyPagerAdapter(this);
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(position);
        }
    }

    public class MyPagerAdapter extends PagerAdapter {
        private LayoutInflater mLayoutInflater;
        private ImageView mDetailImageView;

        public MyPagerAdapter(Context context) {
            super();
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = mLayoutInflater.inflate(R.layout.fragment_gallery_image_detail, null);

            if ((position == mListData.size() - 2) && !isPageFinished && (mode == MODE_GALLERY)) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("page", page + 1);
                params.put("count", count);
                NetData netData = new NetData(NetData.ProtocolType.GALLERY_LIST, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                NetManager netManager = new NetManager(netData, mContext);
                netManager.setCallback(mNetManagerGalleryListCallback);
                netManager.execute((Void) null);
            }

            mDetailImageView = (ImageView) v.findViewById(R.id.iv_detail_image);
            Glide.with(mContext).load(mListData.get(position).getUrl()).crossFade(0).into(mDetailImageView);

            ((ViewPager) container).addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
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
                if (resultCode == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("gallery_list");
                    if (jsonArray.length() != 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            int id = object.getInt("id");
                            String url = object.getString("img_url");
                            String type = object.getString("type");
                            int sort = object.getInt("sort");
                            String cDate = object.getString("cdate");
                            GalleryTabContentData item = new GalleryTabContentData(id, url, type, sort, cDate);
                            mListData.add(item);
                        }
                        page++;
                        mPagerAdapter.notifyDataSetChanged();
                    } else {
                        isPageFinished = true;
                    }
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };
}
