package com.dmedia.dlimited.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.dmedia.dlimited.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xema0 on 2016-12-02.
 */

public class SettingsPolicyContentsActivity extends AppCompatActivity {
    Context mContext;

    private TextView mPolicyContentTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_policy_contents);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);

        String title = getIntent().getStringExtra("title");
        a.setTitle(title);

        mContext = this;

        mPolicyContentTextView = (TextView) findViewById(R.id.tv_policy_content);

        mPolicyContentTextView.setText(readTxt(title));
    }

    private String readTxt(String what) {
        String data = null;
        InputStream inputStream = null;
        if (what.equals(getString(R.string.setting_policy_terms))) {
            inputStream = getResources().openRawResource(R.raw.terms);
        } else if (what.equals(getString(R.string.setting_policy_privacy))) {
            inputStream = getResources().openRawResource(R.raw.privacy);
        } else if (what.equals(getString(R.string.setting_policy_manage))) {
            // TODO: 2016-12-02 바뀌면 수정
            inputStream = getResources().openRawResource(R.raw.manage);
        } else if (what.equals(getString(R.string.setting_policy_open_source))) {
            //
            inputStream = getResources().openRawResource(R.raw.open_source);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            data = new String(byteArrayOutputStream.toByteArray(), "MS949");
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
