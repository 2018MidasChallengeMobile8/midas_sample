package com.dmedia.dlimited;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_PASSWORD;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_UDID;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING_EVENT_ALARM;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING_INVITE_ALARM;

// TODO: 2016-11-05 버전 정보 받아오는 통신 구현
public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    private boolean debugMode = false;

    private ImageView mSloganImageView;
    //private ImageView mLogoImageView;

    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    //private Animation logoAnimation;

    private boolean isNetworkCheck = false;
    Context mContext;

    public static final String PREFERENCE_TUTORIAL = "preference_tutorial";
    public static final String PREFERENCE_WALK_THROUGH_CHECKED = "preference_walk_through_checked";

    private FirebaseAnalytics mFirebaseAnalystics;

    private boolean dboxCategoryCheck = false;
    private boolean dboxPlaceCheck = false;
    private boolean deviceCheck = false;
    private boolean dboxBannerCheck = false;
    private boolean introCheck = false;
    private boolean versionCheck = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        mContext = this;

        //버전 체크 - 서버에서 가져오는방식에서 마켓의 버전정보를 HTML파싱하는 방법으로 수정
        VersionChecker versionChecker = new VersionChecker();
        try {
            String storeVersion = versionChecker.execute().get();
            if (storeVersion != null) {
                String appVersion = Utils.getAppVersion(mContext);
                Log.d(TAG, ">>>>>>>>>> playstore version is " + storeVersion + ", " + appVersion + " :" + storeVersion.compareTo(appVersion));
                if (appVersion.compareTo(storeVersion) >= 0)
                    versionCheck = true;
                else {
                    versionCheck = false;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("최신버전이 아닙니다.");
                    dialog.setMessage("Google Play Store에서 업데이트해주세요.");
                    dialog.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                    return;
                }
            } else {
                //네트워크 상태 확인
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(getString(R.string.error_internet_connection_title));
                dialog.setMessage(getString(R.string.error_internet_connection_content));
                dialog.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.show();
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        mFirebaseAnalystics = FirebaseAnalytics.getInstance(this);

        if (debugMode) {
            Intent intent = new Intent(IntroActivity.this, AccountSignupActivity2.class);
            startActivity(intent);
            finish();
        } else {
            initialization();

            fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
            fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_fade_out);

            mSloganImageView = (ImageView) findViewById(R.id.iv_slogan);
            // TODO: 2016-10-19 순서 부여하는 방법이 리스너 넣는거말고 더 좋은 방법이 있을텐데...찾아보기
            fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    new CountDownTimer(500, 500) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            mSloganImageView.startAnimation(fadeOutAnimation);
                        }
                    }.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mSloganImageView.setVisibility(ImageView.GONE);
                    introCheck = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            mSloganImageView.startAnimation(fadeInAnimation);
        }
    }

    private void initialization() {
        new Thread() {
            @Override
            public void run() {
                if (FirebaseInstanceId.getInstance().getToken() == null) {
                    //앱 설치후 초기 실행
                    while (!(dboxCategoryCheck && dboxPlaceCheck && dboxBannerCheck && introCheck && versionCheck)) {

                    }
                    if (!isNetworkCheck) {
                        SharedPreferences workThrougnPrefs = getSharedPreferences(PREFERENCE_TUTORIAL, MODE_PRIVATE);
                        String check = workThrougnPrefs.getString(PREFERENCE_WALK_THROUGH_CHECKED, "");
                        if (check.contentEquals("")) {
                            //워크스루
                            Intent intent = new Intent(IntroActivity.this, WalkthroughActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                            String phone = prefs.getString(PREFERENCE_LOGIN_ID, "");
                            String password = prefs.getString(PREFERENCE_LOGIN_PASSWORD, "");
                            String udid = prefs.getString(PREFERENCE_LOGIN_UDID, "");
                            if (phone.contentEquals("")) {
                                Intent intent = new Intent(IntroActivity.this, AccountLoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // TODO: 2016-10-29 현재 user 정보 api가 정상작동x -> 로그인 한번더 하는 방식으로... 나중에 서버 수정요청해서 pwd저장 안하도록
                                //자동로그인
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("userid", phone);
                                params.put("password", password);
                                params.put("udid", udid);
                                NetData netData = new NetData(NetData.ProtocolType.USER_SIGNIN, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                                NetManager netManager = new NetManager(netData, IntroActivity.this);
                                netManager.setCallback(mNetManagerSignInCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    }
                } else {
                    //초기실행 x
                    while (!(dboxCategoryCheck && dboxPlaceCheck && dboxBannerCheck && deviceCheck && introCheck && versionCheck)) {

                    }
                    if (!isNetworkCheck) {
                        SharedPreferences workThrougnPrefs = getSharedPreferences(PREFERENCE_TUTORIAL, MODE_PRIVATE);
                        String check = workThrougnPrefs.getString(PREFERENCE_WALK_THROUGH_CHECKED, "");
                        if (check.contentEquals("")) {
                            //워크스루
                            Intent intent = new Intent(IntroActivity.this, WalkthroughActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                            String phone = prefs.getString(PREFERENCE_LOGIN_ID, "");
                            String password = prefs.getString(PREFERENCE_LOGIN_PASSWORD, "");
                            String udid = prefs.getString(PREFERENCE_LOGIN_UDID, "");
                            if (phone.contentEquals("")) {
                                Intent intent = new Intent(IntroActivity.this, AccountLoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // TODO: 2016-10-29 현재 user 정보 api가 정상작동x -> 로그인 한번더 하는 방식으로... 나중에 서버 수정요청해서 pwd저장 안하도록
                                //자동로그인
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("userid", phone);
                                params.put("password", password);
                                params.put("udid", udid);
                                NetData netData = new NetData(NetData.ProtocolType.USER_SIGNIN, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                                NetManager netManager = new NetManager(netData, IntroActivity.this);
                                netManager.setCallback(mNetManagerSignInCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    }

                }
            }
        }.start();

        HashMap<String, Object> params = new HashMap<>();
        NetData netData = new NetData(NetData.ProtocolType.DBOX_CATEGORY_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxCategoryCallback);
        netManager.execute((Void) null);

        if (FirebaseInstanceId.getInstance().getToken() != null) {
            params = new HashMap<>();
            netData = new NetData(NetData.ProtocolType.USER_DEVICE_PUT, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
            SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
            String phone = prefs.getString(PREFERENCE_LOGIN_ID, "");
            if (phone.contentEquals("") || prefs == null) {
                params.put("userid", "nophone");
            } else {
                params.put("userid", phone);
            }
            params.put("udid", Utils.getUDID(mContext));
            params.put("deviceToken", FirebaseInstanceId.getInstance().getToken());
            params.put("platform", "android");

            SharedPreferences prefsNoti = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
            String eventCheck = prefsNoti.getString(PREFERENCE_SETTING_EVENT_ALARM, "true");
            String inviteCheck = prefsNoti.getString(PREFERENCE_SETTING_INVITE_ALARM, "true");
            if (String.valueOf(eventCheck).equals(false)) {
                params.put("event_flag", 0);
            } else {
                params.put("event_flag", 1);
            }
            if (String.valueOf(inviteCheck).equals(false)) {
                params.put("invite_flag", 1);
            } else {
                params.put("invite_flag", 1);
            }
            netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerUserDevicePutCallback);
            netManager.execute((Void) null);
        }


        params = new HashMap<>();
        netData = new NetData(NetData.ProtocolType.DBOX_PLACE_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
        netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxPlaceCallback);
        netManager.execute((Void) null);

        params = new HashMap<>();
        netData = new NetData(NetData.ProtocolType.DBOX_BANNER_LIST, NetData.MethodType.GET, NetData.ProgressType.NONE, params);
        netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxBannerCallback);
        netManager.execute((Void) null);

        //FcmFirebaseInstanceIDService.sendRegistrationToServer(mContext,FirebaseInstanceId.getInstance().getToken());
        //Toast.makeText(mContext, FirebaseInstanceId.getInstance().getToken()+"", Toast.LENGTH_SHORT).show();
    }

    private NetManager.Callbacks mNetManagerDBoxCategoryCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                isNetworkCheck = true;
                return;
            }
            CommonData.mDBoxCategoryList = new ArrayList<>();
            CommonData.mDBoxCategoryNameList = new ArrayList<>();
            try {
                JSONArray jsonArray = jsonObject.getJSONArray("category_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    CommonData.mDBoxCategoryList.add(new CommonData.DBoxCategoryData(object.getInt("id"), object.getString("title")));
                    CommonData.mDBoxCategoryNameList.add(object.getString("title"));
                }
                dboxCategoryCheck = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private NetManager.Callbacks mNetManagerDBoxPlaceCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                isNetworkCheck = true;
                return;
            }
            try {
                if (jsonObject.getInt("result") == 1) {
                    JSONArray jsonArray = jsonObject.getJSONArray("place_list");
                    CommonData.mPlaceNameList = new ArrayList<>();
                    CommonData.mPlaceList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        CommonData.mPlaceList.add(new CommonData.PlaceData(object.getInt("id"), object.getString("name"), object.getString("phonenumber"), object.getString("address")));
                        CommonData.mPlaceNameList.add(object.getString("name"));
                    }
                    dboxPlaceCheck = true;
                } else {
                    Toast.makeText(mContext, "서버와의 통신중 에러가 발생했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private NetManager.Callbacks mNetManagerSignInCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                isNetworkCheck = true;
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    CommonData.LoginUserData.loginToken = jsonObject.getString("session_token");

                    JSONObject user = jsonObject.getJSONObject("user");
                    CommonData.LoginUserData.id = user.getInt("id");//회원 인덱스
                    CommonData.LoginUserData.userId = user.getString("userid");//아이디(전화번호)
                    CommonData.LoginUserData.userName = user.getString("username");//닉네임
                    CommonData.LoginUserData.gender = user.getString("gender");//성별
                    CommonData.LoginUserData.birthday = user.getString("birthday");//생일
                    CommonData.LoginUserData.address = user.getString("address");//지역
                    CommonData.LoginUserData.comment = user.getString("comment");//자기소개
                    CommonData.LoginUserData.instagramId = user.getString("instagram");//인스타 numeric 아이디
                    CommonData.LoginUserData.instagramName = user.getString("instagram_name");//인스타 아이디
                    CommonData.LoginUserData.profileImgUrl = user.getString("profile_img_url");//인스
                    if (user.has("dcode_id")) {
                        CommonData.LoginUserData.dcodeId = user.getInt("dcode_id");//D-Code 인덱스
                    }
                    CommonData.LoginUserData.dMoney = user.getInt("dmoney");//D-Money잔액
                    CommonData.LoginUserData.level = user.getString("level");//레벨(guest,h
                    CommonData.LoginUserData.barcode = user.getString("barcode");//D-Money
                    CommonData.LoginUserData.cDate = user.getString("cdate");//가입 시각
                    CommonData.LoginUserData.instaToken = jsonObject.getString("server_instagram_access_token");


                    if (getIntent() != null) {
                        Bundle b = getIntent().getExtras();
                        //if (b != null)
                        if (b != null
                                && getIntent().getExtras().containsKey("url")
                                && getIntent().getExtras().getString("url") != null
                                && !getIntent().getExtras().getString("url").equals("")) {
                            //푸쉬로 들어온 경우
                            //String uriStr = getIntent().getExtras().getString("url");
                            if (getIntent().getExtras().getString("url") != null) {
                                Uri schemeUri = Uri.parse(getIntent().getExtras().getString("url"));
                                String schemeHost = schemeUri.getHost();
                                Log.d("host", schemeHost);
                                Log.d("uri", schemeUri.toString());
                                if (schemeHost.equalsIgnoreCase("product_detail")) {
                                    if (schemeUri.getQuery() != null) {
                                        String productId = schemeUri.getQueryParameter("product_id");
                                        if (!productId.isEmpty()) {
                                            Intent intent = new Intent(IntroActivity.this, ProductInformationActivity.class);
                                            intent.putExtra("product_id", Integer.parseInt(schemeUri.getQueryParameter("product_id")));
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                } else if (schemeHost.equalsIgnoreCase("dbox_tab")) {
                                    Intent intent = new Intent(IntroActivity.this, DBoxTabActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (schemeHost.equalsIgnoreCase("update")) {
                                    Intent intent = new Intent(IntroActivity.this, SettingsVersionActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (schemeHost.equalsIgnoreCase("event_detail")) {
                                    if (schemeUri.getQuery() != null) {
                                        String eventId = schemeUri.getQueryParameter("event_id");
                                        if (!eventId.isEmpty()) {
                                            Intent intent = new Intent(IntroActivity.this, EventInformationActivity.class);
                                            intent.putExtra("event_id", Integer.parseInt(schemeUri.getQueryParameter("event_id")));
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                } else {
                                    //scheme host 일치하지 않거나 공지사항일때.
                                    Intent intent = new Intent(IntroActivity.this, HomeTabActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Intent intent = new Intent(IntroActivity.this, HomeTabActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Intent intent = new Intent(IntroActivity.this, HomeTabActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    /*
                    if (getIntent() != null) {
                        Uri uri = getIntent().getData();
                        if (uri != null) {
                            // TODO: 2016-11-14 액티비티 전환 마무리
                            // TODO: 2016-11-14 권한 이슈 해결하기
                            //카카오 링크로 들어온경우
                            if (uri.getQueryParameter("share").equals("true")) {
                                String type = uri.getQueryParameter("type");
                                int id = Integer.parseInt(uri.getQueryParameter("id"));

                                Intent intent = new Intent(IntroActivity.this, HomeTabActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Intent intent = new Intent(IntroActivity.this, HomeTabActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    */

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(IntroActivity.this, AccountLoginActivity.class);
                    startActivity(intent);
                    finish();
                } else if (resultCode == 2) {
                    Toast.makeText(mContext, "자동 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(IntroActivity.this, AccountLoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private NetManager.Callbacks mNetManagerUserDevicePutCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                isNetworkCheck = true;
                return;
            }
            try {
                if (jsonObject.getInt("result") == 1) {
                    String udid = Utils.getUDID(mContext);
                    CommonData.LoginUserData.UDID = udid;
                    SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_LOGIN_UDID, udid);
                    editor.commit();

                    deviceCheck = true;
                } else {
                    Toast.makeText(mContext, "서버와의 통신중 에러가 발생했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private NetManager.Callbacks mNetManagerDBoxBannerCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                isNetworkCheck = true;
                return;
            }
            Log.d(TAG, jsonObject.toString());
            try {
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    CommonData.mDBoxBannerList = new ArrayList<>();
                    JSONArray jsonArray = jsonObject.getJSONArray("banner_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        JSONArray imgArray = object.getJSONArray("img_list");
                        int imgId = 1;
                        String imgUrl = "";
                        if (imgArray.length() >= 1) {
                            imgId = imgArray.getJSONObject(0).getInt("id");
                            imgUrl = imgArray.getJSONObject(0).getString("img_url");
                        }

                        CommonData.mDBoxBannerList.add(new CommonData.DBoxBannerData(
                                object.getInt("id"),
                                object.getInt("dbox_id"),
                                imgId,
                                imgUrl
                        ));
                    }
                    dboxBannerCheck = true;
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}
