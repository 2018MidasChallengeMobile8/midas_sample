package com.dmedia.dlimited.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.dmedia.dlimited.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
// TODO: 2016-09-19 ScrollView 는 내부가 wrap_conent외에는 되지 않음 -> 가로모드일때 뷰를 어떻게 조정할것인지...
// TODO: 2016-09-19 게다가 스크롤뷰가 이미 두개인데 그 위에다 스크롤뷰를 하나 더 띄워도 되나?? 이상하지 않을려나

// TODO: 2016-10-12 뒤로가기버튼 asset 적용?
// TODO: 2016-10-12 체크박스 테마 설정(색상)
// TODO: 2016-10-12 넥스트 버튼에 >이미지 달기
// TODO: 2016-10-13 seterror 테마(색상)수정

/**
 * Created by min on 2016-09-18.
 */
public class AccountSignupAgreementActivity extends AppCompatActivity implements View.OnClickListener {
    public static Activity mSignupAgreementActivity;
    private static final String TAG = "AccountSignupAgreementActivity";
    private CheckBox mAgreeCheckBox1;
    private CheckBox mAgreeCheckBox2;
    private TextView mCheckBoxTextView1;
    private TextView mCheckBoxTextView2;
    private TextView mPolicyTextView1;
    private TextView mPolicyTextView2;
    private Button mNextButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup_agreement);

        mSignupAgreementActivity = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle(getString(R.string.agreement));

        mAgreeCheckBox1 = (CheckBox) findViewById(R.id.cb_agree_1);
        mAgreeCheckBox2 = (CheckBox) findViewById(R.id.cb_agree_2);
        mCheckBoxTextView1 = (TextView) findViewById(R.id.tv_agree_1);
        mCheckBoxTextView2 = (TextView) findViewById(R.id.tv_agree_2);
        mPolicyTextView1 = (TextView) findViewById(R.id.tv_policy1);
        mPolicyTextView2 = (TextView) findViewById(R.id.tv_policy2);
        mNextButton = (Button) findViewById(R.id.btn_next);

        mNextButton.setOnClickListener(this);
        mPolicyTextView1.setText(readTxt(getString(R.string.setting_policy_terms)));
        mPolicyTextView2.setText(readTxt(getString(R.string.setting_policy_privacy)));
    }

    private String readTxt(String what) {
        String data = null;
        InputStream inputStream = null;
        if (what.equals(getString(R.string.setting_policy_terms))) {
            inputStream = getResources().openRawResource(R.raw.terms);
        } else if (what.equals(getString(R.string.setting_policy_privacy))) {
            inputStream = getResources().openRawResource(R.raw.privacy);
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

    public void check() {
        mCheckBoxTextView1.setError(null);
        mCheckBoxTextView2.setError(null);

        boolean cancel = false;
        View focusView = null;

        //CheckBox에 포커스 먹일경우 뷰가 이상해져서 텍스트뷰에 포커스를 먹인다.
        if (!mAgreeCheckBox1.isChecked()) {
            mCheckBoxTextView1.setError("약관에 동의해주세요");
            focusView = mCheckBoxTextView1;
            cancel = true;
        } else if (!mAgreeCheckBox2.isChecked()) {
            mCheckBoxTextView2.setError("약관에 동의해주세요");
            focusView = mCheckBoxTextView2;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Intent intent = new Intent(AccountSignupAgreementActivity.this, AccountSignupActivity1.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        check();
    }
}
