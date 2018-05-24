package com.dmedia.dlimited.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.adapter.SubImageAdapter;
import com.dmedia.dlimited.model.SubImageData;
import com.dmedia.dlimited.util.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by xema0 on 2016-10-06.
 */

public class EventModifyActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;

    private ImageView mMainImage;
    private ImageView mMainImageChangeImageView;
    private Button mSubImageAddButton;

    private Bitmap[] mUploadBitmap;

    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_MAIN_IMAGE = 2;
    protected static final int REQUEST_SUB_IMAGE = 3;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<String> path;

    private int eventId = 0;
    private int mainImageId;
    private String mainImageFilePath;
    private boolean mainImageUploadCheck = false;
    private Bundle b;
    private String mainImageUrl = "";
    private SubImageAdapter mSubImageAdapter;
    private ArrayList<SubImageData> mSubImageDataList;
    private ProgressDialog pd;

    private EditText mTitleEditText;
    private EditText mInformationEditText;
    private Button mDateButton;
    private Button mTimeButton;
    private AppCompatSpinner mLimitSpinner;
    private AppCompatSpinner mPlaceSpinner;
    private Button mModifyButton;
    private int year, month, day, hour, minute;
    private String dateString = "";
    private String timeString = "";
    private boolean dateCheck = false;
    private boolean timeCheck = false;

    private String dateAndTime;
    private int placeId;
    private int limit;

    private File mainImageFile;
    //private Bitmap mMainBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_modify);

        mContext = this;
        mMainImageChangeImageView = (ImageView) findViewById(R.id.iv_change);

        if (getIntent() != null) {
            eventId = getIntent().getIntExtra("event_id", 0);
            b = getIntent().getBundleExtra("data");
            //mDataList = b.getStringArrayList("info_image_url");
            mainImageUrl = b.getString("main_image_url");
            mSubImageDataList = b.getParcelableArrayList("sub_image_data");
            mainImageId = b.getInt("main_image_id", -1);

            dateAndTime = getIntent().getStringExtra("date_and_time");
            placeId = getIntent().getIntExtra("place_id", -1);
            limit = getIntent().getIntExtra("limit", -1);
        } else {
            //mDataList = new ArrayList<>();
            mainImageUrl = "";
            mSubImageDataList = new ArrayList<>();
        }
        if (mainImageId != -1) {
            mMainImageChangeImageView.setImageResource(R.drawable.p14_bt_modify);
        }

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("수정하기");

        pd = new ProgressDialog(mContext);
        pd.setMessage("이미지 업로드 중입니다.");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        mMainImage = (ImageView) findViewById(R.id.iv_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mSubImageAdapter = new SubImageAdapter(mContext, mSubImageDataList, mRecyclerView, SubImageAdapter.MODE_MODIFY);
        mRecyclerView.setAdapter(mSubImageAdapter);

        if (mainImageUrl != null)
            Glide.with(mContext).load(mainImageUrl).into(mMainImage);

        mSubImageAddButton = (Button) findViewById(R.id.btn_image_add);
        mMainImageChangeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(REQUEST_MAIN_IMAGE);
            }
        });
        mSubImageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(REQUEST_SUB_IMAGE);
            }
        });


        mTitleEditText = (EditText) findViewById(R.id.et_title);
        mInformationEditText = (EditText) findViewById(R.id.et_information);
        mDateButton = (Button) findViewById(R.id.btn_date);
        mTimeButton = (Button) findViewById(R.id.btn_time);
        mLimitSpinner = (AppCompatSpinner) findViewById(R.id.sp_limit);
        mPlaceSpinner = (AppCompatSpinner) findViewById(R.id.sp_place);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CommonData.mPlaceNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlaceSpinner.setAdapter(adapter);
        mDateButton.setOnClickListener(this);
        mTimeButton.setOnClickListener(this);

        mTitleEditText.setText(b.getString("info_title"));
        mInformationEditText.setText(b.getString("info_info"));

        if (dateAndTime != null) {
            if (!dateAndTime.equals("")) {
                mDateButton.setText(dateAndTime.substring(0, 10));
                mTimeButton.setText(dateAndTime.substring(11, 16));
                dateCheck = true;
                timeCheck = true;
            }
        }
        if (placeId != -1) {
            for (int i = 0; i < CommonData.mPlaceList.size(); i++) {
                if (CommonData.mPlaceList.get(i).id == placeId) {
                    mPlaceSpinner.setSelection(i);
                }
            }
        }
        if (limit != -1) {
            for (int i = 0; i < mLimitSpinner.getAdapter().getCount(); i++) {
                if (mLimitSpinner.getItemAtPosition(i).toString().replaceAll("[^0-9]", "").equals(limit + "")) {
                    mLimitSpinner.setSelection(i);

                }
            }
        }

        mModifyButton = (Button) findViewById(R.id.btn_modify);
        mModifyButton.setOnClickListener(this);
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
            case R.id.btn_modify:
                attemptModifyEvent();
                break;
            default:
                break;
        }
    }

    private void attemptModifyEvent() {

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
            pd.show();
            //메인이미지 업로드 해야하는지 결정
            if (mainImageUploadCheck) {
                //원래있던 메인 이미지 삭제
                if (mainImageId != -1) {
                    serverMainImageDelete();
                } else {
                    //서버에있는 메인이미지 삭제할 필요 X
                    mainImageUpload();
                }

            } else {
                subImageUploadAndModifyEvent();
            }

        }
    }

    private void serverMainImageDelete() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("event_img_id", mainImageId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_IMAGE_REMOVE, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventImageRemoveCallback);
        netManager.execute((Void) null);
    }

    private void mainImageUpload() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("event_id", eventId);
        params.put("userid", CommonData.LoginUserData.userId);
        File[] imgArr = new File[1];
        //imgArr[0] = new File(mainImageFilePath);
        imgArr[0] = mainImageFile;
        try {
            params.put("img_list", imgArr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        params.put("is_main", 1);



        /*
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mMainBitmap.compress(Bitmap.CompressFormat.PNG, 2, out);
        byte[] myByteArray = out.toByteArray();
        params.put("img_list", new ByteArrayInputStream(myByteArray), "image.png");
        */

        params.put("session_token", CommonData.LoginUserData.loginToken);
        client.post(EventModifyActivity.this, "http://www.dlimited.co.kr/api/event/image/put", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                subImageUploadAndModifyEvent();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (pd != null && pd.isShowing()) {
                    pd.hide();
                }
                Toast.makeText(mContext, "이미지 업로드중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subImageUploadAndModifyEvent() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("event_id", eventId);
        params.put("userid", CommonData.LoginUserData.userId);
        ArrayList<String> tmpFilePathArrayList = new ArrayList<>();
        for (int i = 0; i < mSubImageDataList.size(); i++) {
            if (mSubImageDataList.get(i).isFile()) {
                tmpFilePathArrayList.add(mSubImageDataList.get(i).getFilePath());
            }
        }
        File[] imgArr = new File[tmpFilePathArrayList.size()];
        for (int i = 0; i < tmpFilePathArrayList.size(); i++) {
            imgArr[i] = new File(tmpFilePathArrayList.get(i));
        }
        try {
            params.put("img_list", imgArr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("is_main", 0);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        client.post(EventModifyActivity.this, "http://www.dlimited.co.kr/api/event/image/put", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("event_id", eventId);
                params.put("userid", CommonData.LoginUserData.userId);
                params.put("title", mTitleEditText.getText().toString());
                params.put("place_id", CommonData.mPlaceList.get(mPlaceSpinner.getSelectedItemPosition()).id);
                params.put("information", mInformationEditText.getText().toString());
                params.put("start_date", dateString);
                params.put("start_time", timeString);
                String capacity = mLimitSpinner.getSelectedItem().toString().replaceAll("[^0-9]", ""); //x명 빼고 숫자만 전달
                params.put("capacity", capacity);
                params.put("session_token", CommonData.LoginUserData.loginToken);

                NetData netData = new NetData(NetData.ProtocolType.EVENT_EDIT, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                NetManager netManager = new NetManager(netData, mContext);
                netManager.setCallback(mNetManagerEventEditCallback);
                netManager.execute((Void) null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (pd != null && pd.isShowing()) {
                    pd.hide();
                }
                Toast.makeText(mContext, "이미지 업로드중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void requestPermission(final java.lang.String permission, java.lang.String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EventModifyActivity.this, new java.lang.String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new java.lang.String[]{permission}, requestCode);
        }
    }

    private void pickImage(final int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            MultiImageSelector selector = MultiImageSelector.create(EventModifyActivity.this);
            selector.showCamera(false);
            selector.count(10);
            if (mode == REQUEST_MAIN_IMAGE) {
                selector.single();
                selector.start(EventModifyActivity.this, REQUEST_MAIN_IMAGE);
            } else if (mode == REQUEST_SUB_IMAGE) {
                //selector.multi();
                selector.single();
                selector.start(EventModifyActivity.this, REQUEST_SUB_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAIN_IMAGE) {
            if (resultCode == RESULT_OK) {
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                File imgFile = new File(path.get(0));
                //Toast.makeText(mContext, (imgFile.length()/1024)+"", Toast.LENGTH_SHORT).show();
                if (imgFile.exists()) {
                    mainImageFilePath = path.get(0);
                    mainImageUploadCheck = true;
                    Bitmap myBitmap = Utils.decodeSampledBitmapFromResource(mainImageFilePath);

                    mainImageFile = Utils.saveBitmapToFileCache(myBitmap, mainImageFilePath, "");
                    //Toast.makeText(mContext, (mainImageFile.length() / 1024) + "", Toast.LENGTH_SHORT).show();
                    //mMainBitmap =  Utils.decodeSampledBitmapFromResource(mainImageFilePath);
                    mMainImage.setImageBitmap(myBitmap);
                    mMainImageChangeImageView.setImageResource(R.drawable.p14_bt_modify);
                }
            }
        } else if (requestCode == REQUEST_SUB_IMAGE) {
            if (resultCode == RESULT_OK) {
                path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                File imgFile = new File(path.get(0));
                if (imgFile.exists()) {
                    mSubImageDataList.add(new SubImageData(eventId, path.get(0), true));
                    mSubImageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private NetManager.Callbacks mNetManagerEventImageRemoveCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    mainImageUpload();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private NetManager.Callbacks mNetManagerEventEditCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    if (pd != null && pd.isShowing()) {
                        pd.hide();
                    }
                    Toast.makeText(mContext, "이벤트를 수정했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (pd != null && pd.isShowing()) {
                        pd.hide();
                    }
                    Toast.makeText(mContext, "이벤트 수정에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                if (pd != null && pd.isShowing()) {
                    pd.hide();
                }
                e.printStackTrace();
            }
        }
    };

}
