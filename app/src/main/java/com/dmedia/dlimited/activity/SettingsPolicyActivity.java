package com.dmedia.dlimited.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.dmedia.dlimited.R;

/**
 * Created by xema0 on 2016-10-20.
 */
//SERVER 사용 대신 클라이언트에 박기로 결정
// TODO: 2016-11-13 웹뷰 대신 새창을 이용해서 띄운다
public class SettingsPolicyActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout mServicePolicyLayout;
    private LinearLayout mPrivatePolicyLayout;
    private LinearLayout mOperationPolicyLayout;
    private LinearLayout mOpenSourcePolicyLayout;

    //private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_policy);


        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("약관 및 정책");

        //webView = (WebView) findViewById(R.id.wb_policy);
        //webView.getSettings().setDomStorageEnabled(true);
        //warning : cr_BindingManager: Cannot call determinedVisibility() - never saw a connection for the pid: 29673
        /*
        WebSettings webSetting = webView.getSettings();

        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setDomStorageEnabled(true);


        //새창으로 실행
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
            {
                // return true or false after performing the URL request
                WebView newWebView = new WebView(SettingsPolicyActivity.this);
                view.addView(newWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse(url));
                        startActivity(browserIntent);
                        return true;
                    }
                });
                return true;
            }
        });
        */

        mServicePolicyLayout = (LinearLayout) findViewById(R.id.ll_policy_terms);
        mPrivatePolicyLayout = (LinearLayout) findViewById(R.id.ll_policy_privacy);
        mOperationPolicyLayout = (LinearLayout) findViewById(R.id.ll_policy_manage);
        mOpenSourcePolicyLayout = (LinearLayout) findViewById(R.id.ll_policy_open_source);
        mServicePolicyLayout.setOnClickListener(this);
        mPrivatePolicyLayout.setOnClickListener(this);
        mOperationPolicyLayout.setOnClickListener(this);
        mOpenSourcePolicyLayout.setOnClickListener(this);


    }

    /*
    @Override
    public void onClick(View v) {
        String title = "";
        switch (v.getId()) {
            case R.id.ll_policy_terms:
                title = getString(R.string.setting_policy_terms);
                break;
            case R.id.ll_policy_privacy:
                title = getString(R.string.setting_policy_privacy);
                break;
            case R.id.ll_policy_manage:
                title = getString(R.string.setting_policy_manage);
                break;
            case R.id.ll_policy_open_source:
                title = getString(R.string.setting_policy_open_source);
                break;
            default:
                break;
        }
        Intent intent = new Intent(SettingsPolicyActivity.this, SettingsPolicyContentsActivity.class);
        intent.putExtra("title", title);
        startActivity(intent);
    }
    */

    @Override
    public void onClick(View v) {
        String url = "";
        switch (v.getId()) {
            case R.id.ll_policy_terms:
                url = getString(R.string.policy_terms);
                break;
            case R.id.ll_policy_privacy:
                url = getString(R.string.policy_privacy);
                break;
            case R.id.ll_policy_manage:
                url = getString(R.string.policy_manage);
                break;
            case R.id.ll_policy_open_source:
                url = getString(R.string.policy_open_source);
                break;
            default:
                break;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
