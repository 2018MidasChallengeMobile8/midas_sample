package com.dmedia.dlimited;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.chenglei.widget.datepicker.DatePicker;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

/**
 * Created by min on 2016-09-18.
 */
// TODO: 2016-11-01 테스트해보기
// TODO: 2016-09-23 error 세팅
public class AccountSignupActivity2 extends AppCompatActivity implements View.OnClickListener {
    public static Activity mSignupActivty2;
    Context mContext;
    private EditText mNicknameEditText;
    private Button mGenderButton;
    private Button mBirthButton;
    private Button mLocationButton;
    private Button mNextButton;

    private int year, month, day;
    private boolean genderCheck = false;
    private boolean birthCheck = false;
    private boolean locationCheck = false;

    //서버에 넘길 변수
    private String phone = "";
    private String password = "";
    private String nickname = "";
    private String gender = "";
    private String birth = "";
    private String location = "";
    private String instaId = "";
    private String instaName = "";
    private String profileUrl = "";

    private LinearLayout mInstaLayout;

    private InstagramApp mApp;
    private TextView mInstaTextView;

    private boolean instaAuthCheck = false;

    private DatePicker mBirthDatePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup_2);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);//뒤로가기
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("회원 가입");
        mInstaTextView = (TextView) findViewById(R.id.tv_insta);

        mContext = this;
        mSignupActivty2 =this;

        mApp = new InstagramApp(this, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        mApp.setListener(instagramListener);

        Intent intent = getIntent();
        if (intent != null) {
            phone = intent.getStringExtra("phone");
            password = intent.getStringExtra("password");
        }

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mNicknameEditText = (EditText) findViewById(R.id.edt_nickname);
        mGenderButton = (Button) findViewById(R.id.btn_gender);
        mBirthButton = (Button) findViewById(R.id.btn_birth);
        mLocationButton = (Button) findViewById(R.id.btn_location);
        mNextButton = (Button) findViewById(R.id.btn_next);
        mInstaLayout = (LinearLayout) findViewById(R.id.rl_connect_insta);

        mGenderButton.setOnClickListener(this);
        mBirthButton.setOnClickListener(this);
        mLocationButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mInstaLayout.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_connect_insta:
                mApp.authorize();
                break;
            case R.id.btn_gender:
                makeGenderDialog();
                break;
            case R.id.btn_birth:
                makeBirthDialog();
                break;
            case R.id.btn_location:
                makeLocationDialog();
                break;
            case R.id.btn_next:
                nickname = mNicknameEditText.getText().toString();
                if (Utils.isNicknameValid(nickname)) {
                    if (genderCheck) {
                        if (birthCheck) {
                            if (locationCheck) {
                                if (instaAuthCheck) {
                                    //모든 형식 일치
                                    HashMap<String, Object> params = new HashMap<>();
                                    params.put("userid", phone);
                                    params.put("password", password);
                                    params.put("username", nickname);
                                    params.put("gender", gender);
                                    params.put("birthday", birth);
                                    params.put("address", location);
                                    params.put("instagram", instaId);
                                    params.put("instagram_name", instaName);
                                    params.put("profile_img_url", profileUrl);
                                    params.put("udid", Utils.getUDID(mContext));
                                    NetData netData = new NetData(NetData.ProtocolType.USER_SIGNUP, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                    NetManager netManager = new NetManager(netData, mContext);
                                    netManager.setCallback(mNetManagerSignUpCallback);
                                    netManager.execute((Void) null);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setTitle("확인");
                                    builder.setMessage("Instagram 연동을 안하시면\n서비스 이용에 제한이 있을 수 있습니다.");
                                    builder.setPositiveButton("가입", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            HashMap<String, Object> params = new HashMap<>();
                                            params.put("userid", phone);
                                            params.put("password", password);
                                            params.put("username", nickname);
                                            params.put("gender", gender);
                                            params.put("birthday", birth);
                                            //server api에서 프로필 url 넣어주지않으면 에러 발생. 더미이미지 주소를 보낸다.
                                            params.put("profile_img_url", getString(R.string.instagram_dummy_image_url));
                                            params.put("address", location);
                                            params.put("udid", Utils.getUDID(mContext));
                                            NetData netData = new NetData(NetData.ProtocolType.USER_SIGNUP, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                            NetManager netManager = new NetManager(netData, mContext);
                                            netManager.setCallback(mNetManagerSignUpCallback);
                                            netManager.execute((Void) null);
                                        }
                                    });
                                    builder.setNegativeButton("돌아가기",null);
                                    builder.show();
                                }
                            } else {
                                //장소 선택 안함
                                Toast.makeText(this, "장소를 선택해주세요", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //생일 선택 안함
                            Toast.makeText(this, "생일을 선택해주세요", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //성별 선택 안함
                        Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //닉네임 부적합
                    mNicknameEditText.requestFocus();
                    Toast.makeText(this, "닉네임은 2자리 이상,14자리 이하로 설정해주세요", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //Spinner Dialog 생성
    private void makeGenderDialog() {
        final String[] items = {"남성", "여성"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("성별 선택");
        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGenderButton.setText(items[which]);
                if (which == 0) {
                    gender = "male";
                } else if (which == 1) {
                    gender = "female";
                }
                genderCheck = true;
            }
        });
        b.show();
    }

    /*
    private void makeBirthDialog() {
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
                mBirthButton.setText(date);
                birth = date;
                birthCheck = true;
            }
        }, year, month, day);
        d.show();
    }
    */


    private void makeBirthDialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_datepicker, null);
        AlertDialog.Builder buider = new AlertDialog.Builder(this);
        buider.setTitle("생년월일");
        buider.setView(dialogView);
        mBirthDatePicker = (DatePicker) dialogView.findViewById(R.id.dp_date);
        mBirthDatePicker.setTextColor(Color.BLACK)
                .setFlagTextColor(Color.BLACK)
                .setBackground(Color.WHITE)
                .setTextSize(50)
                .setFlagTextSize(15)
                .setRowNumber(5);
        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = mBirthDatePicker.getYear();
                int monthOfYear = mBirthDatePicker.getMonth()-1;
                int dayOfMonth = mBirthDatePicker.getDayOfMonth();
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
                mBirthButton.setText(date);
                birth = date;
                birthCheck = true;
            }
        });
        buider.setNegativeButton("취소", null);
        AlertDialog dialog = buider.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    //Spinner Dialog 생성
    private void makeLocationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("장소 선택");
        final String[] items = getResources().getStringArray(R.array.category_location);
        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLocationButton.setText(items[which]);
                location = items[which];
                locationCheck = true;
            }
        });
        b.show();
    }

    /*
    private void makeSignUpFinishDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("가입 완료");
        // TODO: 2016-10-27 다일러로그 메세지 바꾸기
        b.setCancelable(false);
        b.setMessage("정상적으로 가입되었습니다.\nD-Code를 입력하시면 모든 콘텐츠를 이용 가능합니다.\n입력하시겠습니까?");
        b.setPositiveButton("입력", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AccountSignupActivity2.this, AccountSignupActivity3.class);
                intent.putExtra("phone", phone);
                intent.putExtra("password", password);
                startActivity(intent);
                finish();
            }
        });
        b.setNegativeButton("넘어가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AccountSignupActivity2.this, AccountLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("phone", phone);
                intent.putExtra("password", password);
                startActivity(intent);
                finish();
            }
        });
        b.show();
    }
    */

    InstagramApp.OAuthAuthenticationListener instagramListener = new InstagramApp.OAuthAuthenticationListener() {

        @Override
        public void onSuccess() {
            mInstaTextView.setText(mApp.getUserName() + "으로 연결되었습니다");
            instaName = mApp.getUserName();
            //instaId = mApp.getUserName();
            instaId = mApp.getId();
            profileUrl = mApp.getProfileUrl();
            mInstaLayout.setOnClickListener(null);//비활성화
            //mApp.resetAccessToken();//인스타그램 세션 초기화
            instaAuthCheck = true;
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(AccountSignupActivity2.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private NetManager.Callbacks mNetManagerSignUpCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    CommonData.LoginUserData.loginToken = jsonObject.getString("session_token");
                    //makeSignUpFinishDialog();
                    Intent intent = new Intent(AccountSignupActivity2.this, AccountSignupActivity3.class);
                    intent.putExtra("phone", phone);
                    intent.putExtra("password", password);
                    startActivity(intent);
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "회원가입에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 2) {
                    Toast.makeText(mContext, "이미 가입된 전화번호입니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mContext, "서버와 통신을 할 수 없습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
