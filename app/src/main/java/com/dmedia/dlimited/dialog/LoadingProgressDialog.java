package com.dmedia.dlimited.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

import com.dmedia.dlimited.R;

/**
 * Created by jeonghoy on 2016. 9. 7..
 */

public class LoadingProgressDialog extends Dialog {
    private static Dialog mDialog;

    private LoadingProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
    }
    public static void showProgress(Context context, String message) {
        if (mDialog == null) {
            mDialog = new ProgressDialog(context);
        }
        ((ProgressDialog)mDialog).setMessage(message);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void showProgress(Context context) {
        if (mDialog == null) {
            mDialog = new LoadingProgressDialog(context);
        }
        if (mDialog.getWindow() != null)
            mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
