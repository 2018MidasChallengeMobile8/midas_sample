package com.dmedia.dlimited;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by min on 2016-09-23.
 */
public class SubImageAdapter extends RecyclerView.Adapter<SubImageAdapter.ListItemViewHolder> {
    private Context mContext = null;
    private ArrayList<SubImageData> mListData;

    private int mPosition;

    public static final int MODE_MODIFY = 300;
    public static final int MODE_DEFAULT = 400;
    public static final int MODE_CHANGE = 500;
    private int mode;

    public SubImageAdapter(Context mContext, ArrayList<SubImageData> listData, RecyclerView recyclerView, int mode) {
        super();
        this.mContext = mContext;
        this.mListData = listData;
        this.mode = mode;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_sub_image_list_item, parent, false);

        return new ListItemViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, final int position) {
        final SubImageData mData = mListData.get(position);
        holder.mContext = mContext;


        if (mode == MODE_MODIFY) {
            holder.rlMenu.setVisibility(RelativeLayout.GONE);
        } else {
            holder.rlMenu.setVisibility(RelativeLayout.VISIBLE);
        }


        if (mData.isFile()) {
            File imgFile = new File(mData.getFilePath());
            if (imgFile.exists()) {
                Bitmap myBitmap = Utils.decodeSampledBitmapFromResource(imgFile.getAbsolutePath());
                holder.ivImage.setImageBitmap(myBitmap);
            }
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListData.remove(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            if (mode == MODE_MODIFY) {
                //url 로 받아온 이미지들은 수정 창에서는 삭제 못하도록
                holder.ivDelete.setVisibility(ImageView.GONE);
            } else {
                holder.ivDelete.setVisibility(ImageView.VISIBLE);
            }
            Glide.with(mContext).load(mData.getImageUrl()).into(holder.ivImage);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPosition = position;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle("삭제 확인");
                    dialog.setMessage("이미지를 삭제하시겠습니까?");
                    dialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mData.isEvent()) {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("event_id", mData.getEventOrDBoxId());
                                params.put("userid", CommonData.LoginUserData.userId);
                                params.put("event_img_id", mData.getId());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.EVENT_IMAGE_REMOVE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventImageRemoveCallback);
                                netManager.execute((Void) null);
                            }
                            else {
                                HashMap<String, Object> params = new HashMap<>();
                                params.put("dbox_id", mData.getEventOrDBoxId());
                                params.put("userid", CommonData.LoginUserData.userId);
                                params.put("dbox_img_id", mData.getId());
                                params.put("session_token", CommonData.LoginUserData.loginToken);
                                NetData netData = new NetData(NetData.ProtocolType.DBOX_IMAGE_REMOVE, NetData.MethodType.POST, NetData.ProgressType.SPLASH, params);
                                NetManager netManager = new NetManager(netData, mContext);
                                netManager.setCallback(mNetManagerEventImageRemoveCallback);
                                netManager.execute((Void) null);
                            }
                        }
                    });
                    dialog.setNegativeButton("취소", null);
                    dialog.show();
                }
            });
        }
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
        ImageView ivImage;
        ImageView ivDelete;
        RelativeLayout rlMenu;
        RelativeLayout rlLayout;
        Context mContext = null;

        public ListItemViewHolder(View itemView, int viewType) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_sub_image);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            rlMenu = (RelativeLayout) itemView.findViewById(R.id.rl_menu);
            rlLayout = (RelativeLayout) itemView.findViewById(R.id.rl_layout);
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
                    mListData.remove(mPosition);
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "이미지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else if (resultCode == 0) {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
