package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.ApplyListData;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.model.GroupMemberData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.model.SubImageData;
import com.dmedia.dlimited.util.Utils;
import com.dmedia.dlimited.fragment.ProductListDetailTabGroupFragment;
import com.dmedia.dlimited.fragment.ProductListDetailTabInformationFragment;
import com.dmedia.dlimited.fragment.ProductListDetailTabPermitFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//ExpandableListView인지 아니면 액티비티를 새로 띄우는것인지
//'information'는 텍스트뷰인지 에딧텍스트인지
//사람들 뷰 옆에있는 '공개','승인' ->버튼? or 텍스트뷰? 배경 색깔이 바뀌는 조건은?
//초대하기 버튼이 이미 가입되어있는 멤버를 초대하는건지 외부인을 초대하는건지 질문.
//밑에 사람들이 보이는 조건은?

/**
 * Created by min on 2016-09-23.
 */
public class ProductListDetailActivity extends AppCompatActivity {
    Context mContext;

    private String title;
    private int dboxId;

    private ViewPager mViewPager;
    private ArrayList<String> mCategoryList;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mCategoryTabLayout;
    private int productId;

    private String mainImageUrl;

    private ImageView mMainImageView;

    private String mStatus = "request";

    private Bundle b;//페이저에 데이터 전달

    public static int page = 0;

    private String bundleStartDateAndTime;
    private String bundleEndDateAndTime;
    private int bundleLimit;
    private int bundlePlaceId;

    private boolean networkCheck = false;
    private MenuItem mModifyOptionMenuItem;

    @Override
    protected void onResume() {
        super.onResume();
        getDBoxDetail();
    }

    @Override
    protected void onPause() {
        super.onPause();

        page = mViewPager.getCurrentItem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        page = 0;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list_detail);

        mContext = this;
        b = new Bundle();

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);

        title = getIntent().getStringExtra("product_title");
        if (title.equals("") || title == null) {
            a.setTitle("Product 상세");
        } else {
            a.setTitle(title);
        }
        dboxId = getIntent().getIntExtra("product_id", -1);

        mMainImageView = (ImageView) findViewById(R.id.iv_main);

        //getDBoxDetail();

        mCategoryList = new ArrayList<>();
        mCategoryList.add(getString(R.string.information_korean));
        mCategoryList.add(getString(R.string.group_korean));
        mCategoryList.add(getString(R.string.permit_korean));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);

        mCategoryTabLayout = (TabLayout) findViewById(R.id.tl_category);
        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //mCategoryTabLayout.setupWithViewPager(mViewPager);
    }

    private void getDBoxDetail() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("dbox_id", dboxId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_DETAIL, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxDetailCallback);
        netManager.execute((Void) null);
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Fragment mFragmentInformation;
        Fragment mFragmentGroup;
        Fragment mFragmentPermit;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentInformation = new ProductListDetailTabInformationFragment();
            mFragmentGroup = new ProductListDetailTabGroupFragment();
            mFragmentPermit = new ProductListDetailTabPermitFragment();
            mFragmentInformation.setArguments(b);
            mFragmentGroup.setArguments(b);
            mFragmentPermit.setArguments(b);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = mFragmentInformation;
                    break;
                case 1:
                    fragment = mFragmentGroup;
                    break;
                case 2:
                    fragment = mFragmentPermit;
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mModifyOptionMenuItem = menu.findItem(R.id.action_modify);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        return true;
    }

    public void dateCompare(String eventDayStr) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
        String currentDayStr = fm.format(new Date());
        try {
            Date eventDate = fm.parse(eventDayStr);
            Date currentDate = fm.parse(currentDayStr);
            if (eventDate.compareTo(currentDate) >= 0) {
                mModifyOptionMenuItem.setVisible(true);
            } else {
                mModifyOptionMenuItem.setVisible(false);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //수정하기
        if (id == R.id.action_modify) {
            if (networkCheck) {
                Intent intent = new Intent(ProductListDetailActivity.this, ProductModifyActivity.class);
                intent.putExtra("dbox_id", dboxId);
                intent.putExtra("data", b);

                intent.putExtra("start_date_and_time", bundleStartDateAndTime);
                intent.putExtra("end_date_and_time", bundleEndDateAndTime);
                intent.putExtra("limit", bundleLimit);
                intent.putExtra("place_id", bundlePlaceId);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, "통신 완료까지 기다려주세요", Toast.LENGTH_SHORT).show();
            }
        }
        //공유하기
        else if (id == R.id.action_share) {
            if ((mStatus.equals("request")) || (mStatus.equals("cancel"))) {
                Toast.makeText(mContext, "승인 완료된 프로덕트만 공유 가능합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Utils.kakaoShare(mContext, "[D-Limited 알림]\n\n" + CommonData.LoginUserData.userName + "님이 [" + title + "] 프로덕트를 공유하셨습니다.\n", mainImageUrl, "dbox", productId);
            }
            //리뷰목록
        } else if (id == R.id.action_review_list) {
            Intent intent = new Intent(ProductListDetailActivity.this, ProductReviewListActivity.class);
            intent.putExtra("dbox_id", dboxId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private NetManager.Callbacks mNetManagerDBoxDetailCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    int applyUserCnt = jsonObject.getInt("apply_user_cnt");//신청 대기자 수
                    b.putInt("apply_user_cnt", applyUserCnt);

                    JSONObject productObject = jsonObject.getJSONObject("dbox");
                    {
                        int id = productObject.getInt("id");
                        int categoryId = productObject.getInt("dbox_category_id");
                        String title = productObject.getString("title");
                        int placeId = productObject.getInt("place_id");
                        String place = productObject.getString("place");
                        String information = productObject.getString("information");
                        String mission = productObject.getString("mission");
                        String startDate = productObject.getString("start");
                        String endDate = productObject.getString("end");
                        String status = productObject.getString("status");
                        int capacity = productObject.getInt("capacity");
                        int viewCnt = productObject.getInt("viewcnt");//조회수
                        int deleted = productObject.getInt("is_deleted");
                        String cDate = productObject.getString("cdate");//생성일자

                        mStatus = status;

                        dateCompare(startDate.substring(0, 10));

                        bundleStartDateAndTime = startDate;
                        bundleEndDateAndTime = endDate;
                        bundleLimit = capacity;
                        bundlePlaceId = placeId;

                        b.putInt("info_id", id);
                        b.putInt("info_place_id", placeId);
                        b.putInt("info_view_cnt", viewCnt);
                        b.putInt("info_deleted", deleted);
                        b.putString("info_title", title);
                        b.putString("info_info", information);
                        b.putString("info_mission", mission);
                        b.putString("info_start_date", startDate);
                        b.putString("info_end_date", endDate);
                        b.putString("info_status", status);
                        b.putString("info_cdate", cDate);

                        productId = id;
                    }

                    JSONArray imgArray = productObject.getJSONArray("img_list");
                    {
                        ArrayList<SubImageData> subImageList = new ArrayList<>();
                        ArrayList<String> imageUrlList = new ArrayList<>();
                        for (int i = 0; i < imgArray.length(); i++) {
                            JSONObject object = imgArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int dboxId = object.getInt("dbox_id");
                            String url = object.getString("img_url");
                            int sort = object.getInt("sort");
                            int isMain = object.getInt("is_main");//1(메인이미지), 0(메인이미지 아님)

                            if (isMain == 1) {
                                b.putInt("main_image_id", id);
                                b.putString("main_image_url", url);
                                Glide.with(ProductListDetailActivity.this).load(url).into(mMainImageView); //메인이미지는 액티비티에서 처리
                                mainImageUrl = url;
                            } else {
                                imageUrlList.add(url); //메인이미지 아닌것들 프래그먼트에서 처리
                                subImageList.add(new SubImageData(id, dboxId, url, false, false));
                            }
                        }
                        b.putStringArrayList("info_image_url", imageUrlList);
                        b.putParcelableArrayList("sub_image_data", subImageList);
                    }

                    //메인게스트?
                    JSONArray dboxModelUserArray = jsonObject.getJSONArray("dbox_model_user_list");
                    {
                        ArrayList<GroupMemberData> modelUserList = new ArrayList<>();
                        for (int i = 0; i < dboxModelUserArray.length(); i++) {
                            JSONObject object = dboxModelUserArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int dboxId = object.getInt("dbox_id");
                            String username = object.getString("username");
                            String level = object.getString("level");
                            String instagram = object.getString("instagram");
                            String profileImgUrl = object.getString("profile_img_url");

                            modelUserList.add(new GroupMemberData(id, dboxId, username, level, instagram, profileImgUrl));
                        }
                        b.putParcelableArrayList("group_model_list", modelUserList);
                    }

                    // TODO: 2016-11-04 홈 탭에서 바로 접근하는 컨텐츠들은 일반유저를 보여줄 필요가 없다?
                    //일반유저들?
                    JSONArray userArray = jsonObject.getJSONArray("user_list");
                    {
                        ArrayList<ApplyListData> userList = new ArrayList<>();
                        for (int i = 0; i < userArray.length(); i++) {
                            JSONObject object = userArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int dboxId = object.getInt("dbox_id");
                            int userId = object.getInt("user_id");
                            String username = object.getString("username");
                            String instagram = object.getString("instagram");
                            String instagramName = object.getString("instagram_name");
                            String phone = object.getString("userid");
                            String status = object.getString("status");
                            String cDate = object.getString("cdate");
                            String level = object.getString("level");
                            String profileImgUrl = object.getString("profile_img_url");

                            // TODO: 2016-11-07 리스폰스에 comment는 없지만 상관 x
                            userList.add(new ApplyListData(id, dboxId, userId, username, instagram, instagramName, phone, "", status, cDate, profileImgUrl));
                        }
                        b.putParcelableArrayList("permit_user_list", userList);
                    }


                    //통신완료 후에 뷰페이저 생성.
                    mSectionsPagerAdapter = new ProductListDetailActivity.SectionsPagerAdapter(getSupportFragmentManager());
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                    //카테고리 탭 attach
                    mCategoryTabLayout.setupWithViewPager(mViewPager);

                    mViewPager.setCurrentItem(page);

                    networkCheck = true;

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
