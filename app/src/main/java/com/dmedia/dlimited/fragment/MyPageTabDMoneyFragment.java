package com.dmedia.dlimited.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmedia.dlimited.R;

public class MyPageTabDMoneyFragment extends Fragment implements View.OnClickListener {
    @Override
    public void onClick(View v) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage_tab_dmoney, null);
        return view;
    }

    /*
    Context mContext;
    protected Handler handler;

    private Button mDMoneyButton;
    private Button mHistoryButton;
    private LinearLayout mHistoryLayout;

    private ImageView mBarcodeImageView;
    private LinearLayout mPointLayout;
    private TextView mPointTextView;

    private LinearLayoutManager mLayoutManager;
    private ArrayList<DMoneyHistoryListData> mDataList;
    private DMoneyHistroryListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    public static final int MODE_DMONEY_BARCODE = 101;
    public static final int MODE_DMONEY_HISTORY = 102;
    private int mode = MODE_DMONEY_BARCODE;

    int mCount = Const.PAGE_SIZE_SMALL_ITEMS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        mode = MODE_DMONEY_BARCODE; //가끔씩 버튼 안눌리던 현상 수정
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage_tab_dmoney, null);
        handler = new Handler();

        mDMoneyButton = (Button) view.findViewById(R.id.btn_dmoney);
        mHistoryButton = (Button) view.findViewById(R.id.btn_history);
        mBarcodeImageView = (ImageView) view.findViewById(R.id.iv_barcode);
        mHistoryLayout = (LinearLayout) view.findViewById(R.id.ll_history);
        mPointLayout = (LinearLayout) view.findViewById(R.id.ll_point);
        mPointTextView = (TextView) view.findViewById(R.id.tv_point);
        mDMoneyButton.setOnClickListener(this);
        mHistoryButton.setOnClickListener(this);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mDataList = new ArrayList<>();

        mAdapter = new DMoneyHistroryListAdapter(mContext, mDataList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

        mPointTextView.setText(CommonData.LoginUserData.dMoney + "");
        if (CommonData.LoginUserData.barcode != null || CommonData.LoginUserData.barcode.equals("")) {
            drawCode128Barcode(CommonData.LoginUserData.barcode);
        }

        mAdapter.setOnLoadMoreListener(new DMoneyHistroryListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                final Runnable r = new Runnable() {
                    public void run() {
                        mAdapter.notifyItemInserted(mDataList.size());
                    }
                };
                handler.post(r);

                if (mDataList.size() < mCount * current_page) {
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //remove progress item
                        mAdapter.notifyItemRemoved(mDataList.size() - 1);
                        //add items one by one
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userid", CommonData.LoginUserData.userId);
                        params.put("page", current_page + 1);
                        params.put("count", mCount);
                        params.put("session_token", CommonData.LoginUserData.loginToken);
                        NetData netData = new NetData(NetData.ProtocolType.DMONEY_HISTORY, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                        NetManager netManager = new NetManager(netData, mContext);
                        netManager.setCallback(mNetManagerDMoneyHistoryCallback);
                        netManager.execute((Void) null);

                    }
                }, 500);
            }
        });

        return view;
    }

    private void drawCode128Barcode(String data) {
        Code128 code = new Code128(mContext);
        code.setData(data);
        Bitmap bitmap = code.getBitmap(680, 300);
        mBarcodeImageView.setImageBitmap(bitmap);
    }

    //13자리 바코드
    private void drawEAN13Barcode(String data) {
        EAN13 code = new EAN13();
        code.setData(data);
        Bitmap bitmap = code.getBitmap(680, 300);
        mBarcodeImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dmoney:
                if (mode != MODE_DMONEY_BARCODE) {
                    mHistoryLayout.setVisibility(LinearLayout.GONE);
                    mBarcodeImageView.setVisibility(ImageView.VISIBLE);
                    mRecyclerView.setVisibility(RecyclerView.GONE);
                    mPointLayout.setVisibility(LinearLayout.VISIBLE);
                    mode = MODE_DMONEY_BARCODE;
                    mDMoneyButton.setTextColor(Color.WHITE);
                    mDMoneyButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_fill_black));
                    mHistoryButton.setTextColor(Color.BLACK);
                    mHistoryButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));

                    mPointTextView.setText(CommonData.LoginUserData.dMoney + "");
                    if (CommonData.LoginUserData.barcode != null || CommonData.LoginUserData.barcode.equals("")) {
                        drawCode128Barcode(CommonData.LoginUserData.barcode);
                    }
                }
                break;
            case R.id.btn_history:
                if (mode != MODE_DMONEY_HISTORY) {
                    mHistoryLayout.setVisibility(LinearLayout.VISIBLE);
                    mBarcodeImageView.setVisibility(ImageView.GONE);
                    mRecyclerView.setVisibility(RecyclerView.VISIBLE);
                    mPointLayout.setVisibility(LinearLayout.GONE);
                    mode = MODE_DMONEY_HISTORY;
                    mDMoneyButton.setTextColor(Color.BLACK);
                    mDMoneyButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_black));
                    mHistoryButton.setTextColor(Color.WHITE);
                    mHistoryButton.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_fill_black));

                    mDataList.clear();
                    mAdapter.setCurrentPageToZero();

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("userid", CommonData.LoginUserData.userId);
                    params.put("page", 1);
                    params.put("count", mCount);
                    params.put("session_token", CommonData.LoginUserData.loginToken);
                    NetData netData = new NetData(NetData.ProtocolType.DMONEY_HISTORY, NetData.MethodType.GET, NetData.ProgressType.SPLASH, params);
                    NetManager netManager = new NetManager(netData, mContext);
                    netManager.setCallback(mNetManagerDMoneyHistoryCallback);
                    netManager.execute((Void) null);
                }
                break;
            default:
                break;
        }
    }

    private void addList(int id, String date, String place, String label, String dmoney) {
        date = date.substring(0, 10);//시간까지 나오는것 방지
        if (label.equals("use")) {
            label = "사용";
            dmoney = "-" + dmoney;
        }
        if (label.equals("add")) {
            label = "적립";
            dmoney = "+" + dmoney;
        }
        mDataList.add(new DMoneyHistoryListData(id, date, place, label, dmoney));
    }

    private NetManager.Callbacks mNetManagerDMoneyHistoryCallback = new NetManager.Callbacks() {
        @Override
        public void result(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            try {
                Log.d("callback : ", jsonObject.toString());
                int resultCode = jsonObject.getInt("result");

                if (resultCode == 1) {
                    //성공
                    JSONArray jsonArray = jsonObject.getJSONArray("return_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        int id = object.getInt("id");
                        String date = object.getString("cdate");
                        String place = object.getString("place");
                        String label = object.getString("type");//add(+),use(-)
                        String dmoney = object.getString("money");//금액

                        addList(id, date, place, label, dmoney);
                    }

                    mAdapter.setLoaded();
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(mContext, "서버와의 통신에 실패했습니다.\n잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    */
}
