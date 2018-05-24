package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;
import com.dmedia.dlimited.others.VersionChecker;

import java.util.concurrent.ExecutionException;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

/**
 * Created by xema0 on 2016-10-20.
 */
// TODO: 2016-11-13 스토어 -> 업데이트 주소 붙이기
public class SettingsVersionActivity extends AppCompatActivity {
    Context mContext;

    private LinearLayout mUpdateLinearLayout;
    private TextView mUpdateStatusTextView;
    private TextView mLatestVersionTextView;
    private TextView mCurrentVersionTextView;
    //private TextView mSdkVersionTextView;

    private String currentVersion;//어플리케이션 버전
    private String latestVersion;//스토어 버전

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_version);
        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("버전정보");

        mUpdateLinearLayout = (LinearLayout) findViewById(R.id.ll_update);
        mUpdateStatusTextView = (TextView) findViewById(R.id.tv_version_state);
        mLatestVersionTextView = (TextView) findViewById(R.id.tv_latest_version_code);
        mCurrentVersionTextView = (TextView) findViewById(R.id.tv_current_version_code);
        //mSdkVersionTextView = (TextView) findViewById(R.id.tv_sdk_ver);

        currentVersion = Utils.getAppVersion(mContext);
        mCurrentVersionTextView.setText(currentVersion);
        //mSdkVersionTextView.setText(Build.VERSION.SDK_INT);

        //sdk 21이상 버전 필요
        /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            pi.applicationInfo.minSdkVersion;
        }
        */

        mUpdateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dmedia.dlimited"));
                startActivity(intent);
            }
        });


        VersionChecker versionChecker = new VersionChecker();
        try {
            latestVersion = versionChecker.execute().get();
            mLatestVersionTextView.setText(latestVersion);
            if (latestVersion != null) {
                if (currentVersion.compareTo(latestVersion) >= 0) {
                    mUpdateStatusTextView.setText("현재 최신 버전을 사용중입니다.");
                } else {
                    mUpdateStatusTextView.setText("현재 최신 버전이 아닙니다.");
                }
            } else {
                mUpdateStatusTextView.setText("네트워크 접속상태를 확인해주세요.");
            }
        } catch (InterruptedException e) {
            mUpdateStatusTextView.setText("네트워크 접속상태를 확인해주세요.");
        } catch (ExecutionException e) {
            mUpdateStatusTextView.setText("네트워크 접속상태를 확인해주세요.");
        }


        //서버에서 버전정보 가져오는 방식 -> 마켓의 버전정보 HTML 파싱하는 방식으로 수정
        /*
        HashMap<String, Object> params = new HashMap<>();
        NetData netData = new NetData(NetData.ProtocolType.INFO_VERSION, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerInfoVersionCallback);
        netManager.execute((Void) null);
        */
    }

    /*
    private NetManager.Callbacks mNetManagerInfoVersionCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                if (jsonObject.getInt("result") == 1) {
                    latestVersion = jsonObject.getString("version");
                    mLatestVersionTextView.setText(latestVersion);
                    if (currentVersion.equals(latestVersion)) {
                        mUpdateStatusTextView.setText("현재 최신 버전을 사용중입니다.");
                    } else {
                        mUpdateStatusTextView.setText("현재 최신 버전이 아닙니다.");
                    }

                } else {
                    Toast.makeText(mContext, "서버와의 통신중 에러가 발생했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        super.finish();
    }
}
