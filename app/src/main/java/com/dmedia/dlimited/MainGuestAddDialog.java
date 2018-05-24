package com.dmedia.dlimited;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.util.ArrayList;

/**
 * Created by xema0 on 2016-10-04.
 */
// TODO: 2016-10-04 Host도 spinner에 추가시키는 건가?
public class MainGuestAddDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "MainGuestAddDialog";

    private EditText mNameEditText;
    private AppCompatSpinner mClassSpinner;
    private EditText mInstaEditText;
    private Button mAddButton;


    //부모로 데이터 전송 위한 인터페이스
    public interface ICustomDialogEventListener {
        public void customDialogEvent(String name, String className, String instaID);
    }

    private ICustomDialogEventListener onCustomDialogEventListener;

    protected MainGuestAddDialog(@NonNull Context context, ICustomDialogEventListener onCustomDialogEventListener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.onCustomDialogEventListener = onCustomDialogEventListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_main_guest);

        mNameEditText = (EditText) findViewById(R.id.et_name);
        mClassSpinner = (AppCompatSpinner) findViewById(R.id.sp_class);
        mInstaEditText = (EditText) findViewById(R.id.et_insta_id);
        mAddButton = (Button) findViewById(R.id.btn_add);

        mAddButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO: 2016-10-04 main guest 추가 버튼을 누르면 부모 프래그먼트의 리사이클러 뷰에 데이터가 추가되나? - 물어보기
        // TODO: 2016-10-04 아니면 서버에 값을 올리고 다이얼로그가 없어질때 통신을 한번 더하는지 물어보기
        String name = mNameEditText.getText().toString();
        String instaID = mInstaEditText.getText().toString();
        String className = (String) mClassSpinner.getSelectedItem();

        // TODO: 2016-10-04 instaID를 리사이클러뷰에 추가시켜야 하나?
        if (Utils.isNameValid(name) && Utils.isInstaIdValid(instaID)) {
            onCustomDialogEventListener.customDialogEvent(name, className, instaID);
            dismiss();
        } else {
            // TODO: 2016-10-04 입력한 값이 유효하지 않을때 에러창 띄우기 - toast 나 snackbar 로?
            Toast.makeText(getContext(), "이름 혹은 인스타그램 아이디를 확인해주세요", Toast.LENGTH_SHORT).show();
        }
        // TODO: 2016-10-04 넷매니저 -> 서버에 값 통신해서 올리기
    }
}
