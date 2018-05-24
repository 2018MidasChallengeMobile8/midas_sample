package com.dmedia.dlimited.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.adapter.GroupMemberAdapter;
import com.dmedia.dlimited.model.GroupMemberData;
import com.dmedia.dlimited.network.InstagramApp;
import com.dmedia.dlimited.dialog.MainGuestAddDialog;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

//Event 상세 - 구성 탭
public class ProductListDetailTabGroupFragment extends Fragment implements View.OnClickListener {
    Context mContext;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupMemberAdapter mAdapter;
    private ArrayList<GroupMemberData> mDataList;

    private LinearLayout mAddMainGuestLinearLayout;

    private int dboxId;
    private String mName = "";
    private String mClassName = "";
    private String mInstaID = "";

    private InstagramApp mApp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list_detail_tab_group, null);

        mDataList = new ArrayList<>();
        if (getArguments() != null) {
            Bundle b = getArguments();
            mDataList = b.getParcelableArrayList("group_model_list");
            dboxId = b.getInt("info_id");
        }

        mApp = new InstagramApp(mContext, getString(R.string.insta_client_id), getString(R.string.insta_client_secret), getString(R.string.insta_redirect_url));
        mApp.setListener(instagramListener);

        mAddMainGuestLinearLayout = (LinearLayout) view.findViewById(R.id.ll_main_guest);
        mAddMainGuestLinearLayout.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new GroupMemberAdapter(mContext, mDataList, mRecyclerView);
        mAdapter.setMode(GroupMemberAdapter.MODE_DELETE);
        mAdapter.setActivityMode(GroupMemberAdapter.ACTIVITY_MODE_DBOX);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public void showDialog() {
        Dialog dialog = new MainGuestAddDialog(mContext, new MainGuestAddDialog.ICustomDialogEventListener() {
            @Override
            public void customDialogEvent(String name, String className, String instaID) {
                /*
                HashMap<String, Object> params = new HashMap<>();
                params.put("dbox_id", dboxId);
                params.put("username", name);
                if (className.equals("Celebrity") || className.equals("celebrity")) {
                    className = "celeb";
                }
                params.put("level", className);
                params.put("instagram_name", instaID);
                params.put("session_token", CommonData.LoginUserData.loginToken);
                NetData netData = new NetData(NetData.ProtocolType.DBOX_MODEL_ADD, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                NetManager netManager = new NetManager(netData, mContext);
                netManager.setCallback(mNetManagerDBoxModelAddCallback);
                netManager.execute((Void) null);
                */

                //Toast.makeText(mContext, "Main guest 추가 기능을 사용할 수 없습니다.\n고객센터에 문의해주세요", Toast.LENGTH_LONG).show();

                mName = name;
                mClassName = className;
                mInstaID = instaID;

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("q", instaID);
                //params.put("access_token", new InstagramSession(mContext).getAccessToken());
                params.put("access_token", CommonData.LoginUserData.instaToken);
                //params.put("access_token", mApp.getAccessToken());
                params.put("count", 1);
                client.get(mContext, "https://api.instagram.com/v1/users/search", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            if (response.getJSONArray("data").length() == 0) {
                                Toast.makeText(mContext, "해당 인스타그램 유저가 없습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("dbox_id", dboxId);
                                params.put("username", mName);
                                if (mClassName.equals("Celebrity") || mClassName.equals("celebrity")) {
                                    mClassName = "celeb";
                                }
                                //이벤트와 다르게 서버에서 소문자만 받음 -> 전달
                                mClassName = mClassName.toLowerCase();
                                Toast.makeText(mContext, mClassName, Toast.LENGTH_SHORT).show();
                                params.put("level", mClassName);
                                JSONObject jsonObject = response.getJSONArray("data").getJSONObject(0);
                                params.put("instagram", jsonObject.getString("id"));
                                params.put("img_url", jsonObject.getString("profile_picture"));
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.DBOX_MODEL_ADD, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerDBoxModelAddCallback);
                                netManager.execute((Void) null);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        /*
                        try {
                            if (errorResponse.getJSONObject("meta").getString("error_type").equals("OAuthAccessTokenException")) {
                                Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                                mApp.authorize();
                            } else {
                                Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                                mApp.authorize();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        */
                        Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setTitle(null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    InstagramApp.OAuthAuthenticationListener instagramListener = new InstagramApp.OAuthAuthenticationListener() {

        @Override
        public void onSuccess() {
            Toast.makeText(mContext, "연결되었습니다", Toast.LENGTH_SHORT).show();
            CommonData.LoginUserData.instaToken = mApp.getAccessToken();
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(mContext, "인스타그램 서버와 접속에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    };


    //main guest 추가버튼 리스너
    @Override
    public void onClick(View v) {
        showDialog();
    }

    private NetManager.Callbacks mNetManagerDBoxModelAddCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                int resultCode = jsonObject.getInt("result");
                Log.d("callback : ", jsonObject.toString());
                if (resultCode == 1) {
                    updateList();
                    Toast.makeText(mContext, "추가되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void updateList() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("dbox_id", dboxId);
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_DETAIL, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxDetailCallback);
        netManager.execute((Void) null);
    }

    private NetManager.Callbacks mNetManagerDBoxDetailCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    mDataList.clear();
                    JSONArray dboxModelUserArray = jsonObject.getJSONArray("dbox_model_user_list");
                    {
                        for (int i = 0; i < dboxModelUserArray.length(); i++) {
                            JSONObject object = dboxModelUserArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int dboxId = object.getInt("dbox_id");
                            String username = object.getString("username");
                            String level = object.getString("level");
                            String instagram = object.getString("instagram");
                            String profileImgUrl = object.getString("profile_img_url");

                            mDataList.add(new GroupMemberData(id, dboxId, username, level, instagram, profileImgUrl));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
