package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-07.
 */

public class EventApplyActivity extends AppCompatActivity {
    Context mContext;
    private TextView mInstaTextView;
    private TextView mPhoneTextView;
    private EditText mApplyCommentEditText;
    private Button mApplyButton;

    private int eventId;
    private String eventTitle = "";
    private String instaStr;
    private String phoneStr;

    //private InstagramApp mApp;

    /*
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
    */


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_apply);

        mContext = this;

        eventId = getIntent().getIntExtra("event_id", 0);
        eventTitle = getIntent().getStringExtra("event_title");

        //mApp = new InstagramApp(mContext, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        //mApp.setListener(instagramListener);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("참가 신청 작성");

        mInstaTextView = (TextView) findViewById(R.id.tv_insta);
        mPhoneTextView = (TextView) findViewById(R.id.tv_phone);
        mApplyCommentEditText = (EditText) findViewById(R.id.edt_apply_comment);
        mApplyButton = (Button) findViewById(R.id.btn_apply);

        instaStr = CommonData.LoginUserData.instagramId;

        /*
        //인스타 numeric id로 인스타 username 얻기
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("access_token", CommonData.LoginUserData.instaToken);
        //params.put("access_token",mApp.getAccessToken());
        client.get(mContext, "https://api.instagram.com/v1/users/" + instaStr + "/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    mInstaTextView.setText("@" + data.getString("username"));
                } catch (JSONException e) {
                    Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if (errorResponse.getJSONObject("meta").getString("error_type").equals("OAuthAccessTokenException")) {
                        Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        mApp.authorize();
                    } else {
                        Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                        mApp.authorize();
                    }
                } catch (JSONException e) {
                    Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                };
            }
        });
        */

        phoneStr = CommonData.LoginUserData.userId;
        mInstaTextView.setText("@" + CommonData.LoginUserData.instagramName);

        if (phoneStr.contains("010") && phoneStr.length() == 11) {
            mPhoneTextView.setText(phoneStr.substring(0, 3) + "-" + phoneStr.substring(3, 7) + "-" + phoneStr.substring(7, 11));
        } else {
            mPhoneTextView.setText(phoneStr);
        }

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("참가 신청");
                builder.setMessage(eventTitle + " 이벤트에 참가를 신청하시겠습니까?");
                builder.setPositiveButton("신청", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String applyComment = mApplyCommentEditText.getText().toString();
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("event_id", eventId);
                        params.put("userid", phoneStr);
                        params.put("comment", applyComment);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_REQUEST, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerEventUserApplyCallback);
                        netManager.execute((Void) null);
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();
            }
        });

    }

    private NetManager.Callbacks mNetManagerEventUserApplyCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                if (jsonObject.getInt("result") == 1) {
                    Toast.makeText(mContext, "신청 되었습니다", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(mContext, "서버와의 통신중 에러가 발생했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}
