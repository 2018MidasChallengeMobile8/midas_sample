package com.dmedia.dlimited.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dmedia.dlimited.model.CommonData;
import com.dmedia.dlimited.model.GroupMemberData;
import com.dmedia.dlimited.network.NetData;
import com.dmedia.dlimited.network.NetManager;
import com.dmedia.dlimited.R;
import com.dmedia.dlimited.widget.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jeonghoy on 2016. 6. 16..
 */
/*
 * HOME컨텐츠, DBOX컨텐츠의 '구성' 탭 어댑터 (메인 게스트)
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ListItemViewHolder> {
    private static final String TAG = "GroupMemberAdapter";
    private Context mContext = null;
    private ArrayList<GroupMemberData> mListData;

    public static final String MODE_NORMAL = "mode_normal";
    public static final String MODE_DELETE = "mode_delete";
    private int mode = 0; //0 : normal  1 : delete

    public static final int ACTIVITY_MODE_DBOX = 101;
    public static final int ACTIVITY_MODE_EVENT = 201;
    private int activityMode = 101;

    private int eventId;


    public void setActivityMode(int mode) {
        activityMode = mode;
    }

    public void setMode(String s) {
        if (s.equals(MODE_DELETE)) {
            mode = 1;
        } else if (s.equals(MODE_NORMAL)) {
            mode = 0;
        }
    }

    public GroupMemberAdapter(Context mContext, ArrayList<GroupMemberData> listData, RecyclerView recyclerView) {
        super();
        this.mContext = mContext;
        this.mListData = listData;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_people_list_item, parent, false);
        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {
        final GroupMemberData mData = mListData.get(position);
        eventId = mData.getEventId();
        if (mode == 0) {
            //delete버튼 없는모드(호스트 아닐때)
            holder.ivDelete.setVisibility(ImageView.GONE);
        } else if (mode == 1) {
            //delete모드
            holder.ivDelete.setVisibility(ImageView.VISIBLE);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("삭제 확인");
                    builder.setMessage("삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activityMode == ACTIVITY_MODE_EVENT) {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("event_id", mData.getEventId());
                                params.put("event_model_user_id", mData.getId());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.EVENT_GUEST_REMOVE, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventGuestRemoveCallback);
                                netManager.execute((Void) null);
                            } else if (activityMode == ACTIVITY_MODE_DBOX) {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("dbox_id", mData.getEventId());
                                params.put("dbox_model_user_id", mData.getId());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.DBOX_MODEL_REMOVE, NetData.MethodType.POST, NetData.ProgressType.NONE, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerDBoxModelRemoveCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            });
        }
        if ((mData.getProfileImgUrl() == null) || (mData.getProfileImgUrl().equals("")) || (mData.getProfileImgUrl().equals("none")) || (mData.getProfileImgUrl().equals("null"))) {
            // TODO: 2016-10-30 이미지url이 없을경우 나오는 이미지설정
            holder.rivIcon.setImageResource(R.drawable.profile_male);
        } else {
            Glide.with(mContext).load(mData.getProfileImgUrl()).into(holder.rivIcon);
        }
        holder.tvName.setText(mData.getUsername());
        String level = mData.getLevel();
        if (level.equals("celeb")) {
            level = "celebrity";
        }
        holder.tvLevel.setText(level);
        /*
        holder.rlListItemArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        */
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
        TextView tvLevel;
        ImageView ivDelete;

        RelativeLayout rlListItemArea;

        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            rivIcon = (RoundedImageView) itemView.findViewById(R.id.riv_icon);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvLevel = (TextView) itemView.findViewById(R.id.tv_role);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            rlListItemArea = (RelativeLayout) itemView.findViewById(R.id.rl_list_item_area);
        }
    }

    private NetManager.Callbacks mNetManagerEventGuestRemoveCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    updateEventModelList();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private NetManager.Callbacks mNetManagerDBoxModelRemoveCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    updateDBoxModelList();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public void updateEventModelList() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("userid", CommonData.LoginUserData.userId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.EVENT_DETAIL, NetData.MethodType.GET, NetData.ProgressType.MESSAGE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerEventDetailCallback);
        netManager.execute((Void) null);
    }

    public void updateDBoxModelList() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("dbox_id", eventId);
        params.put("session_token", CommonData.LoginUserData.loginToken);
        NetData netData = new NetData(NetData.ProtocolType.DBOX_DETAIL, NetData.MethodType.GET, NetData.ProgressType.MESSAGE, params);
        NetManager netManager = new NetManager(netData, mContext);
        netManager.setCallback(mNetManagerDBoxDetailCallback);
        netManager.execute((Void) null);
    }

    private NetManager.Callbacks mNetManagerEventDetailCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");
                if (resultCode == 1) {
                    mListData.clear();
                    //메인게스트?
                    JSONArray eventModelUserArray = jsonObject.getJSONArray("event_model_user_list");
                    {
                        for (int i = 0; i < eventModelUserArray.length(); i++) {
                            JSONObject object = eventModelUserArray.getJSONObject(i);
                            int id = object.getInt("id");
                            int eventId = object.getInt("event_id");
                            String username = object.getString("username");
                            String level = object.getString("level");
                            String instagram = object.getString("instagram");
                            String profileImgUrl = object.getString("profile_img_url");

                            mListData.add(new GroupMemberData(id, eventId, username, level, instagram, profileImgUrl));
                        }
                    }
                    notifyDataSetChanged();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
                    mListData.clear();
                    //메인게스트?
                    JSONArray dboxModelUserArray = jsonObject.getJSONArray("dbox_model_user_list");
                    for (int i = 0; i < dboxModelUserArray.length(); i++) {
                        JSONObject object = dboxModelUserArray.getJSONObject(i);
                        int id = object.getInt("id");
                        int dboxId = object.getInt("dbox_id");
                        String username = object.getString("username");
                        String level = object.getString("level");
                        String instagram = object.getString("instagram");
                        String profileImgUrl = object.getString("profile_img_url");
                        mListData.add(new GroupMemberData(id, dboxId, username, level, instagram, profileImgUrl));
                    }
                    notifyDataSetChanged();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}