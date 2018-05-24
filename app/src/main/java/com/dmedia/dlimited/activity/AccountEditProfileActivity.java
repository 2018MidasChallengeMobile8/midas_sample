package com.dmedia.dlimited.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.util.Utils;

import org.chenglei.widget.datepicker.DatePicker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by xema0 on 2016-10-20.
 */
// TODO: 2016-10-31 인스타그램 연동 버튼 구현
// TODO: 2016-10-31 폰번호입력하는 edittext는 왜있는거지 -> 이것도 바꾸는건가 아니면 그냥 인증용인가
// TODO: 2016-10-31 internal server error
public class AccountEditProfileActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;
    private EditText mNicknameEditText;
    private EditText mPhoneEditText;
    //private Button mInstagramButton;
    private EditText mCommentEditText;
    private Button mBirthButton;
    private Button mGenderButton;
    private Button mModifyButton;

    private int year, month, day;
    private String nickname = "";
    private String phone = "";
    private String comment = "";
    private String gender = "";
    private String birth = "";
    private String instaId = "";
    private String profileUrl = "";
    private String instaName = "";
    private boolean genderCheck = false;
    private boolean birthCheck = false;

    //private InstagramApp mApp;
    private boolean instaAuthCheck = false;

    private DatePicker mBirthDatePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit_profile);

        mContext = this;
        //mApp = new InstagramApp(this, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        //mApp.setListener(instagramListener);

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        ActionBar a = getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);
        a.setHomeAsUpIndicator(R.drawable.p08_bt_back);
        a.setTitle("프로필 수정");

        mNicknameEditText = (EditText) findViewById(R.id.edt_nickname);
        mPhoneEditText = (EditText) findViewById(R.id.edt_phone);
        //mInstagramButton = (Button) findViewById(R.id.btn_insta);
        mCommentEditText = (EditText) findViewById(R.id.edt_comment);
        mGenderButton = (Button) findViewById(R.id.btn_gender);
        mBirthButton = (Button) findViewById(R.id.btn_birth);
        mModifyButton = (Button) findViewById(R.id.btn_modify);

        mPhoneEditText.setEnabled(false);

        String birth = CommonData.LoginUserData.birthday;
        String gender = CommonData.LoginUserData.gender;
        if (birth != null) {
            mBirthButton.setText(birth);
            birthCheck = true;
        }
        if (gender != null) {
            if (gender.equals("male")) {
                this.gender = gender;
                mGenderButton.setText("남성");
                genderCheck = true;
            } else if (gender.equals("female")) {
                this.gender = gender;
                mGenderButton.setText("여성");
                genderCheck = true;
            }
        }

        mNicknameEditText.setText(CommonData.LoginUserData.userName);
        mPhoneEditText.setText(CommonData.LoginUserData.userId);
        if (CommonData.LoginUserData.comment != null && !CommonData.LoginUserData.comment.equals("null"))
            mCommentEditText.setText(CommonData.LoginUserData.comment);

        //mInstagramButton.setOnClickListener(this);
        mBirthButton.setOnClickListener(this);
        mGenderButton.setOnClickListener(this);
        mModifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //case R.id.btn_insta:
            //    mApp.authorize();
            //    break;
            case R.id.btn_birth:
                makeBirthDialog();
                break;
            case R.id.btn_gender:
                makeGenderDialog();
                break;
            case R.id.btn_modify:
                nickname = mNicknameEditText.getText().toString();
                phone = mPhoneEditText.getText().toString();
                comment = mCommentEditText.getText().toString();
                if (Utils.isNicknameValid(nickname)) {
                    if (genderCheck) {
                        if (birthCheck) {
                            if (Utils.isPhoneValid(phone)) {
                                //if (instaAuthCheck) {
                                //모든 형식 일치
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("userid", phone);
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                params.put("username", nickname);
                                params.put("gender", gender);
                                params.put("birthday", birth);
                                // TODO: 2016-10-31 지역 관련한것이 기획에서 빠져있음!
                                params.put("address", CommonData.LoginUserData.address);

                                // TODO: 2017-01-16 인스타그램 관련 수정 - 서버에서 optional 하게 바뀌면 예외 처리
                                instaId = CommonData.LoginUserData.instagramId;
                                instaName = CommonData.LoginUserData.instagramName;
                                profileUrl = CommonData.LoginUserData.profileImgUrl;
                                params.put("instagram", instaId);
                                params.put("instagram_name", instaName);
                                params.put("profile_img_url", profileUrl);

                                params.put("comment", comment);
                                NetData netData = new NetData(NetData.ProtocolType.USER_UPDATE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerUserUpdateCallback);
                                netManager.execute((Void) null);
                                //} else {
                                //    Toast.makeText(mContext, "인스타그램 인증이 필요합니다", Toast.LENGTH_SHORT).show();
                                //}
                            } else {
                                //폰 형식 불일치
                                Toast.makeText(this, "유효하지 않은 휴대폰 번호입니다.", Toast.LENGTH_SHORT).show();
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
                int monthOfYear = mBirthDatePicker.getMonth() - 1;
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

    /*
    private void makeBirthDialog() {
        DatePickerDialog d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
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

    /*
    InstagramApp.OAuthAuthenticationListener instagramListener = new InstagramApp.OAuthAuthenticationListener() {

        @Override
        public void onSuccess() {
            mInstagramButton.setText(mApp.getUserName() + "으로 연결되었습니다");
            instaName = mApp.getUserName();
            instaId = mApp.getId();
            profileUrl = mApp.getProfileUrl();
            mInstagramButton.setOnClickListener(null);//비활성화
            //mApp.resetAccessToken();//인스타그램 세션 초기화
            instaAuthCheck = true;
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(AccountEditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };
    */

    private NetManager.Callbacks mNetManagerUserUpdateCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    //서버 수정됨 : user 리턴하지 않음
                    /*
                    JSONObject user = jsonObject.getJSONObject("user");
                    CommonData.LoginUserData.id = user.getInt("id");//회원 인덱스
                    CommonData.LoginUserData.userId = user.getString("userid");//아이디(전화번호)
                    CommonData.LoginUserData.userName = user.getString("username");//닉네임
                    CommonData.LoginUserData.gender = user.getString("gender");//성별
                    CommonData.LoginUserData.birthday = user.getString("birthday");//생일
                    CommonData.LoginUserData.address = user.getString("address");//지역
                    CommonData.LoginUserData.comment = user.getString("comment");//자기소개
                    CommonData.LoginUserData.instagramId = user.getString("instagram");//인스타
                    CommonData.LoginUserData.profileImgUrl = user.getString("profile_img_url");//인스
                    if (user.has("dcode_id")) {
                        CommonData.LoginUserData.dcodeId = user.getInt("dcode_id");//D-Code 인덱스
                    }
                    CommonData.LoginUserData.dMoney = user.getInt("dmoney");//D-Money잔액
                    CommonData.LoginUserData.level = user.getString("level");//레벨(guest,h
                    CommonData.LoginUserData.barcode = user.getString("barcode");//D-Money
                    CommonData.LoginUserData.cDate = user.getString("cdate");//가입 시각
                    */
                    CommonData.LoginUserData.userName = nickname;
                    CommonData.LoginUserData.comment = comment;
                    CommonData.LoginUserData.birthday = birth;
                    CommonData.LoginUserData.gender = gender;
                    CommonData.LoginUserData.instagramId = instaId;
                    CommonData.LoginUserData.instagramName = instaName;
                    CommonData.LoginUserData.profileImgUrl = profileUrl;

                    Toast.makeText(mContext, "회원정보를 수정했습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccountEditProfileActivity.this, MyPageTabActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "회원정보 수정을 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 2) {
                    Toast.makeText(mContext, "존재하지 않는 회원입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "회원정보 수정을 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
