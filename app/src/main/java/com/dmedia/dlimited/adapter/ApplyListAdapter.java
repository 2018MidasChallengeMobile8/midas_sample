package com.dmedia.dlimited.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.ApplyListData;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.network.InstagramApp;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.activity.MemberViewActivity;
import com.dmedia.dlimited.widget.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * HOME컨텐츠, DBOX컨텐츠의 '승인' 탭, 신청 대기 리스트의 어댑터 (일반 유저)
 */
public class ApplyListAdapter extends RecyclerView.Adapter<ApplyListAdapter.ListItemViewHolder> {
    private static final String TAG = "ApplyListAdapter";
    private Context mContext = null;
    private ArrayList<ApplyListData> mListData;
    public static final String MODE_PERMIT = "mode_permit";
    public static final String MODE_DELETE = "mode_delete";
    private int mode = 0; //0 : permit   1 : delete

    public static final int ACTIVITY_MODE_DBOX = 101;
    public static final int ACTIVITY_MODE_EVENT = 201;
    private int activityMode = 101;

    private int visibleThreshold = 1;
    private int lastVisibleItem;
    private int totalItemCount;
    private int current_page = 0;

    private boolean loading;
    private boolean loadListenerBlock = false;
    private ApplyListAdapter.OnLoadMoreListener onLoadMoreListener;

    private int gonePosition = 0;

    private InstagramApp mApp;

    //부모로 데이터 전송 위한 인터페이스
    public interface ICustomDeleteListener {
        public void delete();
    }

    private ICustomDeleteListener onCustomDeleteListener;

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


    public void setActivityMode(int mode) {
        activityMode = mode;
    }

    public void setMode(String s) {
        if (s.equals(MODE_DELETE)) {
            mode = 1;
        } else if (s.equals(MODE_PERMIT)) {
            mode = 0;
        }
    }

    public boolean isLoadListenerBlock() {
        return loadListenerBlock;
    }

    public void setLoaded() {
        loading = false;
    }

    public void setCurrentPageToZero() {
        current_page = 0;
    }

    public void setOnLoadMoreListener(ApplyListAdapter.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public void setLoadListenerBlock(boolean loadListenerBlock) {
        this.loadListenerBlock = loadListenerBlock;
    }

    public void setOnCustomDeleteListener(ICustomDeleteListener deleteListener) {
        this.onCustomDeleteListener = deleteListener;
    }

    public ApplyListAdapter(Context mContext, ArrayList<ApplyListData> listData, RecyclerView recyclerView) {
        super();
        this.mContext = mContext;
        this.mListData = listData;

        mApp = new InstagramApp(mContext, mContext.getString(R.string.insta_client_id), mContext.getString(R.string.insta_client_secret), mContext.getString(R.string.insta_redirect_url));
        mApp.setListener(instagramListener);

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (!loadListenerBlock) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            // End has been reached
                            current_page++;
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore(current_page);
                            }
                            loading = true;
                        }
                    }
                }
            });
        }
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_apply_list_item, parent, false);
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, final int position) {
        final ApplyListData mData = mListData.get(position);

        /*
        //인스타 numeric id로 인스타 username 얻기
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("access_token", CommonData.LoginUserData.instaToken);
        client.get(mContext, "https://api.instagram.com/v1/users/" + mData.getInstagram() + "/", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    holder.tvInsta.setText("@" + data.getString("username"));
                } catch (JSONException e) {
                    Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    if (errorResponse.getJSONObject("meta").getString("error_type").equals("OAuthAccessTokenException")) {
                        //Toast.makeText(mContext, "인스타그램 토큰이 만료되었습니다.\n다시 로그인해주세요", Toast.LENGTH_SHORT).show();
                        mApp.authorize();
                    } else {
                        mApp.authorize();
                        Log.d("res",errorResponse.toString());
                        //Toast.makeText(mContext, errorResponse+"", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Toast.makeText(mContext, "일시적인 오류가 발생했습니다.\n잠시후 다시 시도해세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

        holder.tvInsta.setText("@" + mData.getInstagramName());

        //승인
        if (mode == 0) {
            holder.ivDelete.setVisibility(ImageView.GONE);
            holder.llPermit.setVisibility(Button.VISIBLE);
            holder.llPermit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("승인하기");
                    builder.setMessage("해당 신청자의 참가 요청을\n승인하시겠습니까?");
                    builder.setPositiveButton("승인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activityMode == ACTIVITY_MODE_EVENT) {
                                gonePosition = position;
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("event_user_id", mData.getId());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_APPLY, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventUserApplyCallback);
                                netManager.execute((Void) null);
                            } else if (activityMode == ACTIVITY_MODE_DBOX) {
                                gonePosition = position;
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("dbox_id", mData.getEventId());
                                params.put("userid", mData.getPhoneNumber());
                                //params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.DBOX_GUEST_APPROVE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventUserApplyCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });
                    builder.show();
                }
            });
        }
        //제거
        else {
            holder.llPermit.setVisibility(Button.GONE);
            if (mData.getStatus().equals("reviewed") || mData.getStatus().equals("rewarded")) {
                holder.ivDelete.setVisibility(ImageView.GONE);
            } else {
                holder.ivDelete.setVisibility(ImageView.VISIBLE);
            }
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("삭제하기");
                    builder.setMessage("해당 신청자의 참가 요청을\n삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activityMode == ACTIVITY_MODE_EVENT) {
                                gonePosition = position;
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("event_id", mData.getEventId());
                                params.put("event_userid", mData.getPhoneNumber());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.EVENT_USER_REMOVE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventUserRemoveCallback);
                                netManager.execute((Void) null);
                            } else if (activityMode == ACTIVITY_MODE_DBOX) {
                                gonePosition = position;
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("dbox_id", mData.getEventId());
                                params.put("dbox_userid", mData.getPhoneNumber());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.DBOX_USER_REMOVE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventUserRemoveCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });
                    builder.show();
                }
            });
        }

        if ((mData.getProfileUrl() == null) || (mData.getProfileUrl().equals("")) || (mData.getProfileUrl().equals("none")) || (mData.getProfileUrl().equals("null"))) {
            holder.rivIcon.setImageResource(R.drawable.profile_male);
        } else {
            Glide.with(mContext).load(mData.getProfileUrl()).into(holder.rivIcon);
        }

        holder.tvName.setText(mData.getUserName());

        //if (activityMode == ACTIVITY_MODE_EVENT) {
        //    holder.tvInsta.setText("@" + mData.getInstagram());
        //}
        //holder.tvInsta.setText("@" + mData.getInstagram());

        //이벤트일때만 액티비티 생성해서 코멘트 보여야하므로 리스너 부착
        if (activityMode == ACTIVITY_MODE_EVENT) {
            holder.rlListItemArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                    String url = "https://www.instagram.com/" + mData.getInstagramName();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mContext.startActivity(intent);
                    */

                    // TODO: 2016-11-29 인스타그램 permission public-content 승인 되면 바꾸기.
                    Intent intent = new Intent(mContext, MemberViewActivity.class);
                    intent.putExtra("event_id", mData.getEventId());
                    intent.putExtra("user_phone", mData.getPhoneNumber());
                    intent.putExtra("insta_id", mData.getInstagram());
                    mContext.startActivity(intent);

                }
            });
        }
        holder.mContext = mContext;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView rivIcon;
        TextView tvName;
        TextView tvInsta;
        //Button btnPermit;
        LinearLayout llPermit;
        ImageView ivDelete;

        RelativeLayout rlListItemArea;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            rivIcon = (RoundedImageView) itemView.findViewById(R.id.riv_icon);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvInsta = (TextView) itemView.findViewById(R.id.tv_insta);
            //btnPermit = (Button) itemView.findViewById(R.id.btn_permit);
            llPermit = (LinearLayout) itemView.findViewById(R.id.ll_permit);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            rlListItemArea = (RelativeLayout) itemView.findViewById(R.id.rl_list_item_area);
        }
    }

    private NetManager.Callbacks mNetManagerEventUserApplyCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    // TODO: 2016-11-07 재통신 할 필요 x - 페이지 단위의 통신이라 여러 페이지를 다시 로딩하는것 보단 나을듯
                    mListData.remove(gonePosition);
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "승인되었습니다.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private NetManager.Callbacks mNetManagerEventUserRemoveCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    onCustomDeleteListener.delete();
                    mListData.remove(gonePosition);
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}