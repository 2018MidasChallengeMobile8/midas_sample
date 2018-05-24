package com.dmedia.dlimited;

/**
 * Created by jeonghoy on 2016. 6. 21..
 */

import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.moveTaskToBack(true);
            ActivityCompat.finishAffinity(activity);
            activity.finish();
            toast.cancel();
        }
    }


    private void showGuide() {
        toast = Toast.makeText(activity, activity.getString(R.string.toast_backbutton), Toast.LENGTH_SHORT);
        toast.show();
    }
}