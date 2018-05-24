package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by xema0 on 2016-10-18.
 */

public class ProductReviewDetailActivity extends AppCompatActivity {
    Context mContext;

    private TextView mNicknameTextView;
    private TextView mPhoneTextView;
    private LinearLayout mInstaReviewLayout;
    private TextView mReviewTextView;
    private Button mCompensateButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review_detail);
        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("리뷰 상세보기");

        final ProductReviewListData data = getIntent().getParcelableExtra("data");

        mNicknameTextView = (TextView) findViewById(R.id.tv_nickname);
        mPhoneTextView = (TextView) findViewById(R.id.tv_phone);
        mInstaReviewLayout = (LinearLayout) findViewById(R.id.ll_insta_review);
        mReviewTextView = (TextView) findViewById(R.id.tv_review);
        mCompensateButton = (Button) findViewById(R.id.btn_compensate);

        mNicknameTextView.setText(data.getUserNickname());
        mPhoneTextView.setText(data.getPhoneNumber());
        mReviewTextView.setText(data.getReviewText());
        mCompensateButton.setEnabled(false);
        mCompensateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2016-11-11 보상하기 버튼이 무엇인지
                Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
            }
        });
        mInstaReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = data.getMissionUrl();
                if (!url.contains("http://") && !url.contains("https://")) {
                    url = "http://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }
}
