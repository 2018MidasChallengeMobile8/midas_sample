package com.dmedia.dlimited.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by min on 2016-09-22.
 */
// TODO: 2016-10-13 scroll 안에 edittext를 스크롤 되게할지 결정(150자 제한 사라졌으므로)
public class EventMakeActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;

    private EditText mTitleEditText;
    private EditText mInformationEditText;
    private EditText mRequestEditText;
    private Button mDateButton;
    private Button mTimeButton;
    private AppCompatSpinner mLimitSpinner;
    private AppCompatSpinner mPlaceSpinner;
    private Button mMakeButton;

    private int year, month, day, hour, minute;
    private String dateString = "";
    private String timeString = "";

    private boolean dateCheck = false;
    private boolean timeCheck = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_make);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("Event 개설");

        mContext = this;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        mTitleEditText = (EditText) findViewById(R.id.et_title);
        //mLetterCountTextView = (TextView) findViewById(R.id.tv_letter_count);
        mInformationEditText = (EditText) findViewById(R.id.et_information);
        mRequestEditText = (EditText) findViewById(R.id.et_request);
        mDateButton = (Button) findViewById(R.id.btn_date);
        mTimeButton = (Button) findViewById(R.id.btn_time);
        mLimitSpinner = (AppCompatSpinner) findViewById(R.id.sp_limit);
        mMakeButton = (Button) findViewById(R.id.btn_make);

        mPlaceSpinner = (AppCompatSpinner) findViewById(R.id.sp_place);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CommonData.mPlaceNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlaceSpinner.setAdapter(adapter);

        /*
        //글자수 count
        mInformationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mLetterCountTextView.setText(s.length() + "/150");
            }
        });
        */
        mDateButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);
        mMakeButton.setOnClickListener(this);
    }

    // TODO: 2016-10-13 버튼은 requestfocus할 필요 없을듯
    private void attemptMakeEvent() {
        View focusView = null;
        boolean cancel = false;
        String errorText = "";

        if (mTitleEditText.length() == 0) {
            errorText = "타이틀이 입력되지 않았습니다.";
            focusView = mTitleEditText;
            cancel = true;
        } else if (!isValidDate()) {
            errorText = "날짜가 설정되지 않았습니다.";
            focusView = mDateButton;
            cancel = true;
        } else if (!isValidTime()) {
            errorText = "시간이 설정되지 않았습니다.";
            focusView = mTimeButton;
            cancel = true;
        }
        if (cancel) {
            Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
            focusView.requestFocus();
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("userid", CommonData.LoginUserData.userId);
            params.put("title", mTitleEditText.getText().toString());
            // TODO: 2016-10-27 장소의 디폴트값은 디브릿지. - 나중에 수정?
            params.put("place_id", CommonData.mPlaceList.get(mPlaceSpinner.getSelectedItemPosition()).id);
            params.put("information", mInformationEditText.getText().toString());
            params.put("requirement", mRequestEditText.getText().toString());
            params.put("start_date", dateString);
            params.put("start_time", timeString);
            // TODO: 2016-10-27 종료시간 parameter로 안넘겨도 될듯 - 기획단계에서 필요없다고 검증
            String capacity = mLimitSpinner.getSelectedItem().toString().replaceAll("[^0-9]", ""); //x명 빼고 숫자만 전달
            params.put("capacity", capacity);
            params.put("session_token", CommonData.LoginUserData.loginToken);

            NetData netData = new NetData(NetData.ProtocolType.EVENT_REQUEST, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerEventRequestCallback);
            netManager.execute((Void) null);
        }
    }

    private void makeEventFinishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventMakeActivity.this);
        builder.setTitle("개설 요청 완료");
        // TODO: 2016-10-13 텍스트를 "이벤트를 개설하시겠습니까? 로 바꾸는게 나을듯"
        builder.setMessage("이벤트 개설 요청을 완료하였습니다.\n문의 사항이 있으시면 \n매장으로 직접 연락주세요.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(EventMakeActivity.this, EventListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });
        builder.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date:
                makeDateDialog();
                break;
            case R.id.btn_time:
                makeTimeDialog();
                break;
            case R.id.btn_make:
                attemptMakeEvent();
                break;
            default:
                break;
        }
    }

    private boolean isValidDate() {
        return dateCheck;
    }

    private boolean isValidTime() {
        return timeCheck;
    }

    private void makeDateDialog() {
        DatePickerDialog d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String monthStr = "";
                String dayStr = "";
                if (monthOfYear + 1 < 10) {
                    monthStr = "0" + (monthOfYear + 1) + "";
                } else {
                    monthStr = (monthOfYear + 1) + "";
                }
                if (dayOfMonth < 10) {
                    dayStr = "0" + dayOfMonth + "";
                } else {
                    dayStr = dayOfMonth + "";
                }
                String date = year + "-" + monthStr + "-" + dayStr;
                mDateButton.setText(date);
                dateString = date;
                dateCheck = true;
            }
        }, year, month, day);
        d.show();
    }

    private void makeTimeDialog() {
        TimePickerDialog t = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hourStr = "";
                String minuteStr = "";
                if (hourOfDay < 10) {
                    hourStr = "0" + hourOfDay + "";
                } else {
                    hourStr = hourOfDay + "";
                }
                if (minute < 10) {
                    minuteStr = "0" + minute + "";
                } else {
                    minuteStr = minute + "";
                }
                String time = hourStr + ":" + minuteStr;
                mTimeButton.setText(time);
                timeString = time;
                timeCheck = true;
            }
        }, hour, minute, false);
        t.show();
    }

    private NetManager.Callbacks mNetManagerEventRequestCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    makeEventFinishDialog();
                } else {
                    Toast.makeText(mContext, "이벤트 개설에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}
