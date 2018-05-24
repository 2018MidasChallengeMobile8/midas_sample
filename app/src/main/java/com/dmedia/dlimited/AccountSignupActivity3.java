package com.dmedia.dlimited;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_UDID;
import static com.dmedia.dlimited.AccountSignupActivity1.mSignupActivty1;
import static com.dmedia.dlimited.AccountSignupActivity2.mSignupActivty2;
import static com.dmedia.dlimited.AccountSignupAgreementActivity.mSignupAgreementActivity;

/**
 * Created by min on 2016-09-18.
 */
// TODO: 2016-09-23 error 세팅
//D-Code 입력화면
public class AccountSignupActivity3 extends AppCompatActivity implements View.OnClickListener {
    private EditText mDCodeEditText;
    private Button mFinishButton;
    private Button mSkipButton;

    Context mContext;

    private String phone = "";
    private String password = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup_3);

        mSignupAgreementActivity.finish();
        mSignupActivty1.finish();
        mSignupActivty2.finish();

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(false);//뒤로가기 방지 - 회원가입을 다시하지 않도록
        //a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("D-Code입력");

        mContext = this;

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
        String prefPhone = prefs.getString(PREFERENCE_LOGIN_ID, "");
        //String prefPassword = prefs.getString(PREFERENCE_LOGIN_PASSWORD, "");
        if (getIntent().getStringExtra("phone") != null && !getIntent().getStringExtra("phone").equals("")) {
            //signup activity 2에서 넘어온 경우
            phone = getIntent().getStringExtra("phone");
            password = getIntent().getStringExtra("password");
        } else {
            //예외상황
            Toast.makeText(mContext, "에러가 발생했습니다.\n로그인 화면으로 넘어갑니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountSignupActivity3.this, AccountLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        mDCodeEditText = (EditText) findViewById(R.id.edt_dcode);
        mFinishButton = (Button) findViewById(R.id.btn_finish);
        mSkipButton = (Button) findViewById(R.id.btn_skip);

        mFinishButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_finish) {
            HashMap<String, Object> params = new HashMap<>();
            String code = mDCodeEditText.getText().toString();
            params.put("userid", phone);
            params.put("code", code);
            params.put("session_token", CommonData.LoginUserData.loginToken);
            NetData netData = new NetData(NetData.ProtocolType.USER_AUTH_DCODE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerDCodeAuthCallback);
            netManager.execute((Void) null);
        } else if (v.getId() == R.id.btn_skip) {
            Toast.makeText(mContext, "D-Code는 마이페이지 - 설정에서 입력 가능합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AccountSignupActivity3.this, AccountLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("phone", phone);
            intent.putExtra("password", password);
            startActivity(intent);
            finish();
        }
    }

    private NetManager.Callbacks mNetManagerDCodeAuthCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    //성공
                    Toast.makeText(mContext, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccountSignupActivity3.this, AccountLoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("phone", phone);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "인증에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 2) {
                    Toast.makeText(mContext, "존재하지 않는 사용자입니다", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 3) {
                    Toast.makeText(mContext, "존재하지 않는 D-Code입니다.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 4) {
                    Toast.makeText(mContext, "이미 사용된 D-Code입니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}
