package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_PASSWORD;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_TOKEN;

/**
 * Created by xema0 on 2016-10-20.
 */

public class AccountChangePasswordActivity extends AppCompatActivity {
    Context mContext;
    private EditText mCurrentPasswordEditText;
    private EditText mNewPasswordEditText;
    private EditText mNewPasswordConfirmEditText;
    private Button mFinishButton;
    private Button mFindPasswordButton;

    private String userId = "";
    private String cntPassword = "";
    private String newPassword = "";
    private String newConfirmPassword = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_change_password);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("비밀번호 변경");

        userId = CommonData.LoginUserData.userId;

        mCurrentPasswordEditText = (EditText) findViewById(R.id.edt_current_password);
        mNewPasswordEditText = (EditText) findViewById(R.id.edt_new_password);
        mNewPasswordConfirmEditText = (EditText) findViewById(R.id.edt_new_password_confirm);
        mFinishButton = (Button) findViewById(R.id.btn_finish);
        mFindPasswordButton = (Button) findViewById(R.id.btn_goto_find_password);

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cntPassword = mCurrentPasswordEditText.getText().toString();
                newPassword = mNewPasswordEditText.getText().toString();
                newConfirmPassword = mNewPasswordConfirmEditText.getText().toString();

                if (Utils.isPasswordValid(newPassword)) {
                    if (newPassword.equals(newConfirmPassword)) {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userid", userId);
                        params.put("current_password", cntPassword);
                        params.put("new_password", newPassword);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.USER_CHANGE_PASSWORD, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserChangePasswordCallback);
                        netManager.execute((Void) null);
                    } else {
                        Toast.makeText(AccountChangePasswordActivity.this, "비밀번호가 서로 다릅니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountChangePasswordActivity.this, getString(R.string.signup1_text_password_setting), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFindPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountChangePasswordActivity.this, AccountFindPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private NetManager.Callbacks mNetManagerUserChangePasswordCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int result = jsonObject.getInt("result");
                if (result == 1) {
                    CommonData.LoginUserData.loginToken = jsonObject.getString("new_session_token");

                    Toast.makeText(mContext, "비밀번호가 변경되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_LOGIN_ID, CommonData.LoginUserData.userId);
                    editor.putString(PREFERENCE_LOGIN_PASSWORD, newPassword);
                    //editor.putString(PREFERENCE_LOGIN_TOKEN, jsonObject.getString("new_session_token"));
                    editor.commit();

                    Intent intent = new Intent(AccountChangePasswordActivity.this, AccountLoginActivity.class);
                    //다시 로그인 화면으로
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                    intent.putExtra("phone", userId);
                    intent.putExtra("password", newPassword);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, "비밀번호 변경을 실패했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
