package com.dmedia.dlimited;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dmedia.dlimited.IntroActivity;
import com.dmedia.dlimited.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by xema0 on 2016-11-18.
 */

public class FcmFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map<String, String> data = message.getData();

        Log.d("remote",data.toString());

        int notifyId = Integer.valueOf(getString(data, "notify_id", "0"));
        String title = getString(data, "title", "");
        String msg = getString(data, "message", "");
        String link = getString(data, "url", "");

        Bundle dataBundle = new Bundle();
        for(String key: data.keySet()){
            dataBundle.putString(key, data.get(key));
        }
        dataBundle.putString("link",link);

        notifyId = 0;
        sendNotification(notifyId, title, msg, dataBundle);

    }
    private String getString(Map<String, String> data, String str, String def){
        String rtnStr = def;
        try {
            if( data.get(str) != null ){
                rtnStr = data.get(str);
            }
        }catch (Exception e) {

        }

        return rtnStr;
    }

    private void sendNotification(int notifyId, String title, String message, Bundle data) {
        String link = data.getString("url", "");
        Intent intent = null;

        if(!link.isEmpty()){
            intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(link);
            intent.setData(uri);
            intent.putExtras(data);
        }else{
            intent = new Intent(this, IntroActivity.class);
            intent.putExtras(data);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
    }


    /*
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        if (remoteMessage != null) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        }

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String url = "";
        if (remoteMessage.getData().containsKey("url")) {
            url = remoteMessage.getData().get("url").toString();
        }

        Log.d(TAG, "url: " + url);


        //Calling method to generate notification
        sendNotification(title, body, url);
    }

    private void sendNotification(String title, String body, String url) {


        Intent intent;

        Log.d("firebase url",url);

        if (url.equals("")) {
            intent = new Intent(this, IntroActivity.class);
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setTicker(body)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[] { 1000, 1000, 1000 })
                .setContentIntent(pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(TAG, 1, notificationBuilder.build());

//        Intent intent = new Intent(this, NotificationPopupActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
//        this.startActivity(intent);



    }

    */
}
