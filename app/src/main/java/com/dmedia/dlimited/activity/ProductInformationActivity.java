package com.dmedia.dlimited.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.adapter.DBoxTabContentsListAdapter;
import com.dmedia.dlimited.model.GroupMemberData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;
import com.dmedia.dlimited.fragment.ProductInformationTabGroupFragment;
import com.dmedia.dlimited.fragment.ProductInformationTabInformationFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by min on 2016-09-23.
 */
public class ProductInformationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProductInformationActivity";
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

    private boolean isValidLevel;
    private boolean isValidInsta;

    private Button mApplyButton;
    private Bundle b;//페이저에 데이터 전달

    private int mode = DBoxTabContentsListAdapter.MODE_DEFAULT;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void showCoachMark() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_coach_mark_product);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        View masterView = dialog.findViewById(R.id.rl_layout);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(HomeTabActivity.PREFERENCE_COACH_MARK_PRODUCT, "checked");
                editor.commit();
            }
        });
        dialog.show();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);
        mContext = this;
        b = new Bundle();

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);

        SharedPreferences prefs = getSharedPreferences(HomeTabActivity.PREFERENCE_COACH_MARK, MODE_PRIVATE);
        String check = prefs.getString(HomeTabActivity.PREFERENCE_COACH_MARK_PRODUCT, "");
        if (check.equals("")) {
            showCoachMark();
        }

        title = getIntent().getStringExtra("product_title");
        if (title == null) {
            a.setTitle("Product 상세");
        } else {
            if (title.equals(""))
                a.setTitle("Product 상세");
            else
                a.setTitle(title);
        }
        dboxId = getIntent().getIntExtra("product_id", -1);
        if (getIntent().getIntExtra("mode", DBoxTabContentsListAdapter.MODE_DEFAULT) == DBoxTabContentsListAdapter.MODE_MYPAGE) {
            setMode( DBoxTabContentsListAdapter.MODE_MYPAGE);
        }

        mMainImageView = (ImageView) findViewById(R.id.iv_main);

        //getDBoxDetail();

        mCategoryList = new ArrayList<>();
        mCategoryList.add(getString(R.string.information_korean));
        mCategoryList.add(getString(R.string.group_korean));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        mCategoryTabLayout = (TabLayout) findViewById(R.id.tl_category);
        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //mCategoryTabLayout.setupWithViewPager(mViewPager);
        mApplyButton = (Button) findViewById(R.id.btn_apply);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApplyButton.setEnabled(false);

        getDBoxDetail();
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

    private void applyDBoxGuest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("확인");
        builder.setMessage("D-BOX에 신청하시겠습니까?");
        builder.setPositiveButton("신청", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mApplyButton.setOnClickListener(null);
                HashMap<String, Object> params = new HashMap<>();
                params.put("dbox_id", dboxId);
                params.put("userid", CommonData.LoginUserData.userId);
                params.put("session_token", CommonData.LoginUserData.loginToken);
                NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_APPLY, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                NetManager netManager = new NetManager(netData, mContext);
                netManager.setCallback(mNetManagerDBoxGuestApplyCallback);
                netManager.execute((Void) null);
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_apply) {

            /*
            if (!Utils.isLevelValid(Const.LEVEL_GUEST)) {
                Utils.showInvalidLevelDialog(mContext);
            } else {
                applyDBoxGuest();
            }
            */

            if (showInvalidLevelAndInstagramDialog(mContext)){
                applyDBoxGuest();
            }
        }
    }

    public boolean showInvalidLevelAndInstagramDialog(final Context context) {
        isValidLevel = false;
        isValidInsta = false;

        if (Utils.isLevelValid(Const.LEVEL_GUEST)) {
            isValidLevel = true;
        }
        if (Utils.isInstaValid()) {
            isValidInsta = true;
        }

        if (isValidInsta && isValidLevel) {
            return true;
        } else {
            String message = "";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("권한 부족");
            if (!isValidLevel) {
                message += "D-Code 인증\n";
            }
            if (!isValidInsta) {
                message += "Instagram 연동\n";
            }
            message += "\n참가신청을 하기 위해 위 항목을 마이페이지에서 설정해주세요.";
            builder.setMessage(message);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!isValidInsta) {
                        Intent intent = new Intent(ProductInformationActivity.this, MyPageTabActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if (!isValidLevel) {
                        Intent intent = new Intent(ProductInformationActivity.this, SettingsDcodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
            });
            builder.show();
            return false;
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Fragment mFragmentInformation;
        Fragment mFragmentGroup;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentInformation = new ProductInformationTabInformationFragment();
            mFragmentGroup = new ProductInformationTabGroupFragment();
            mFragmentInformation.setArguments(b);
            mFragmentGroup.setArguments(b);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //공유하기
        if (id == R.id.action_share) {
            Utils.kakaoShare(mContext, "[D-Limited 알림]\n\n" + CommonData.LoginUserData.userName + "님이 " + title + "이벤트를 공유하셨습니다.\n", mainImageUrl, "dbox", productId);
        } else if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private NetManager.Callbacks mNetManagerDBoxGuestApplyCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    mApplyButton.setBackgroundColor(Color.DKGRAY);
                    mApplyButton.setText("승인대기");
                    Toast.makeText(mContext, "신청 되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                } else {
                    mApplyButton.setOnClickListener(ProductInformationActivity.this);
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
                        ArrayList<String> imageUrlList = new ArrayList<>();
                        for (int i = 0; i < imgArray.length(); i++) {
                            JSONObject object = imgArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int dboxId = object.getInt("dbox_id");
                            String url = object.getString("img_url");
                            int sort = object.getInt("sort");
                            int isMain = object.getInt("is_main");//1(메인이미지), 0(메인이미지 아님)

                            if (isMain == 1) {
                                Glide.with(ProductInformationActivity.this).load(url).into(mMainImageView); //메인이미지는 액티비티에서 처리
                                mainImageUrl = url;
                            } else {
                                imageUrlList.add(url); //메인이미지 아닌것들 프래그먼트에서 처리
                            }
                        }
                        b.putStringArrayList("info_image_url", imageUrlList);
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
                        //ArrayList<ApplyListData> userList = new ArrayList<>();
                        for (int i = 0; i < userArray.length(); i++) {
                            JSONObject object = userArray.getJSONObject(i);
                            int id = object.getInt("id");
                            String userId = object.getString("user_id");
                            String username = object.getString("username");
                            String level = object.getString("level");
                            //String instagram = object.getString("instagram");
                            String profileImgUrl = object.getString("profile_img_url");

                            //userList.add(new GroupMemberData(id, eventId, username, level, "", profileImgUrl));
                            // TODO: 2016-11-04 서버 리스폰스에 인스타그램 존재x
                            // TODO: 2016-11-04 api에는 존재하지 않지만 실제로는 보내는듯..
                        }
                        //b.putParcelableArrayList("group_user_list", userList);
                    }

                    //통신완료 후에 뷰페이저 생성.
                    mSectionsPagerAdapter = new ProductInformationActivity.SectionsPagerAdapter(getSupportFragmentManager());
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                    //카테고리 탭 attach
                    mCategoryTabLayout.setupWithViewPager(mViewPager);

                    //신청버튼 제거하고 공간 확보
                    boolean isMine = jsonObject.getBoolean("is_mine");
                    int size = Math.round(getResources().getDimension(R.dimen.apply_button_padding));
                    if (isMine) {
                        mApplyButton.setVisibility(Button.GONE);
                        mViewPager.setPadding(size, size, size, size);
                    } else {
                        mApplyButton.setVisibility(Button.VISIBLE);
                        //밑에 여백을 주어서 버튼의 공간 확보
                        mViewPager.setPadding(size, size, size, size * 5);

                        String myStatus = jsonObject.getString("dbox_user_status");
                        mApplyButton.setEnabled(true);
                        switch (myStatus) {
                            case "not_request":
                                mApplyButton.setOnClickListener(ProductInformationActivity.this);
                                mApplyButton.setBackgroundColor(Color.parseColor("#724DCE"));
                                mApplyButton.setText("신청하기");
                                break;
                            case "request":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.DKGRAY);
                                mApplyButton.setText("승인대기");
                                break;
                            case "complete":
                                mApplyButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        useReward();
                                    }
                                });
                                mApplyButton.setBackgroundColor(Color.parseColor("#FFCC33"));
                                mApplyButton.setText("Product 사용");
                                break;
                            case "terminate":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.GRAY);
                                mApplyButton.setText("이벤트 종료");
                                break;
                            case "denied":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.BLACK);
                                mApplyButton.setText("승인 거절됨");
                                break;
                            case "reviewed":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.BLACK);
                                mApplyButton.setText("리뷰 완료");
                                break;
                            case "rewarded":
                                mApplyButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ProductInformationActivity.this, ProductReviewApplyActivity.class);
                                        intent.putExtra("dbox_id", dboxId);
                                        startActivity(intent);
                                    }
                                });
                                mApplyButton.setBackgroundColor(Color.parseColor("#EC407A"));
                                mApplyButton.setText("리뷰하기");
                                break;
                            default:
                                break;
                        }
                    }

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void useReward() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("수령 확인");
        builder.setMessage("상품을 수령하시겠습니까?");
        builder.setPositiveButton("수령", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("dbox_id", dboxId);
                params.put("dbox_userid", CommonData.LoginUserData.userId);
                params.put("session_token", CommonData.LoginUserData.loginToken);
                params.put("status", "rewarded");
                NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_CHANGE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                NetManager netManager = new NetManager(netData, mContext);
                netManager.setCallback(mNetManagerDBoxGuestChangeCallback);
                netManager.execute((Void) null);
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private NetManager.Callbacks mNetManagerDBoxGuestChangeCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Toast.makeText(mContext, "수령하였습니다", Toast.LENGTH_SHORT).show();
                    mApplyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ProductInformationActivity.this, ProductReviewApplyActivity.class);
                            intent.putExtra("dbox_id", dboxId);
                            startActivity(intent);
                        }
                    });
                    mApplyButton.setBackgroundColor(Color.parseColor("#EC407A"));
                    mApplyButton.setText("리뷰하기");
                } else {
                    mApplyButton.setOnClickListener(ProductInformationActivity.this);
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void finish() {
        if (mode == DBoxTabContentsListAdapter.MODE_DEFAULT) {
            Intent intent = new Intent(this, DBoxTabActivity.class);
            intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        super.finish();
    }
}
