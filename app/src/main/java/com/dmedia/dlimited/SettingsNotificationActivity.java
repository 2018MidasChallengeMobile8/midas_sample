package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-20.
 */
// TODO: 2016-10-20 스위치 디자인 애셋 없음
public class SettingsNotificationActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    Context mContext;
    private SwitchCompat mEventAlarmSwitch;
    private SwitchCompat mInviteAlarmSwitch;

    public static final String PREFERENCE_SETTING = "preference_setting";
    public static final String PREFERENCE_SETTING_EVENT_ALARM = "preference_setting_event_alarm";
    public static final String PREFERENCE_SETTING_INVITE_ALARM = "preference_setting_invite_alarm";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notification);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("알림 설정");

        mContext = this;

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
        String eventCheck = prefs.getString(PREFERENCE_SETTING_EVENT_ALARM, "true");
        String inviteCheck = prefs.getString(PREFERENCE_SETTING_INVITE_ALARM, "true");

        mEventAlarmSwitch = (SwitchCompat) findViewById(R.id.sc_event_alarm);
        mInviteAlarmSwitch = (SwitchCompat) findViewById(R.id.sc_invite_alarm);

        if (eventCheck.equals("false")) {
            mEventAlarmSwitch.setChecked(false);
        } else {
            mEventAlarmSwitch.setChecked(true);
        }

        if (inviteCheck.equals("false")) {
            mInviteAlarmSwitch.setChecked(false);
        } else {
            mInviteAlarmSwitch.setChecked(true);
        }

        mEventAlarmSwitch.setOnCheckedChangeListener(this);
        mInviteAlarmSwitch.setOnCheckedChangeListener(this);
    }


    /*
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_setting_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_confirm) {
            //프리퍼런스에도 저장, 서버에도 저장
            SharedPreferences prefs = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (!mEventAlarmSwitch.isChecked()) {
                editor.putString(PREFERENCE_SETTING_EVENT_ALARM, "false");
            } else {
                editor.putString(PREFERENCE_SETTING_EVENT_ALARM, "true");
            }
            if (!mInviteAlarmSwitch.isChecked()) {
                editor.putString(PREFERENCE_SETTING_INVITE_ALARM, "false");
            } else {
                editor.putString(PREFERENCE_SETTING_INVITE_ALARM, "true");
            }
            editor.commit();

            HashMap<String, Object> params = new HashMap<>();
            params.put("userid", CommonData.LoginUserData.userId);
            params.put("device", CommonData.LoginUserData.UDID);
            int eventCheck = 1;
            int inviteCheck = 1;
            if (!mEventAlarmSwitch.isChecked()) {
                eventCheck = 0;
            } else {
                eventCheck = 1;
            }
            if (!mInviteAlarmSwitch.isChecked()) {
                inviteCheck = 0;
            } else {
                inviteCheck = 1;
            }
            params.put("event_flag", eventCheck);//1-on,0-off
            params.put("invite_flag", inviteCheck);//1-on,0-off
            params.put("session_token", CommonData.LoginUserData.loginToken);

            NetData netData = new NetData(NetData.ProtocolType.USER_ALARM, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerUserAlarmCallback);
            netManager.execute((Void) null);
        }
        return super.onOptionsItemSelected(item);
    }
    */

    private NetManager.Callbacks mNetManagerUserAlarmCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Toast.makeText(SettingsNotificationActivity.this, "수정되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //프리퍼런스에도 저장, 서버에도 저장
        SharedPreferences prefs = getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (!mEventAlarmSwitch.isChecked()) {
            editor.putString(PREFERENCE_SETTING_EVENT_ALARM, "false");
        } else {
            editor.putString(PREFERENCE_SETTING_EVENT_ALARM, "true");
        }
        if (!mInviteAlarmSwitch.isChecked()) {
            editor.putString(PREFERENCE_SETTING_INVITE_ALARM, "false");
        } else {
            editor.putString(PREFERENCE_SETTING_INVITE_ALARM, "true");
        }
        editor.commit();

        HashMap<String, Object> params = new HashMap<>();
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("device", CommonData.LoginUserData.UDID);
        int eventCheck = 1;
        int inviteCheck = 1;
        if (!mEventAlarmSwitch.isChecked()) {
            eventCheck = 0;
        } else {
            eventCheck = 1;
        }
        if (!mInviteAlarmSwitch.isChecked()) {
            inviteCheck = 0;
        } else {
            inviteCheck = 1;
        }
        params.put("event_flag", eventCheck);//1-on,0-off
        params.put("invite_flag", inviteCheck);//1-on,0-off
        params.put("session_token", CommonData.LoginUserData.loginToken);

        NetData netData = new NetData(NetData.ProtocolType.USER_ALARM, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerUserAlarmCallback);
        netManager.execute((Void) null);
    }
}
