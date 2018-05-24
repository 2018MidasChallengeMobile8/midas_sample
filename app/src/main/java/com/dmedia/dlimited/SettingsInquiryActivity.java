package com.dmedia.dlimited;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-20.
 */

public class SettingsInquiryActivity extends AppCompatActivity {
    Context mContext;
    private AppCompatSpinner mTypeSpinner;
    private EditText mTextEditText;
    private EditText mEmailEditText;
    private Button mInquiryButton;

    private String type = "etc";
    private String text = "";
    private String email = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_inquiry);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("문의하기");

        mContext = this;

        mTypeSpinner = (AppCompatSpinner) findViewById(R.id.sp_type);
        mTextEditText = (EditText) findViewById(R.id.edt_text);
        mEmailEditText = (EditText) findViewById(R.id.edt_email);
        mInquiryButton = (Button) findViewById(R.id.btn_inquiry);

        mInquiryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = mTextEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                switch (mTypeSpinner.getSelectedItemPosition()) {
                    case 0:
                        type = "etc";//일반 문의
                        break;
                    case 1:
                        type = "account";//계정 문의
                        break;
                    case 2:
                        type = "auth";//가입/탈퇴 문의
                        break;
                    case 3:
                        type = "service";//서비스 개선사항
                        break;
                }

                if (text.length() == 0) {
                    Toast.makeText(mContext, "문의 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    if (email.contains("@")) {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userid", CommonData.LoginUserData.userId);
                        params.put("type", type);
                        params.put("text", text);
                        params.put("email", email);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.USER_QNA, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserQnaCallback);
                        netManager.execute((Void) null);
                    } else {
                        Toast.makeText(mContext, "잘못된 이메일 형식입니다", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private NetManager.Callbacks mNetManagerUserQnaCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    Toast.makeText(mContext, "문의되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
