package com.dmedia.dlimited;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
// TODO: 2016-10-02 setting 세부사항 구현

/**
 * Created by min on 2016-09-29.
 */
/*
 * 인스타그램 지정 디자인 없음
 * 이벤트 기록 전체 삭제 - 다이얼로그 디자인 없음(기본으로 사용?)
 * 로그아웃 다이얼로그 디자인 없음
 */
// TODO: 2016-10-27 D-Code 세팅에서도 인증 가능하도록 하기
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // deprecated 된 addPreferenceFromResource 대신 프래그먼트 사용
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.p08_bt_back);
            actionBar.setTitle("설정");
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            // remove dividers
            View rootView = getView();
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(null);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }
    }

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
        Intent intent = new Intent(this, MyPageTabActivity.class);
        intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        super.finish();
    }
}
