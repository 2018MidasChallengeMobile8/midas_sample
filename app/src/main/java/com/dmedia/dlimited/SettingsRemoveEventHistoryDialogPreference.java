package com.dmedia.dlimited;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SettingsRemoveEventHistoryDialogPreference extends DialogPreference {
    Context mContext;

    public SettingsRemoveEventHistoryDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        persistBoolean(positiveResult);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) { // cancel

        } else if (which == DialogInterface.BUTTON_NEGATIVE) { // delete
            // 기록 지우기
            HashMap<String, Object> params = new HashMap<>();
            params.put("userid", CommonData.LoginUserData.userId);
            params.put("session_token", CommonData.LoginUserData.loginToken);
            NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_REMOVE_ALL, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerEventUserRemoveAllCallback);
            netManager.execute((Void) null);
        }
    }

    private NetManager.Callbacks mNetManagerEventUserRemoveAllCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
