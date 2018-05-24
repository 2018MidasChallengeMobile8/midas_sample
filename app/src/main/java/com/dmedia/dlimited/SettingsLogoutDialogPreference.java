package com.dmedia.dlimited;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import static android.content.Context.MODE_PRIVATE;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_ID;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_PASSWORD;

public class SettingsLogoutDialogPreference extends DialogPreference {

    Context mContext;

    public SettingsLogoutDialogPreference(Context context, AttributeSet attrs) {
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

        } else if (which == DialogInterface.BUTTON_NEGATIVE) { // logout
            //로그아웃시 자동로그인 삭제
            SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREFERENCE_LOGIN_ID, "");
            editor.putString(PREFERENCE_LOGIN_PASSWORD, "");
            editor.commit();

            Intent intent = new Intent(mContext, AccountLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }
    }
}
