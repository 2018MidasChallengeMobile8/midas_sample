package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;

/**
 * Created by xema0 on 2016-11-17.
 */

public class SettingsDcodeActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mDCodeEditText;
    private Button mFinishButton;

    Context mContext;

    private String phone = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_dcode);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("D-Code입력");

        mContext = this;

        phone = CommonData.LoginUserData.userId;

        mDCodeEditText = (EditText) findViewById(R.id.edt_dcode);
        mFinishButton = (Button) findViewById(R.id.btn_finish);

        mFinishButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_finish) {
            HashMap<String, Object> params = new HashMap<>();
            String code = mDCodeEditText.getText().toString();
            params.put("userid", phone);
            params.put("code", code);
            params.put("session_token", CommonData.LoginUserData.loginToken);
            NetData netData = new NetData(NetData.ProtocolType.USER_AUTH_DCODE, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerDCodeAuthCallback);
            netManager.execute((Void) null);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        super.finish();
    }
}
