package com.dmedia.dlimited.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.util.BackPressCloseHandler;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.InstagramApp;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.fragment.MyPageTabDMoneyFragment;
import com.dmedia.dlimited.fragment.MyPageTabEventFragment;
import com.dmedia.dlimited.fragment.MyPageTabTicketFragment;
import com.dmedia.dlimited.widget.GrayScaleImageView;
import com.dmedia.dlimited.widget.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by min on 2016-09-28.
 */
// TODO: 2016-10-11 탭을 사진 안쪽으로 들어가도록 구현(아래쪽에)
// TODO: 2016-10-11 프로필 설명 글자수 제한을 두거나 스크롤 되게 해야할듯(넘어가지 않도록)

// TODO: 2016-10-13 프래그먼트 안에 버튼들 눌렀을때 색상 바뀌게하고, 텍스트컬러도 바꾸기
// TODO: 2016-10-13 마이페이지의 d-money에 관한 정보가 없음. - 애셋이랑 기획 물어보기

// TODO: 2016-10-13 로고툴바와 탭 4개(home, gallery,...)도 툴바에 포함시켜서 collapse시키고, 위로 스크롤했을때 빼꼼하고 나오도록 구현
public class MyPageTabActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MyPageTabActivity";
    private BackPressCloseHandler backPressCloseHandler;

    Context mContext;

    private TextView mHomeTabTextView;
    private TextView mGalleryTabTextView;
    private TextView mDBoxTabTextView;
    private TextView mMyPageTabTextView;

    private ViewPager mViewPager;
    private ArrayList<String> mCategoryList;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mCategoryTabLayout;

    private RoundedImageView mProfileRoundedImageView;
    private GrayScaleImageView mProfileBackgroundImageView;

    private TextView mProfileEditTextView;

    private TextView mNameTextView;
    private TextView mCommentTextView;

    private InstagramApp mApp;
    private boolean instaAuthCheck = false;

    private void showCoachMark() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_coach_mark_my_page);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        View masterView = dialog.findViewById(R.id.rl_layout);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(HomeTabActivity.PREFERENCE_COACH_MARK_MY_PAGE, "checked");
                editor.commit();
            }
        });
        dialog.show();
    }

    InstagramApp.OAuthAuthenticationListener instagramListener = new InstagramApp.OAuthAuthenticationListener() {
        @Override
        public void onSuccess() {
            HashMap<String, Object> params = new HashMap<>();
            params.put("userid", CommonData.LoginUserData.userId);
            params.put("session_token", CommonData.LoginUserData.loginToken);
            params.put("username", CommonData.LoginUserData.userName);
            params.put("gender", CommonData.LoginUserData.gender);
            params.put("birthday", CommonData.LoginUserData.birthday);
            params.put("address", CommonData.LoginUserData.address);
            params.put("instagram", mApp.getId());
            params.put("instagram_name", mApp.getUserName());
            params.put("profile_img_url", mApp.getProfileUrl());
            params.put("comment", CommonData.LoginUserData.comment);
            NetData netData = new NetData(NetData.ProtocolType.USER_UPDATE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerUserUpdateCallback);
            netManager.execute((Void) null);
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(MyPageTabActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    // TODO: 2016-10-11 흑백 처리 리팩터링. 성능 향상하기.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage_tab);

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setLogo(R.drawable.p06_img_toplogo);
        setSupportActionBar(toolbar);

        //ActionBar a = getSupportActionBar();
        //a.setTitle("D-Limited");
        //a.setLogo(R.drawable.testicon);

        backPressCloseHandler = new BackPressCloseHandler(this);

        SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
        String check = prefs.getString(HomeTabActivity.PREFERENCE_COACH_MARK_MY_PAGE, "");
        if (check.equals("")) {
            showCoachMark();
        }

        mApp = new InstagramApp(this, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        mApp.setListener(instagramListener);

        mProfileEditTextView = (TextView) findViewById(R.id.tv_edit_profile);

        mHomeTabTextView = (TextView) findViewById(R.id.tv_home);
        mGalleryTabTextView = (TextView) findViewById(R.id.tv_gallery);
        mDBoxTabTextView = (TextView) findViewById(R.id.tv_dbox);
        mMyPageTabTextView = (TextView) findViewById(R.id.tv_mypage);

        mHomeTabTextView.setOnClickListener(this);
        mGalleryTabTextView.setOnClickListener(this);
        mDBoxTabTextView.setOnClickListener(this);
        mMyPageTabTextView.setOnClickListener(this);

        mNameTextView = (TextView) findViewById(R.id.tv_name);
        mCommentTextView = (TextView) findViewById(R.id.tv_comment);

        mNameTextView.setText(CommonData.LoginUserData.userName);


        mProfileRoundedImageView = (RoundedImageView) findViewById(R.id.riv_profile);
        mProfileBackgroundImageView = (GrayScaleImageView) findViewById(R.id.iv_profile_background);

        mCategoryList = new ArrayList<>();
        mCategoryList.add("EVENT");
        mCategoryList.add("PRODUCT");
        mCategoryList.add("D-MONEY");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        mCategoryTabLayout = (TabLayout) findViewById(R.id.tl_category);
        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mCategoryTabLayout.setupWithViewPager(mViewPager);

        // TODO: 2016-10-11 흑백,라운딩 처리 리팩토링
        //흑백 처리
        //Bitmap backGroundBitmap = Utils.drawableToBitmap(mProfileBackgroundImageView.getDrawable());
        //Bitmap grayScaleBitmap = Utils.getGrayImage(backGroundBitmap);
        //mProfileBackgroundImageView.setImageBitmap(grayScaleBitmap);

        //Glide.with(this).load(CommonData.LoginUserData.profileImgUrl).into(mProfileRoundedImageView);
        //Glide.with(this).load(CommonData.LoginUserData.profileImgUrl).into(mProfileBackgroundImageView);
        //라운딩 처리
        //mProfileRoundedImageView.setImageResource(R.drawable.p34_img_profile);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mypage_tab, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mNameTextView.setText(CommonData.LoginUserData.userName);
        mCommentTextView.setText(CommonData.LoginUserData.comment);
        // TODO: 2016-11-01 인스타그램 연동후 사진 바꾸기
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO: 2016-09-29 기능 구현
        if (id == R.id.menu_setting) {
            Intent intent = new Intent(MyPageTabActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        overridePendingTransition(0, 0);
        //한번에 검사할경우 가끔식 에러 발생... 차라리 따로 검사.

        boolean isInstaNull = false;
        if (CommonData.LoginUserData.instagramId.equals("null")) {
            isInstaNull = true;
        }
        if (CommonData.LoginUserData.instagramId == null) {
            isInstaNull = true;
        }
        if (CommonData.LoginUserData.instagramId.equals("")) {
            isInstaNull = true;
        }
        if (CommonData.LoginUserData.instagramId.equals(" ")) {
            isInstaNull = true;
        }
        if (isInstaNull) {
            mCommentTextView.setText("Instagram 연동이 필요합니다.");
            mProfileEditTextView.setText("instagram 연동");
            mProfileEditTextView.setOnClickListener(null);
            mProfileEditTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mApp.authorize();
                }
            });

        } else {
            mProfileEditTextView.setText("edit");
            mProfileEditTextView.setOnClickListener(null);
            mProfileEditTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyPageTabActivity.this, AccountEditProfileActivity.class);
                    startActivity(intent);
                }
            });

            boolean isCommentNull = false;
            if (CommonData.LoginUserData.comment.equals("null")) {
                isCommentNull = true;
            }
            if (CommonData.LoginUserData.comment == null) {
                isCommentNull = true;
            }
            if (CommonData.LoginUserData.comment.equals("")) {
                isCommentNull = true;
            }
            if (CommonData.LoginUserData.comment.equals(" ")) {
                isCommentNull = true;
            }
            if (CommonData.LoginUserData.comment.equals("None")) {
                isCommentNull = true;
            }
            if (CommonData.LoginUserData.comment.equals("none")) {
                isCommentNull = true;
            }

            if (isCommentNull) {
                mCommentTextView.setText("자기소개가 없습니다\nedit버튼을 눌러 프로필을 수정해주세요");
            } else {
                mCommentTextView.setText(CommonData.LoginUserData.comment);
            }
        }


        if (CommonData.LoginUserData.profileImgUrl.equals(getString(R.string.instagram_dummy_image_url))) {
            mProfileRoundedImageView.setImageResource(R.drawable.profile_male);
            mProfileBackgroundImageView.setImageResource(R.drawable.profile_male);
        } else {
            Glide.with(this).load(CommonData.LoginUserData.profileImgUrl).into(mProfileRoundedImageView);
            Glide.with(this).load(CommonData.LoginUserData.profileImgUrl).into(mProfileBackgroundImageView);
        }

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
                intent = new Intent(this, DBoxTabActivity.class);
                intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.tv_mypage:
                //intent = new Intent(getApplicationContext(),MyPageTabActivity.class);
                //startActivity(intent);
                //finish();
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Fragment mFragmentEvent;
        Fragment mFragmentTicket;
        Fragment mFragmentDMoney;


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentEvent = new MyPageTabEventFragment();
            mFragmentTicket = new MyPageTabTicketFragment();
            mFragmentDMoney = new MyPageTabDMoneyFragment();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = mFragmentEvent;
                    break;
                case 1:
                    fragment = mFragmentTicket;
                    break;
                case 2:
                    fragment = mFragmentDMoney;
                    break;
                default:
                    //에러 방지
                    fragment = new Fragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mCategoryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCategoryList.get(position).toString();
        }
    }

    private NetManager.Callbacks mNetManagerUserUpdateCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    CommonData.LoginUserData.instagramId = mApp.getId();
                    CommonData.LoginUserData.instagramName = mApp.getUserName();
                    CommonData.LoginUserData.profileImgUrl = mApp.getProfileUrl();

                    Toast.makeText(mContext, "연동되었습니다.", Toast.LENGTH_SHORT).show();
                    //액티비티 재시작
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }else {
                    Toast.makeText(mContext, "인스타그램 연동을 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
