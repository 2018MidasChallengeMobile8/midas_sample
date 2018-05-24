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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
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
 * Created by xema0 on 2016-10-07.
 */

public class ProductMakeActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;

    private EditText mTitleEditText;
    private ImageView mMainImageView;
    private ImageView mMainImageChangeView;
    private EditText mInformationEditText;
    private EditText mMissionEditText;
    private Button mStartDateButton;
    private Button mEndDateButton;
    private AppCompatSpinner mCategorySpinner;
    private AppCompatSpinner mLimitSpinner;
    private AppCompatSpinner mPlaceSpinner;
    private Button mMakeButton;

    private int year, month, day, hour, minute;
    private String startDateAndTimeString = "";
    private String endDateAndTimeString = "";
    private String startDate;
    private String endDate;

    //시간까지 입력 완료해야 유효함
    private boolean startCheck = false;
    private boolean endCheck = false;

    private boolean mainImageCheck = false;

    private final static int MODE_START = 1001;
    private final static int MODE_END = 2001;

    protected static final int REQUEST_MAIN_IMAGE = 2;
    protected static final int REQUEST_SUB_IMAGE = 3;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private File mUploadMainImageFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_make);

        mContext = this;

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("PRODUCT 생성");

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        mCategorySpinner = (AppCompatSpinner) findViewById(R.id.sp_category);
        mTitleEditText = (EditText) findViewById(R.id.et_title);
        mMainImageView = (ImageView) findViewById(R.id.iv_main);
        mMainImageChangeView = (ImageView) findViewById(R.id.iv_change);
        mInformationEditText = (EditText) findViewById(R.id.et_information);
        mMissionEditText = (EditText) findViewById(R.id.et_mission);
        mStartDateButton = (Button) findViewById(R.id.btn_start_date);
        mEndDateButton = (Button) findViewById(R.id.btn_end_date);
        mLimitSpinner = (AppCompatSpinner) findViewById(R.id.sp_limit);
        mPlaceSpinner = (AppCompatSpinner) findViewById(R.id.sp_place);
        mMakeButton = (Button) findViewById(R.id.btn_make);

        //CATEGORY - ALL 제거
        ArrayList<String> categoryList = CommonData.mDBoxCategoryNameList;
        if (categoryList.get(0) != null){
            if (categoryList.get(0).equals("ALL")){
                categoryList.remove(0);
            }
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(categoryAdapter);
        ArrayAdapter<String> placeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CommonData.mPlaceNameList);
        placeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlaceSpinner.setAdapter(placeAdapter);

        mStartDateButton.setOnClickListener(this);
        mEndDateButton.setOnClickListener(this);
        mMakeButton.setOnClickListener(this);
        mMainImageChangeView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_date:
                startCheck = false;
                makeDateDialog(MODE_START);
                break;
            case R.id.btn_end_date:
                endCheck = false;
                makeDateDialog(MODE_END);
                break;
            case R.id.btn_make:
                makeDBox();
                break;
            case R.id.iv_change:
                mainImageCheck = false;
                mMainImageView.setImageDrawable(null);
                pickImage();
                break;
            default:
                break;
        }
    }


    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            MultiImageSelector selector = MultiImageSelector.create(ProductMakeActivity.this);
            selector.showCamera(false);
            //selector.count(10);
            selector.single();
            selector.start(ProductMakeActivity.this, REQUEST_MAIN_IMAGE);
        }
    }

    private void requestPermission(final java.lang.String permission, java.lang.String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ProductMakeActivity.this, new java.lang.String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new java.lang.String[]{permission}, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAIN_IMAGE) {
            if (resultCode == RESULT_OK) {
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                mUploadMainImageFile = new File(path.get(0));
                if (mUploadMainImageFile.exists()) {
                    mainImageCheck = true;
                    Bitmap myBitmap = Utils.decodeSampledBitmapFromResource(mUploadMainImageFile.getAbsolutePath());
                    mMainImageView.setImageBitmap(myBitmap);
                    mMainImageChangeView.setImageResource(R.drawable.p14_bt_modify);
                }
            }
        }
    }

    private void mainImageUpload(int dboxId) {
        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("이미지 업로드 중입니다.");
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("dbox_id", dboxId);
        params.put("userid", CommonData.LoginUserData.userId);
        File[] imgArr = new File[1];
        imgArr[0] = mUploadMainImageFile;
        try {
            params.put("img_list", imgArr);
        } catch (FileNotFoundException e) {
            Toast.makeText(mContext, "이미지를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
        params.put("is_main", 1);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        client.post(ProductMakeActivity.this, "http://www.dlimited.co.kr/api/dbox/image/put", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (pd != null && pd.isShowing()) {
                    pd.hide();
                }
                makeFinishDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (pd != null && pd.isShowing()) {
                    pd.hide();
                }
                Toast.makeText(mContext, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeDBox() {
        View focusView = null;
        boolean cancel = false;
        String errorText = "";

        if (mTitleEditText.length() == 0) {
            errorText = "타이틀이 입력되지 않았습니다.";
            focusView = mTitleEditText;
            cancel = true;
        } else if (!isValidStart()) {
            errorText = "시작 날짜가 설정되지 않았습니다.";
            focusView = mStartDateButton;
            cancel = true;
        } else if (!isValidEnd()) {
            errorText = "종료 날짜가 설정되지 않았습니다.";
            focusView = mEndDateButton;
            cancel = true;
        }
        if (cancel) {
            Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
            focusView.requestFocus();
        } else {

            CommonData.mDBoxCategoryList.get(mCategorySpinner.getSelectedItemPosition());

            HashMap<String, Object> params = new HashMap<>();
            params.put("category_id", CommonData.mDBoxCategoryList.get(mCategorySpinner.getSelectedItemPosition()).id);
            params.put("userid", CommonData.LoginUserData.userId);
            params.put("title", mTitleEditText.getText().toString());
            params.put("place_id", CommonData.mPlaceList.get(mPlaceSpinner.getSelectedItemPosition()).id);
            params.put("information", mInformationEditText.getText().toString());
            params.put("mission", mMissionEditText.getText().toString());
            params.put("start", startDateAndTimeString);
            params.put("end", endDateAndTimeString);
            String capacity = mLimitSpinner.getSelectedItem().toString().replaceAll("[^0-9]", ""); //x명 빼고 숫자만 전달
            params.put("capacity", capacity);
            // TODO: 2016-11-08 파라미터중에 share_url이 뭔지
            params.put("share_url", "www.dlimited.co.kr");
            params.put("session_token", CommonData.LoginUserData.loginToken);

            NetData netData = new NetData(NetData.ProtocolType.DBOX_REQUEST, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
            NetManager netManager = new NetManager(netData, mContext);
            netManager.setCallback(mNetManagerDBoxRequestCallback);
            netManager.execute((Void) null);

        }
    }

    private boolean isValidStart() {
        return startCheck;
    }

    private boolean isValidEnd() {
        return endCheck;
    }


    private void makeDateDialog(final int mode) {
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
                if (mode == MODE_START) {
                    makeTimeDialog(MODE_START);
                    startDate = year + "-" + monthStr + "-" + dayStr;
                    mStartDateButton.setText("");
                } else if (mode == MODE_END) {
                    makeTimeDialog(MODE_END);
                    endDate = year + "-" + monthStr + "-" + dayStr;
                    mEndDateButton.setText("");
                }
            }
        }, year, month, day);
        d.show();
    }

    private void makeTimeDialog(final int mode) {
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
                if (mode == MODE_START) {
                    String time = hourStr + ":" + minuteStr;
                    startDateAndTimeString = startDate + " " + time;
                    mStartDateButton.setText(startDateAndTimeString);
                    startCheck = true;
                } else if (mode == MODE_END) {
                    String time = hourStr + ":" + minuteStr;
                    endDateAndTimeString = endDate + " " + time;
                    mEndDateButton.setText(endDateAndTimeString);
                    endCheck = true;
                }
            }
        }, hour, minute, false);
        t.setCanceledOnTouchOutside(false);
        t.setCancelable(false);

        t.show();
    }

    private void makeFinishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductMakeActivity.this);
        builder.setTitle("개설 요청 완료");
        builder.setCancelable(false);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(ProductMakeActivity.this, ProductListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });
        builder.setMessage("D-Box 개설 요청을 완료하였습니다.\n문의 사항이 있으시면 \n매장으로 직접 연락주세요.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ProductMakeActivity.this, ProductListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });
        builder.show();
    }


    private NetManager.Callbacks mNetManagerDBoxRequestCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    JSONObject dbox = jsonObject.getJSONObject("dbox");
                    //이미지도 같이 업로드?
                    if (mainImageCheck) {
                        mainImageUpload(dbox.getInt("id"));
                    } else {
                        makeFinishDialog();
                    }
                } else {
                    Toast.makeText(mContext, "D-Box 개설에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
