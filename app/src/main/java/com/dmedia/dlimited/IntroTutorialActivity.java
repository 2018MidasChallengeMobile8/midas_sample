package com.dmedia.dlimited;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by min on 2016-09-18.
 */
//기획상으로 수정됨 - 튜토리얼 삭제.
public class IntroTutorialActivity extends AppCompatActivity {
    private static final String TAG = "IntroTutorialActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    Context mContext;

    private ViewPager mViewPager;
    private Button mDirectLoginButton;
    private Button mSignupButton;
    private ImageView mTutorialImage;

    private TextView mTutorialText;

    private int currentFragmentPosition;

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_tutorial);

        mContext = this.getApplicationContext();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mTutorialText = (TextView) findViewById(R.id.tv_tutorial);
        //mTutorialImage = (ImageView) findViewById(R.id.iv_tutorial_img);
        //mTutorialImage.setImageDrawable(getResources().getDrawable(R.drawable.testimage));
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //String stepTitle = "";
                //String stepContent = "";
                currentFragmentPosition = position;
                switch (position) {
                    case 0:
                        //mTutorialImage.setImageDrawable(getResources().getDrawable(R.drawable.testimage));
                        break;
                    case 1:
                        //mTutorialImage.setImageDrawable(getResources().getDrawable(R.drawable.testimage2));
                        break;
                    case 2:
                        //mTutorialImage.setImageDrawable(getResources().getDrawable(R.drawable.testimage));
                        break;
                }
                mTutorialText.setText("TUTORIAL TEST\nPage :  " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        indicator.setViewPager(mViewPager);

        mDirectLoginButton = (Button) findViewById(R.id.btn_direct_login);
        mDirectLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, AccountLoginActivity.class);
                startActivity(intent);
            }
        });

        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, AccountSignupAgreementActivity.class);
                startActivity(intent);
            }
        });

        backPressCloseHandler = new BackPressCloseHandler(this);

    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        private ImageView mImageBackgroundView;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_intro_tutorial, container, false);
            mImageBackgroundView = (ImageView) rootView.findViewById(R.id.iv_tutorial);
            switch (getArguments().getInt(ARG_SECTION_NUMBER, 1)) {
                case 1:
                    //mImageBackgroundView.setImageDrawable(getResources().getDrawable(R.drawable.testimage));
                    mImageBackgroundView.setBackgroundColor(Color.DKGRAY);
                    break;
                case 2:
                    //mImageBackgroundView.setImageDrawable(getResources().getDrawable(R.drawable.testimage2));
                    mImageBackgroundView.setBackgroundColor(Color.LTGRAY);
                    break;
                case 3:
                    //mImageBackgroundView.setImageDrawable(getResources().getDrawable(R.drawable.testimage));
                    mImageBackgroundView.setBackgroundColor(Color.BLACK);
                    break;
            }
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}
