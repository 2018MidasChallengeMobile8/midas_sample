package com.dmedia.dlimited.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.adapter.GalleryTabContentAdapter;
import com.dmedia.dlimited.model.GalleryTabContentData;
import com.dmedia.dlimited.network.InstagramApp;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.widget.RoundedImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by xema0 on 2016-10-05.
 */
//멤버 뷰 액티비티
// TODO: 2016-11-07 인스타그램 사진 연동.


// TODO: 2016-10-05 인스타그램 사진 연동, 닉네임 누르면 인스타 계정으로 이동. 
// TODO: 2016-10-05 승인버튼에 리스너 부착.

// TODO: 2016-10-05 멤버 뷰 레이아웃에 있는 나이나 성별... 이런 데이터들 뭐뭐 필요하지 물어보고 재설정하기.(번들로 받아오기.) - 아니면 서버에서 받아오는건가?
public class MemberViewActivity extends AppCompatActivity {
    Context mContext;
    private RoundedImageView mIconImageView;
    private TextView mNicknameTextView;
    private TextView mGenderAndAgeTextView;
    private TextView mCommentTextView;
    private Button mPermitButton;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    ArrayList<GalleryTabContentData> mDataList;
    public GalleryTabContentAdapter mAdapter;

    private int year;

    private int eventId;
    private String userPhone;
    private String instaId;

    private InstagramApp mApp;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_view);
        mContext = this;

        if (getIntent() != null) {
            eventId = getIntent().getIntExtra("event_id", 0);
            userPhone = getIntent().getStringExtra("user_phone");
            instaId = getIntent().getStringExtra("insta_id");
        }

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);

        mApp = new InstagramApp(mContext, mContext.getString(R.string.insta_client_id), mContext.getString(R.string.insta_client_secret), mContext.getString(R.string.insta_redirect_url));
        mApp.setListener(instagramListener);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("회원 정보");

        mIconImageView = (RoundedImageView) findViewById(R.id.riv_icon);
        mNicknameTextView = (TextView) findViewById(R.id.tv_name);
        mGenderAndAgeTextView = (TextView) findViewById(R.id.tv_gender_and_age);
        mCommentTextView = (TextView) findViewById(R.id.tv_comment);
        mPermitButton = (Button) findViewById(R.id.btn_permit);

        HashMap<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("userid", userPhone);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_DETAIL, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventUserDetailCallback);
        netManager.execute((Void) null);

        mLayoutManager = new GridLayoutManager(this, 3);//한 행에 3개씩
        mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true); // Helps improve performance

        mDataList = new ArrayList<>();

        mAdapter = new GalleryTabContentAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(GalleryTabContentAdapter.MODE_MEMBER);
        mRecyclerView.setAdapter(mAdapter);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams instaParams = new RequestParams();
        instaParams.put("access_token", CommonData.LoginUserData.instaToken);
        instaParams.put("count", Const.PAGE_SIZE_GALLERY_ITEMS);
        client.get(mContext, "https://api.instagram.com/v1/users/" + instaId + "/media/recent/", instaParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        String url = jsonObject.getJSONObject("images").getJSONObject("standard_resolution").getString("url");

                        mDataList.add(new GalleryTabContentData(0, url, "place", ""));
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                try {
                    if (errorResponse.getJSONObject("meta").getString("error_type").equals("OAuthAccessTokenException")) {
                        //Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        //mApp.authorize();
                    } else {
                        //mApp.authorize();
                        Log.d("res", errorResponse.toString());
                        //Toast.makeText(mContext, errorResponse+"", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private NetManager.Callbacks mNetManagerEventUserDetailCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    JSONObject eventUserObject = jsonObject.getJSONObject("event_user");
                    final int eventUserId = eventUserObject.getInt("id"); //event_user table id
                    int eventId = eventUserObject.getInt("event_id");//event table id
                    int userId = eventUserObject.getInt("user_id");//user table id
                    String instagram = eventUserObject.getString("instagram");//인스타 아이디
                    String phone = eventUserObject.getString("phonenumber");//전화번호
                    String comment = eventUserObject.getString("comment");//참가신청시 작성한 글
                    String status = eventUserObject.getString("status");//'request','complete'
                    String cDate = eventUserObject.getString("cdate");//참가신청일자

                    JSONObject userObject = jsonObject.getJSONObject("user");
                    String userName = userObject.getString("username");
                    String gender = userObject.getString("gender");
                    String birth = userObject.getString("birthday");
                    String profileUrl = userObject.getString("profile_img_url");

                    if ((profileUrl == null) || (profileUrl.equals("null")) || (profileUrl.equals(""))) {
                        mIconImageView.setImageResource(R.drawable.profile_male);
                    } else {
                        Glide.with(mContext).load(profileUrl).crossFade().into(mIconImageView);
                    }
                    mNicknameTextView.setText(userName);

                    String genderText = (gender.equals("male")) ? "남" : "여";
                    int koreanAge = year - (Integer.parseInt(birth.substring(0, 4))) + 1;
                    String ageText = (koreanAge >= 0) ? koreanAge + "" : 0 + "";
                    String genderAndAgeText = "(" + genderText + "," + ageText + ")";

                    mGenderAndAgeTextView.setText(genderAndAgeText);
                    mCommentTextView.setText(((comment.equals("") || comment == null || comment.equals("None"))) ? "자기소개가 없습니다" : comment);

                    mPermitButton.setVisibility(Button.VISIBLE);
                    if (status.equals("complete")) {
                        mPermitButton.setEnabled(false);
                        mPermitButton.setText("승인됨");
                    } else {
                        mPermitButton.setEnabled(true);
                        mPermitButton.setText("승인");
                    }


                    mPermitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("event_user_id", eventUserId);
                            params.put("session_token", CommonData.LoginUserData.loginToken);
                            NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_APPLY, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                            NetManager netManager = new NetManager(netData, mContext);
                            netManager.setCallback(mNetManagerEventUserApplyListCallback);
                            netManager.execute((Void) null);
                        }
                    });

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private NetManager.Callbacks mNetManagerEventUserApplyListCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Toast.makeText(mContext, "승인되었습니다.", Toast.LENGTH_SHORT).show();
                    mPermitButton.setEnabled(false);
                    mPermitButton.setText("승인됨");
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
