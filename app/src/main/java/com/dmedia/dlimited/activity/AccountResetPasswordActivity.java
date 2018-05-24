package com.dmedia.dlimited.activity;

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

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-20.
 */

public class AccountResetPasswordActivity extends AppCompatActivity {
    Context mContext;
    private EditText mNewPasswordEditText;
    private EditText mNewPasswordConfirmEditText;
    private Button mFinishButton;

    private String phone = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_reset_password);

        mContext = this;

        String tmp = getIntent().getStringExtra("userid");
        if (tmp != null && !tmp.equals("")) {
            phone = tmp;
        }

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("비밀번호 재설정");

        mNewPasswordEditText = (EditText) findViewById(R.id.edt_new_password);
        mNewPasswordConfirmEditText = (EditText) findViewById(R.id.edt_new_password_confirm);
        mFinishButton = (Button) findViewById(R.id.btn_finish);

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mNewPasswordEditText.getText().toString();
                String passwordConfirm = mNewPasswordConfirmEditText.getText().toString();
                if (Utils.isPasswordValid(password)) {
                    if (password.equals(passwordConfirm)) {
                        //형식 일치
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userid", phone);
                        params.put("new_password", mNewPasswordEditText.getText().toString());
                        NetData netData = new NetData(NetData.ProtocolType.USER_FIND_PASSWORD, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerUserFindPasswordCallback);
                        netManager.execute((Void) null);

                    } else {
                        Toast.makeText(AccountResetPasswordActivity.this, "비밀번호가 서로 다릅니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountResetPasswordActivity.this, getString(R.string.signup1_text_password_setting), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private NetManager.Callbacks mNetManagerUserFindPasswordCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int result = jsonObject.getInt("result");
                if (result == 1) {
                    Toast.makeText(mContext, "비밀번호가 변경되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences(AccountLoginActivity.PREFERENCE_LOGIN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(AccountLoginActivity.PREFERENCE_LOGIN_ID, CommonData.LoginUserData.userId);
                    editor.putString(AccountLoginActivity.PREFERENCE_LOGIN_PASSWORD, mNewPasswordEditText.getText().toString());
                    //editor.putString(PREFERENCE_LOGIN_TOKEN, CommonData.LoginUserData.loginToken);
                    editor.commit();

                    //접근경로가 2개인 액티비티이기 때문에 플로우가 꼬임. -> new task 로 처리함.
                    Intent intent = new Intent(AccountResetPasswordActivity.this, AccountLoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("phone", phone);
                    intent.putExtra("password", mNewPasswordEditText.getText().toString());
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
