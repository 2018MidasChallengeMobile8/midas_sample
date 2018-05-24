package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dmedia.dlimited.activity.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.activity.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.activity.AccountLoginActivity.PREFERENCE_LOGIN_PASSWORD;

/**
 * Created by xema0 on 2016-10-20.
 */

public class SettingsWithdrawActivity extends AppCompatActivity {
    Context mContext;
    private AppCompatCheckBox mAgreeCheckBox;
    private Button mConfirmButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_withdraw);

        mContext = this;

        mAgreeCheckBox = (AppCompatCheckBox) findViewById(R.id.cb_agree);
        mConfirmButton = (Button) findViewById(R.id.btn_confirm);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAgreeCheckBox.isChecked()) {
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("userid", CommonData.LoginUserData.userId);
                    params.put("session_token", CommonData.LoginUserData.loginToken);
                    NetData netData = new NetData(NetData.ProtocolType.USER_WITHDRAW, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerUserWithdrawCallback);
                    netManager.execute((Void) null);
                } else {
                    Toast.makeText(SettingsWithdrawActivity.this, "동의해야만 회원탈퇴가 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private NetManager.Callbacks mNetManagerUserWithdrawCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    Toast.makeText(mContext, "회원탈퇴에 성공했습니다.", Toast.LENGTH_SHORT).show();

                    //자동로그인 지우고 로그아웃
                    SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_LOGIN_ID, "");
                    editor.putString(PREFERENCE_LOGIN_PASSWORD, "");
                    editor.commit();

                    Intent intent = new Intent(mContext, AccountLoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "회원탈퇴에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
