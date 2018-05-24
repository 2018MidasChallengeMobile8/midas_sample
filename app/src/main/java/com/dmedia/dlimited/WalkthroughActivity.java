package com.dmedia.dlimited;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by xema0 on 2016-11-13.
 */

public class WalkthroughActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private CircleIndicator mIndicator;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);

        mViewPager = (ViewPager) findViewById(R.id.vp_walkthrough);
        mIndicator = (CircleIndicator) findViewById(R.id.ci_indicator);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mIndicator.setViewPager(mViewPager);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new WalkthroughFragment();
            Bundle b = new Bundle();
            b.putInt("position", position);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

}
