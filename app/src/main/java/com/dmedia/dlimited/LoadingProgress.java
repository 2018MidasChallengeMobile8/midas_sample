package com.dmedia.dlimited;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by jeonghoy on 2016. 9. 7..
 */

public class LoadingProgress {

    public static Context mContext;
    public static Dialog mDialog;

    public static void showProgress(Context context) { // splash progress
        if (mDialog == null) {
            mDialog = new LoadingProgressDialog(context);
        }
        mDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void showProgress(Context context, String message) {
        if (mDialog == null) {
            mDialog = new ProgressDialog(context);
        }
        ((ProgressDialog)mDialog).setMessage(message);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public static class LoadingProgressDialog extends Dialog {
        public LoadingProgressDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE); // 지저분한(?) 다이얼 로그 제목을 날림
            setContentView(R.layout.dialog_loading); // 다이얼로그에 박을 레이아웃

        }
    }
}
