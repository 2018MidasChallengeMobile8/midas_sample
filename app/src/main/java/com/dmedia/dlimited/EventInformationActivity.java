package com.dmedia.dlimited;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.dmedia.dlimited.HomeTabActivity.PREFERENCE_COACH_MARK;
import static com.dmedia.dlimited.HomeTabActivity.PREFERENCE_COACH_MARK_EVENT;
import static com.dmedia.dlimited.HomeTabActivity.PREFERENCE_COACH_MARK_HOME;

/**
 * Created by xema0 on 2016-10-07.
 */

public class EventInformationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EventInformationActivity";
    Context mContext;

    private ViewPager mViewPager;
    private ArrayList<String> mCategoryList;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mCategoryTabLayout;


    private ImageView mMainImageView;
    private Button mApplyButton;

    private int eventId;
    private String eventTitle;
    private String mainImageUrl = "";

    private boolean isValidLevel;
    private boolean isValidInsta;

    private Bundle b;//페이저에 데이터 전달

    private int mode = HomeTabContentsListAdapter.MODE_DEFAULT;

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
        dialog.setContentView(R.layout.dialog_coach_mark_event);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        View masterView = dialog.findViewById(R.id.rl_layout);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                SharedPreferences prefs = getSharedPreferences(PREFERENCE_COACH_MARK, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PREFERENCE_COACH_MARK_EVENT, "checked");
                editor.commit();
            }
        });
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getEventDetail();
    }

    private void getEventDetail() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_DETAIL, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventDetailCallback);
        netManager.execute((Void) null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_information);
        mContext = this;
        b = new Bundle();

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_COACH_MARK, MODE_PRIVATE);
        String check = prefs.getString(PREFERENCE_COACH_MARK_EVENT, "");
        if (check.equals("")) {
            showCoachMark();
        }
        //SharedPreferences.Editor editor = prefs.edit();
        //editor.commit();

        eventTitle = getIntent().getStringExtra("event_title");
        if (eventTitle == null) {
            a.setTitle("Event 상세");
        } else {
            if (eventTitle.equals(""))
                a.setTitle("Event 상세");
            else
                a.setTitle(eventTitle);
        }
        eventId = getIntent().getIntExtra("event_id", -1);
        if (getIntent().getIntExtra("mode", HomeTabContentsListAdapter.MODE_DEFAULT) == HomeTabContentsListAdapter.MODE_MYPAGE) {
            setMode(HomeTabContentsListAdapter.MODE_MYPAGE);
        }

        mMainImageView = (ImageView) findViewById(R.id.iv_main);

        //getEventDetail();

        mCategoryList = new ArrayList<>();
        mCategoryList.add(getString(R.string.information_korean));
        mCategoryList.add(getString(R.string.group_korean));


        mViewPager = (ViewPager) findViewById(R.id.container);
        mCategoryTabLayout = (TabLayout) findViewById(R.id.tl_category);
        mCategoryTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mApplyButton = (Button) findViewById(R.id.btn_apply);

        // TODO: 2016-10-07 통신 이용해서 정보와 구성원들 받아오기 - fragment
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_apply) {

            /*
            if (!Utils.isLevelValid(Const.LEVEL_GUEST)) {
                Utils.showInvalidLevelDialog(mContext);
            } else {
                Intent intent = new Intent(EventInformationActivity.this, EventApplyActivity.class);
                intent.putExtra("event_id", eventId);
                intent.putExtra("event_title", eventTitle);
                startActivity(intent);
            }
            */
            if (showInvalidLevelAndInstagramDialog(mContext)) {
                Intent intent = new Intent(mContext, EventApplyActivity.class);
                intent.putExtra("event_id", eventId);
                intent.putExtra("event_title", eventTitle);
                startActivity(intent);
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
                        Intent intent = new Intent(EventInformationActivity.this, MyPageTabActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if (!isValidLevel) {
                        Intent intent = new Intent(EventInformationActivity.this, SettingsDcodeActivity.class);
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
            mFragmentInformation = new EventInformationTabInformationFragment();
            mFragmentGroup = new EventInformationTabGroupFragment();
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
        getMenuInflater().inflate(R.menu.menu_event_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Utils.kakaoShare(mContext, "[D-Limited 알림]\n\n" + CommonData.LoginUserData.userName + "님이 " + eventTitle + "이벤트를 공유하셨습니다.\n", mainImageUrl, "event", eventId);
        } else if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private NetManager.Callbacks mNetManagerEventDetailCallback = new NetManager.Callbacks() {
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
                    // TODO: 2016-10-31 서버에서 오타남 -> shar_url 고쳐달라고 부탁
                    String shareUrl = jsonObject.getString("shar_url");//공유하기 위한 url

                    JSONObject eventObject = jsonObject.getJSONObject("event");
                    {
                        int id = eventObject.getInt("id");
                        String title = eventObject.getString("title");
                        int placeId = eventObject.getInt("place_id");
                        String information = eventObject.getString("information");
                        String requirement = eventObject.getString("requirement");
                        String startDate = eventObject.getString("start");
                        String status = eventObject.getString("status");
                        int capacity = eventObject.getInt("capacity");
                        //int privateMode = eventObject.getInt("private");//공개(0),비공개(1) 설정
                        int viewCnt = eventObject.getInt("viewcnt");//조회수
                        int deleted = eventObject.getInt("deleted");
                        String cDate = eventObject.getString("cdate");//생성일자

                        //키값 형식 : 프래그먼트_내용
                        b.putInt("info_id", id);
                        b.putInt("info_place_id", placeId);
                        //b.putInt("info_private_mode",capacity);
                        b.putInt("info_view_cnt", viewCnt);
                        b.putInt("info_deleted", deleted);
                        b.putString("info_title", title);
                        b.putString("info_info", information);
                        b.putString("info_requirement", requirement);
                        b.putString("info_start_date", startDate);
                        b.putString("info_status", status);
                        b.putString("info_cdate", cDate);
                    }

                    JSONArray imgArray = jsonObject.getJSONArray("event_img_list");
                    {
                        ArrayList<String> imageUrlList = new ArrayList<>();
                        for (int i = 0; i < imgArray.length(); i++) {
                            JSONObject object = imgArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int eventId = object.getInt("event_id");
                            String url = object.getString("img_url");
                            int sort = object.getInt("sort");
                            int isMain = object.getInt("is_main");//1(메인이미지), 0(메인이미지 아님)

                            if (isMain == 1) {
                                mainImageUrl = url;
                                Glide.with(EventInformationActivity.this).load(url).into(mMainImageView); //메인이미지는 액티비티에서 처리
                            } else {
                                imageUrlList.add(url); //메인이미지 아닌것들 프래그먼트에서 처리
                            }
                        }
                        b.putStringArrayList("info_image_url", imageUrlList);
                    }

                    //메인게스트?
                    JSONArray eventModelUserArray = jsonObject.getJSONArray("event_model_user_list");
                    {
                        ArrayList<GroupMemberData> modelUserList = new ArrayList<>();
                        for (int i = 0; i < eventModelUserArray.length(); i++) {
                            JSONObject object = eventModelUserArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int eventId = object.getInt("event_id");
                            String username = object.getString("username");
                            String level = object.getString("level");
                            String instagram = object.getString("instagram");
                            String profileImgUrl = object.getString("profile_img_url");

                            modelUserList.add(new GroupMemberData(id, eventId, username, level, instagram, profileImgUrl));
                        }
                        b.putParcelableArrayList("group_model_list", modelUserList);
                    }

                    // TODO: 2016-11-04 홈 탭에서 바로 접근하는 컨텐츠들은 일반유저를 보여줄 필요가 없다?
                    //일반유저들?
                    JSONArray userArray = jsonObject.getJSONArray("user_list");
                    {
                        //ArrayList<GroupMemberData> userList = new ArrayList<>();
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
                    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
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

                        /*
                        ‘not_request’(미신청),‘request’(승인대기)
                          ’complete’(승인완료),’terminate’(이벤트 종료),
                          ’denied’(승인 거절됨)
                         */
                        String myStatus = jsonObject.getString("event_user_status");
                        switch (myStatus) {
                            case "not_request":
                                mApplyButton.setOnClickListener(EventInformationActivity.this);
                                mApplyButton.setBackgroundColor(Color.parseColor("#724DCE"));
                                mApplyButton.setText("신청하기");
                                break;
                            case "request":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.DKGRAY);
                                mApplyButton.setText("승인대기");
                                break;
                            case "complete":
                                mApplyButton.setOnClickListener(null);
                                mApplyButton.setBackgroundColor(Color.parseColor("#EC407A"));
                                mApplyButton.setText("승인완료");
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

    @Override
    public void finish() {
        if (mode == HomeTabContentsListAdapter.MODE_DEFAULT) {
            Intent intent = new Intent(this, HomeTabActivity.class);
            intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        super.finish();
    }
}
