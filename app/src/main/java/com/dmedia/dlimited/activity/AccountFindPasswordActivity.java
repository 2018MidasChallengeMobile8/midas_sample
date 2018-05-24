package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
// TODO: 2016-10-04 공유하기 - '이벤트의 링크가 복사되었습니다' 물어보기 

/**
 * Created by min on 2016-09-18.
 */
public class AccountFindPasswordActivity extends AppCompatActivity {
    Context mContext;
    private EditText mPhoneEditText;
    private EditText mSmsEditText;
    private Button mSmsSendButton;
    private Button mNextButton;

    private boolean smsSendCheck = false;

    private String authNumber = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_find_password);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("비밀번호 찾기");

        mPhoneEditText = (EditText) findViewById(R.id.edt_phone);
        mSmsEditText = (EditText) findViewById(R.id.edt_sms);
        mSmsSendButton = (Button) findViewById(R.id.btn_send);
        mNextButton = (Button) findViewById(R.id.btn_next);

        mSmsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mPhoneEditText.getText().toString();
                if (Utils.isDigit(phone)) {
                    if (!smsSendCheck) {
                        //문자전송을 처음으로 눌렀을때
                        smsSendCheck = true;
                        mSmsSendButton.setText("재전송");
                        mSmsSendButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_round_rectangle_fill_white, null));
                        mSmsSendButton.setTextColor(Color.BLACK);
                    }

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("phone", phone);
                    NetData netData = new NetData(NetData.ProtocolType.USER_AUTH, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerUserAuthCallback);
                    netManager.execute((Void) null);
                } else {
                    Toast.makeText(mContext, "잘못된 휴대폰 번호입니다.\n-를 빼고 적어주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String authStr = mSmsEditText.getText().toString();
                if (smsSendCheck) {
                    if (authStr.equals(authNumber) && !authNumber.equals("")) {
                        Intent intent = new Intent(AccountFindPasswordActivity.this, AccountResetPasswordActivity.class);
                        intent.putExtra("userid",mPhoneEditText.getText().toString());
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "인증번호가 틀립니다.", Toast.LENGTH_SHORT).show();
                        mSmsEditText.requestFocus();
                    }
                } else {
                    Toast.makeText(mContext, "휴대폰 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private NetManager.Callbacks mNetManagerUserAuthCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int result = jsonObject.getInt("result");
                if(result == 1 ){
                    Toast.makeText(mContext, "인증번호가 전송되었습니다.\n잠시 기다려주세요", Toast.LENGTH_SHORT).show();
                    authNumber = jsonObject.getString("auth_number");
                }
                else{
                    Toast.makeText(mContext, "인증번호 전송을 실패했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
