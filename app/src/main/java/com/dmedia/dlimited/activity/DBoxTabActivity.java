package com.dmedia.dlimited.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.util.BackPressCloseHandler;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;
import com.dmedia.dlimited.fragment.DBoxTabContentFragment;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by min on 2016-09-25.
 */
// TODO: 2016-10-13 로고툴바와 탭 4개(home, gallery,...)도 툴바에 포함시켜서 collapse시키고, 위로 스크롤했을때 빼꼼하고 나오도록 구현
public class DBoxTabActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DBoxTabActivity";

    private BackPressCloseHandler backPressCloseHandler;

    private TextView mHomeTabTextView;
    private TextView mGalleryTabTextView;
    private TextView mDBoxTabTextView;
    private TextView mMyPageTabTextView;

    private ViewPager mViewPager;
    private ArrayList<CommonData.DBoxCategoryData> mCategoryList;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mCategoryTabLayout;

    private RelativeLayout mSearchRelativeLayout;

    private FloatingActionButton mUpFloationgActionButton;

    private AppBarLayout mAppBarLayout;

    private ImagePagerAdapter mImagePagerAdapter;
    private ViewPager mMainSliderPager;

    private void showCoachMark() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_coach_mark_dbox);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        View masterView = dialog.findViewById(R.id.rl_layout);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(HomeTabActivity.PREFERENCE_COACH_MARK_DBOX, "checked");
                editor.commit();
            }
        });
        dialog.show();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dbox_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        backPressCloseHandler = new BackPressCloseHandler(this);

        SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
        String check = prefs.getString(HomeTabActivity.PREFERENCE_COACH_MARK_DBOX, "");
        if (check.equals("")) {
            showCoachMark();
        }

        mHomeTabTextView = (TextView) findViewById(R.id.tv_home);
        mGalleryTabTextView = (TextView) findViewById(R.id.tv_gallery);
        mDBoxTabTextView = (TextView) findViewById(R.id.tv_dbox);
        mMyPageTabTextView = (TextView) findViewById(R.id.tv_mypage);
        mUpFloationgActionButton = (FloatingActionButton) findViewById(R.id.fab_up);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        mHomeTabTextView.setOnClickListener(this);
        mGalleryTabTextView.setOnClickListener(this);
        mDBoxTabTextView.setOnClickListener(this);
        mMyPageTabTextView.setOnClickListener(this);
        mUpFloationgActionButton.setOnClickListener(this);

        mCategoryList = CommonData.mDBoxCategoryList;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mCategoryTabLayout = (TabLayout) findViewById(R.id.tl_category);
        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        mCategoryTabLayout.setupWithViewPager(mViewPager);

        mSearchRelativeLayout = (RelativeLayout) findViewById(R.id.rl_search);
        mSearchRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DBoxTabActivity.this, ProductSearchActivity.class);
                startActivity(intent);
            }
        });


        // image slider
        mImagePagerAdapter = new ImagePagerAdapter(this);
        mMainSliderPager = (ViewPager) findViewById(R.id.vp_mainslider);
        mMainSliderPager.setAdapter(mImagePagerAdapter);

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mMainSliderPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utils.isLevelValid(Const.LEVEL_HOST)) {
            getMenuInflater().inflate(R.menu.menu_dbox_tab_host, menu);
        } else if (Utils.isLevelValid(Const.LEVEL_PEEKER)) {
            getMenuInflater().inflate(R.menu.menu_dbox_tab_guest, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //문의하기
        if (id == R.id.menu_inquiry) {
            Intent intent = new Intent(DBoxTabActivity.this, SettingsInquiryActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_dbox_list) {
            Intent intent = new Intent(DBoxTabActivity.this, ProductListActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
                intent = new Intent(this, GalleryTabActivity.class);
                intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.tv_dbox:
                //intent = new Intent(getApplicationContext(),DBoxTabActivity.class);
                //startActivity(intent);
                //finish();
                break;
            case R.id.tv_mypage:
                intent = new Intent(this, MyPageTabActivity.class);
                intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.fab_up:
                mAppBarLayout.setExpanded(true);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment mDBoxContentsListFragment = new DBoxTabContentFragment();
            Bundle b = new Bundle();
            b.putInt("category_id", CommonData.mDBoxCategoryList.get(position).id);
            mDBoxContentsListFragment.setArguments(b);
            return mDBoxContentsListFragment;

        }

        @Override
        public int getCount() {
            return mCategoryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCategoryList.get(position).title;
        }
    }


    class ImagePagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public ImagePagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return CommonData.mDBoxBannerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.viewpager_mainslider_image, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.iv_contents_img);
            Glide.with(mContext)
                    .load(CommonData.mDBoxBannerList.get(position).url)
                    .into(imageView);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
