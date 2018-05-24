package com.dmedia.dlimited;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.LoadingProgress.mContext;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING_EVENT_ALARM;
import static com.dmedia.dlimited.SettingsNotificationActivity.PREFERENCE_SETTING_INVITE_ALARM;

/**
 * Created by xema0 on 2016-11-18.
 */

public class FcmFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FcmFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(this, refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(Context context, String token) {
        // Add custom implementation, as needed.

        Log.d(TAG, "FCM token: " + token);

        //HashMap<String, Object> params = new HashMap<>();
        //params.put("gcm_token", token);
        //NetData netData = new NetData(NetData.ProtocolType.ACCOUNT, NetData.MethodType.PUT, NetData.ProgressType.NONE, params);
        //NetManager netManager = new NetManager(netData, context);
        //netManager.execute((Void) null);

        HashMap<String, Object> params = new HashMap<>();
        NetData netData = new NetData(NetData.ProtocolType.USER_DEVICE_PUT, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
        if (prefs != null) {
            String phone = prefs.getString(PREFERENCE_LOGIN_ID, "");
            if (phone.contentEquals("") || prefs == null) {
                params.put("userid", "nophone");
            } else {
                params.put("userid", phone);
            }
            params.put("udid", Utils.getUDID(context));
            params.put("deviceToken", token);
            params.put("platform", "android");

            SharedPreferences prefsNoti = context.getSharedPreferences(PREFERENCE_SETTING, MODE_PRIVATE);
            String eventCheck = prefsNoti.getString(PREFERENCE_SETTING_EVENT_ALARM, "true");
            String inviteCheck = prefsNoti.getString(PREFERENCE_SETTING_INVITE_ALARM, "true");
            if (String.valueOf(eventCheck).equals(false)) {
                params.put("event_flag", 0);
            }else {
                params.put("event_flag", 1);
            }
            if (String.valueOf(inviteCheck).equals(false)) {
                params.put("invite_flag", 1);
            }else {
                params.put("invite_flag", 1);
            }
            NetManager netManager = new NetManager(netData, context);
            //netManager.setCallback(mNetManagerUserDevicePutCallback);
            netManager.execute((Void) null);
        }
    }

    /*
    private static NetManager.Callbacks mNetManagerUserDevicePutCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Log.d("callback : ",jsonObject.toString());
                } else if (resultCode == 0) {
                    Log.d("callback : ",jsonObject.toString());
                    //Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    */
}
