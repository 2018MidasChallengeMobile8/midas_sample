package com.dmedia.dlimited;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xema0 on 2016-10-25.
 */
// TODO: 2016-11-01 여유되면 AccountFindPasswordActivity에도 리시버 붙이기
// TODO: 2016-10-27 문자 보낸 번호 확정되었을때 보낸사람 번호로 검증하기
public class SmsReceiver extends BroadcastReceiver {
    static final String TAG = "SmsReceiver";
    static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    static final String logTag = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        /*

        if (intent.getAction().equals(ACTION)) {
            abortBroadcast();
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            //pdu 객체 널 체크
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            if (pdusObj == null) {
                return;
            }
            //message 처리
            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for (int i = 0; i < pdusObj.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                Log.e(logTag, "NEW SMS " + i + "th");
                Log.e(logTag, "DisplayOriginatingAddress : "
                        + smsMessages[i].getDisplayOriginatingAddress());
                Log.e(logTag, "DisplayMessageBody : "
                        + smsMessages[i].getDisplayMessageBody());
                Log.e(logTag, "EmailBody : "
                        + smsMessages[i].getEmailBody());
                Log.e(logTag, "EmailFrom : "
                        + smsMessages[i].getEmailFrom());
                Log.e(logTag, "OriginatingAddress : "
                        + smsMessages[i].getOriginatingAddress());
                Log.e(logTag, "MessageBody : "
                        + smsMessages[i].getMessageBody());
                Log.e(logTag, "ServiceCenterAddress : "
                        + smsMessages[i].getServiceCenterAddress());
                Log.e(logTag, "TimestampMillis : "
                        + smsMessages[i].getTimestampMillis());

                // TODO: 2016-10-26 sms 형식이 달라지면 오류나올수도 있으니 예외 핸들링하기
                String sms = smsMessages[i].getMessageBody();
                sms = sms.split("\\[")[1].split("\\]")[0];

                boolean activityCheck = false;
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);
                for (int i1 = 0; i1 < services.size(); i1++) {
                    if (services.get(i1).topActivity.toString().contains("AccountSignupActivity1")) {
                        activityCheck = true;
                    }
                }

                if (activityCheck) {
                    Intent mIntent = new Intent(context, RedirectActivity.class);
                    mIntent.putExtra("auth_number", sms);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mIntent);
                }
            }
        }
        */
    }
}
