package com.dmedia.dlimited;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by min on 2016-09-18.
 */
// TODO: 2016-09-23 error 세팅
// TODO: 2016-10-26 비밀번호 검증 어떻게 할건지. 형식 정하기
public class AccountSignupActivity1 extends AppCompatActivity implements View.OnClickListener {
    public static Activity mSignupActivty1;
    private EditText mPhoneEditText;
    private EditText mAuthSmsEditText;
    private EditText mPassWordEditText;
    private EditText mPassWordConfirmEditText;
    private Button mSmsSendButton;
    private Button mNextButton;

    Context mContext;

    private String authNumber = "";
    private boolean smsPermissionCheck = false;
    private boolean smsSendCheck = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup_1);

        mContext = this;
        mSignupActivty1 = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("회원 가입");

        mPhoneEditText = (EditText) findViewById(R.id.edt_phone);
        mAuthSmsEditText = (EditText) findViewById(R.id.edt_auth_sms);
        mPassWordEditText = (EditText) findViewById(R.id.edt_password);
        mPassWordConfirmEditText = (EditText) findViewById(R.id.edt_password_confirm);
        mSmsSendButton = (Button) findViewById(R.id.btn_send);
        mNextButton = (Button) findViewById(R.id.btn_next);

        mSmsSendButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mNextButton.setEnabled(false);


        // TODO: 2016-10-26 모든 폼에 넥스트 버튼 활성화 넣기는 비효율적->한번 활성화시키고 버튼 누르면 다시 검사하는식으로
        mPassWordConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (Utils.isPasswordValid(s.toString())) {
                    mNextButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_round_rectangle_fill_black, null));
                    mNextButton.setTextColor(Color.WHITE);
                    mNextButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String phone = mPhoneEditText.getText().toString();
                if (Utils.isDigit(phone)) {
                    permissionCheck();

                    if (!smsSendCheck) {
                        //문자전송을 처음으로 눌렀을때
                        smsSendCheck = true;
                        mSmsSendButton.setText("재전송");
                        mSmsSendButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_round_rectangle_fill_white, null));
                        mSmsSendButton.setTextColor(Color.BLACK);
                    }

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("phone", phone);
                    NetData netData = new NetData(NetData.ProtocolType.USER_AUTH, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerUserAuthCallback);
                    netManager.execute((Void) null);
                } else {
                    Toast.makeText(mContext, "잘못된 휴대폰 번호입니다.\n-를 빼고 적어주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_next:
                if (formValidCheck()) {
                    Intent intent = new Intent(AccountSignupActivity1.this, AccountSignupActivity2.class);
                    intent.putExtra("phone", mPhoneEditText.getText().toString());
                    intent.putExtra("password", mPassWordEditText.getText().toString());
                    startActivity(intent);
                }
                break;
        }
    }

    // TODO: 2016-10-31 response code가 1이 아니면 에러 출력
    private NetManager.Callbacks mNetManagerUserAuthCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int result = jsonObject.getInt("result");
                if (result == 1) {
                    Toast.makeText(mContext, "인증번호가 전송되었습니다.\n잠시 기다려주세요", Toast.LENGTH_SHORT).show();
                    authNumber = jsonObject.getString("auth_number");
                } else {
                    Toast.makeText(mContext, "인증번호 전송을 실패했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // TODO: 2016-10-26 권한 체크용도 - 6.0이상 버전에서도 테스팅해보기
    private void permissionCheck() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //권한 있음
            smsPermissionCheck = true;
        } else {
            //권한 없음
            String[] permissions = {Manifest.permission.RECEIVE_SMS};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    smsPermissionCheck = true;
                } else {
                    smsPermissionCheck = false;
                }
            }
        }
    }

    //RedirectActivity 에서 넘어온 데이터를 에딧텍스트에 자동 입력.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String tmp = intent.getStringExtra("auth_number");
        if ((tmp != null) && (tmp != "")) {
            mAuthSmsEditText.setText(tmp);
            Toast.makeText(mContext, "인증되었습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean formValidCheck() {
        String authStr = mAuthSmsEditText.getText().toString();
        String pwdStr = mPassWordEditText.getText().toString();
        String pwdConfirmStr = mPassWordConfirmEditText.getText().toString();

        if (smsSendCheck) {
            if (authStr.equals(authNumber) && !authNumber.equals("")) {
                if (Utils.isPasswordValid(pwdStr)) {
                    if (pwdStr.equals(pwdConfirmStr)) {
                        //모든 형식 일치
                        return true;
                    } else {
                        Toast.makeText(mContext, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show();
                        mPassWordConfirmEditText.requestFocus();
                        return false;
                    }
                } else {
                    Toast.makeText(mContext, "비밀번호는 8~16자의 영문,숫자를 조합하여 설정해주세요", Toast.LENGTH_SHORT).show();
                    mPassWordEditText.requestFocus();
                    return false;
                }
            } else {
                Toast.makeText(mContext, "인증번호가 틀립니다.", Toast.LENGTH_SHORT).show();
                mAuthSmsEditText.requestFocus();
                return false;
            }
        } else {
            Toast.makeText(mContext, "휴대폰 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
