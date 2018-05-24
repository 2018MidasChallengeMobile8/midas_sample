package com.dmedia.dlimited;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by min on 2016-09-18.
 */
// TODO: 2016-10-12 TextInputLayout 사용해서 error 테마 바꾸기
public class AccountLoginActivity extends AppCompatActivity {
    private static final String TAG = "AccountLoginActivity";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    public static final String PREFERENCE_LOGIN = "preference_login";
    public static final String PREFERENCE_LOGIN_ID = "preference_login_id";
    public static final String PREFERENCE_LOGIN_PASSWORD = "preference_login_password";
    public static final String PREFERENCE_LOGIN_TOKEN = "preference_login_token";
    public static final String PREFERENCE_LOGIN_UDID = "preference_login_udid";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    Context mContext;

    // UI references.
    private EditText mPhoneEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mPasswordFindTextView;
    private Button mSignupButton;
    private TextView mLookingTextView; //구경하기

    //회원가입후 되돌아왔을때
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String phone = intent.getStringExtra("phone");
        String password = intent.getStringExtra("password");
        if (!phone.equals("") && phone != null) {
            mPhoneEditText.setText(phone);
        }
        if (!password.equals("") && password != null) {
            mPasswordEditText.setText(password);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);

        mContext = this;

        mPhoneEditText = (EditText) findViewById(R.id.edt_phone);
        mPasswordEditText = (EditText) findViewById(R.id.edt_password);

        //비밀번호 변경, 비밀번호 재설정에서 넘어온 정보 자동입력
        if (getIntent().getStringExtra("phone") != null && !getIntent().getStringExtra("phone").equals("")) {
            mPhoneEditText.setText(getIntent().getStringExtra("phone"));
        }
        if (getIntent().getStringExtra("password") != null && !getIntent().getStringExtra("password").equals("")) {
            mPasswordEditText.setText(getIntent().getStringExtra("password"));
        }

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginButton = (Button) findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AccountSignupAgreementActivity.class);
                startActivity(intent);
            }
        });


        mPasswordFindTextView = (TextView) findViewById(R.id.tv_find_password);
        mPasswordFindTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AccountFindPasswordActivity.class);
                startActivity(intent);
            }
        });

        mLookingTextView = (TextView) findViewById(R.id.tv_looking);
        mLookingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2016-11-05 레벨도 추가될수 있으니 나중에 서버 사용하는걸로 수정
                CommonData.LoginUserData.level = "looker";
                Intent intent = new Intent(mContext, HomeTabActivity.class);
                startActivity(intent);
            }
        });

    }

    private void attemptLogin() {
        // Reset errors.
        mPhoneEditText.setError(null);
        mPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid form
        if (TextUtils.isEmpty(phone)) {
            mPhoneEditText.setError(getString(R.string.error_field_required));
            focusView = mPhoneEditText;
            cancel = true;
        } else if (!Utils.isPhoneValid(phone)) {
            mPhoneEditText.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (!Utils.isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            HashMap<String, Object> params = new HashMap<>();
            params.put("userid", phone);
            params.put("password", password);
            params.put("udid", Utils.getUDID(mContext));
            NetData netData = new NetData(NetData.ProtocolType.USER_SIGNIN, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
            NetManager netManager = new NetManager(netData, this);
            netManager.setCallback(mNetManagerSignInCallback);
            netManager.execute((Void) null);

        }
    }

    private NetManager.Callbacks mNetManagerSignInCallback = new NetManager.Callbacks() {
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

                    JSONObject user = jsonObject.getJSONObject("user");
                    CommonData.LoginUserData.id = user.getInt("id");//회원 인덱스
                    CommonData.LoginUserData.userId = user.getString("userid");//아이디(전화번호)
                    CommonData.LoginUserData.userName = user.getString("username");//닉네임
                    CommonData.LoginUserData.gender = user.getString("gender");//성별
                    CommonData.LoginUserData.birthday = user.getString("birthday");//생일
                    CommonData.LoginUserData.address = user.getString("address");//지역
                    CommonData.LoginUserData.comment = user.getString("comment");//자기소개
                    CommonData.LoginUserData.instagramId = user.getString("instagram");//인스타 numeric 아이디
                    CommonData.LoginUserData.instagramName = user.getString("instagram_name");//인스타 아이디
                    CommonData.LoginUserData.profileImgUrl = user.getString("profile_img_url");//인스
                    // TODO: 2016-10-28 서버 수정됨 - 로그인시에는 dcode_id 리턴하지 않음
                    if (user.has("dcode_id")) {
                        CommonData.LoginUserData.dcodeId = user.getInt("dcode_id");//D-Code 인덱스
                    }
                    CommonData.LoginUserData.dMoney = user.getInt("dmoney");//D-Money잔액
                    CommonData.LoginUserData.level = user.getString("level");//레벨(guest,h
                    CommonData.LoginUserData.barcode = user.getString("barcode");//D-Money
                    CommonData.LoginUserData.cDate = user.getString("cdate");//가입 시각
                    CommonData.LoginUserData.instaToken = jsonObject.getString("server_instagram_access_token");

                    // Store phone and password in preference
                    SharedPreferences prefs = getSharedPreferences(PREFERENCE_LOGIN, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFERENCE_LOGIN_ID, mPhoneEditText.getText().toString());
                    editor.putString(PREFERENCE_LOGIN_PASSWORD, mPasswordEditText.getText().toString());
                    //editor.putString(PREFERENCE_LOGIN_TOKEN, jsonObject.getString("session_token"));
                    editor.commit();

                    Intent intent = new Intent(AccountLoginActivity.this, HomeTabActivity.class);
                    startActivity(intent);
                    finish();

                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와 연결하지 못했습니다.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 2) {
                    Toast.makeText(mContext, "아이디 혹은 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "일시적인 오류입니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(mContext, "일시적인 오류입니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    };

}

