package com.dmedia.dlimited;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dmedia.dlimited.DraggableRecyclerView.CallbackItemTouch;
import com.dmedia.dlimited.DraggableRecyclerView.MyItemTouchHelperCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN;
import static com.dmedia.dlimited.AccountLoginActivity.PREFERENCE_LOGIN_UDID;

//Event 상세 - 정보 탭
public class EventListDetailTabInformationFragment extends Fragment implements CallbackItemTouch {
    Context mContext;
    private TextView mInformationTextView;
    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private int id;
    private int placeId;
    private int viewCnt;
    private int deleted;
    private String title = "";
    private String information = "";
    private String requirement = "";
    private String startDate = "";
    private String status = "";
    private String cDate = "";

    private ArrayList<SubImageData> mSubImageDataList;
    private SubImageAdapter mSubImageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list_detail_tab_information, null);

        if (getArguments() != null) {
            Bundle b = getArguments();
            id = b.getInt("info_id");
            placeId = b.getInt("info_place_id");
            //privateMode=b.getInt("info_private_mode");
            viewCnt = b.getInt("info_view_cnt");
            deleted = b.getInt("info_deleted");
            title = b.getString("info_title");
            information = b.getString("info_info");
            requirement = b.getString("info_requirement");
            startDate = b.getString("info_start_date");
            status = b.getString("info_status");
            cDate = b.getString("info_cdate");

            mSubImageDataList = b.getParcelableArrayList("sub_image_data");
        } else {
            mSubImageDataList = new ArrayList<>();
        }
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mInformationTextView = (TextView) view.findViewById(R.id.tv_information);
        mDateTextView = (TextView) view.findViewById(R.id.tv_date);
        mTimeTextView = (TextView) view.findViewById(R.id.tv_time);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mSubImageAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mSubImageAdapter = new SubImageAdapter(mContext, mSubImageDataList, mRecyclerView, SubImageAdapter.MODE_DEFAULT);
        mRecyclerView.setAdapter(mSubImageAdapter);


        mTitleTextView.setText(title);
        mInformationTextView.setText(information);
        mDateTextView.setText(startDate.substring(0, 10));
        mTimeTextView.setText(startDate.substring(11, 16));

        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(this);// create MyItemTouchHelperCallback
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback); // Create ItemTouchHelper and pass with parameter the MyItemTouchHelperCallback
        touchHelper.attachToRecyclerView(mRecyclerView); // Attach ItemTouchHelper to RecyclerView

        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("event_id", 40);
                params.put("userid", CommonData.LoginUserData.userId);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("img_id_list", Integer.toString(118)));
                nameValuePairs.add(new BasicNameValuePair("img_id_list", Integer.toString(108)));
                nameValuePairs.add(new BasicNameValuePair("img_id_list", Integer.toString(117)));

                params.put("img_id_list", nameValuePairs);
                params.put("session_token", CommonData.LoginUserData.loginToken);

                AsyncHttpClient client = new AsyncHttpClient();
                client.post(mContext, "http://www.dlimited.co.kr/api/event/image/sort", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Toast.makeText(mContext, response + "", Toast.LENGTH_SHORT).show();
                        Log.d("response", response.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(mContext, errorResponse + "", Toast.LENGTH_SHORT).show();
                        Log.d("erro response", errorResponse.toString());
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        Collections.swap(mSubImageDataList, oldPosition, newPosition); // change position
        mSubImageAdapter.notifyItemMoved(oldPosition, newPosition); //notifies changes in adapter, in this case use the notifyItemMoved

        //param.add 쓸경우 순서가 랜덤으로 배치되는 이슈 -> 강제로 url 지정해서 하기로
        /*
        RequestParams params = new RequestParams();
        params.put("event_id", id);
        params.put("userid", CommonData.LoginUserData.userId);

        for (int i = 0; i < mSubImageDataList.size(); i++) {
            Log.d(i + "", mSubImageDataList.get(i).getId() + "");
            params.add("img_id_list", mSubImageDataList.get(i).getId() + "");
        }

        params.put("session_token", CommonData.LoginUserData.loginToken);
        */

        // TODO: 2016-11-23 응답이 엇갈려서 올경우....타이밍 문제. splash띄워서 하든가 해서 해결하기
        String paramStr = "?" + "event_id=" + id + "&userid=" + CommonData.LoginUserData.userId + "&session_token=" + CommonData.LoginUserData.loginToken;
        String imageParamStr = "";
        for (int i = 0; i < mSubImageDataList.size(); i++) {
            imageParamStr += "&img_id_list=" + mSubImageDataList.get(i).getId();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mContext, "http://www.dlimited.co.kr/api/event/image/sort" + paramStr + imageParamStr, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("response", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("error response", errorResponse.toString());
            }
        });


    }

    private NetManager.Callbacks mNetManagerEventImageSortCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback", jsonObject.toString());
                if (jsonObject.getInt("result") == 1) {

                } else {
                    Toast.makeText(mContext, "서버와의 통신중 에러가 발생했습니다.\n잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
