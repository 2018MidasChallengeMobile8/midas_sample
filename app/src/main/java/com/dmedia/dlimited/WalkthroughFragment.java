package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.content.Context.MODE_PRIVATE;
import static com.dmedia.dlimited.IntroActivity.PREFERENCE_TUTORIAL;
import static com.dmedia.dlimited.IntroActivity.PREFERENCE_WALK_THROUGH_CHECKED;

/**
 * Created by min on 2016-09-29.
 */
public class WalkthroughFragment extends Fragment {
    private Context mContext;
    private int position = 0;

    private ImageView mTutorialImageView;
    private Button mStartButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walkthrough, null);

        mTutorialImageView = (ImageView) view.findViewById(R.id.iv_tutorial);
        mStartButton = (Button) view.findViewById(R.id.btn_start);

        switch (position) {
            case 0:
                mTutorialImageView.setImageResource(R.drawable.img_tutorial01);
                break;
            case 1:
                mTutorialImageView.setImageResource(R.drawable.img_tutorial02);
                break;
            case 2:
                mTutorialImageView.setImageResource(R.drawable.img_tutorial03);
                break;
            case 3:
                mTutorialImageView.setImageResource(R.drawable.img_tutorial04);
                mStartButton.setVisibility(Button.VISIBLE);
                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences workThrougnPrefs = mContext.getSharedPreferences(PREFERENCE_TUTORIAL, MODE_PRIVATE);
                        SharedPreferences.Editor editor = workThrougnPrefs.edit();
                        editor.putString(PREFERENCE_WALK_THROUGH_CHECKED, PREFERENCE_WALK_THROUGH_CHECKED);
                        editor.commit();

                        Intent intent = new Intent(mContext, AccountLoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                break;
            default:
                break;
        }

        return view;
    }
}
