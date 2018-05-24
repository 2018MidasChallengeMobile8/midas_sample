package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-18.
 */

// TODO: 2016-11-13 insta url 은 에딧텍스트?? 아니면 텍스트뷰???
public class ProductReviewApplyActivity extends AppCompatActivity {
    Context mContext;

    private TextView mNicknameTextView;
    private TextView mPhoneTextView;
    private EditText mInstaUrlEditText;
    private EditText mReviewEditText;
    private Button mReviewButton;

    private int dboxId;
    private String userId;
    private String phone;
    private String missionUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review_apply);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("리뷰하기");

        mNicknameTextView = (TextView) findViewById(R.id.tv_nickname);
        mPhoneTextView = (TextView) findViewById(R.id.tv_phone);
        mInstaUrlEditText = (EditText) findViewById(R.id.tv_insta_url);
        mReviewEditText = (EditText) findViewById(R.id.edt_review);
        mReviewButton = (Button) findViewById(R.id.btn_compensate);

        dboxId = getIntent().getIntExtra("dbox_id", -1);
        phone = CommonData.LoginUserData.userId;
        //missionUrl = "https://www.instagram.com/" + CommonData.LoginUserData.instagramId + "/";
        // TODO: 2016-11-13 서버에서 전화번호와 유저아이디(전화번호)를 둘다 받음 -> 실수인것같지만 문제없으니...
        userId = CommonData.LoginUserData.userId;

        mNicknameTextView.setText(CommonData.LoginUserData.userName);
        mPhoneTextView.setText(phone);
        //mInstaUrlEditText.setText("@" + CommonData.LoginUserData.instagramId);

        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                missionUrl = mInstaUrlEditText.getText().toString();
                if (missionUrl.length() == 0) {
                    mInstaUrlEditText.requestFocus();
                    Toast.makeText(mContext, "해당 리뷰가 있는 인스타그램 주소를 적어주세요", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("확인");
                    builder.setMessage("리뷰를 작성하시겠습니까?");
                    builder.setPositiveButton("작성", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("dbox_id", dboxId);
                            params.put("userid", userId);
                            params.put("phonenumber", phone);
                            params.put("mission_url", missionUrl);
                            params.put("review", mReviewEditText.getText().toString());
                            NetData netData = new NetData(NetData.ProtocolType.DBOX_REVIEW_PUT, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                            NetManager netManager = new NetManager(netData, mContext);
                            netManager.setCallback(mNetManagerDBoxReviewPutCallback);
                            netManager.execute((Void) null);
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.show();
                }
            }
        });
    }

    private NetManager.Callbacks mNetManagerDBoxReviewPutCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");

                if (resultCode == 1) {
                    Toast.makeText(mContext, "작성되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
