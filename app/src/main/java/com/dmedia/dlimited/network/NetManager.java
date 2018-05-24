package com.dmedia.dlimited.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dmedia.dlimited.R;
import com.dmedia.dlimited.dialog.LoadingProgressDialog;
import com.dmedia.dlimited.model.CommonData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static android.net.ConnectivityManager.TYPE_WIFI;

public class NetManager extends AsyncTask<Void, Void, JSONObject> {
    private static final String TAG = "NetManager";

    private static final String serverAddress = "http://www.dlimited.co.kr/api/";

    private NetData mNetData = null;
    private Context mContext = null;

    final static String crlf = "\r\n";
    final static String twoHyphens = "--";
    final static String boundary = "*****";

    private static boolean isShowAlertDialog = false;

    public NetManager(@NonNull NetData netData, @NonNull Context context) {
        this.mNetData = netData;
        this.mContext = context;
    }

    private ProgressDialog progressDialog;
    private Callbacks mCallback;

    public interface Callbacks {
        public void result(JSONObject jsonObject);
    }

    public void setCallback(Callbacks mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        if (isNetWorkConnected(mContext) == false) {
            return null;
        } else {
            JSONObject resultObject = null;
            URL url;
            try {
                if (mNetData.getMethodType().name().equals("GET") || mNetData.getMethodType().name().equals("DELETE")) {
                    StringBuffer buffer = createRequestParamsBuffer(mNetData.getParams());
                    //url = new URL(serverAddress + mNetData.getProtocolType().name().toLowerCase() + '/' + "?" + buffer.toString());
                    url = new URL(serverAddress + mNetData.getProtocolType().name().toLowerCase().replaceAll("_", "/") + "?" + buffer.toString());
                } else {
                    //url = new URL(serverAddress + mNetData.getProtocolType().name().toLowerCase() + '/');
                    url = new URL(serverAddress + mNetData.getProtocolType().name().toLowerCase().replaceAll("_", "/"));
                }
                Log.d(TAG, "url: " + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDefaultUseCaches(false);

                conn.setDoInput(true);
                conn.setRequestMethod(mNetData.getMethodType().name());
                Log.d(TAG, "========= " + CommonData.LoginUserData.userName);
                //conn.setRequestProperty("Authorization", "Token " + CommonData.LoginUserData.userName);
                conn.setConnectTimeout(8000); //8 secs
                conn.setReadTimeout(8000); //8 secs
                Log.d(TAG, mNetData.getMethodType().name());
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                if (mNetData.isMultipartform()) {
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);

                    DataOutputStream writer = new DataOutputStream(conn.getOutputStream());

                    // Login token 폼 필드 전송
                    //addFormField(writer, "token", CommonData.LoginUserData.userName);

                    HashMap<String, Object> params = mNetData.getParams();

                    for (String key : params.keySet()) {
                        if (key.contains("img_list")) {
                            //dlimited 서버 커스텀
                            // 파일 폼 필드 전송
                            Bitmap[] bitmaps = (Bitmap[]) params.get(key);
                            for (int i = 0; i < bitmaps.length; i++) {
                                addFilePartBitmap(writer, key + i, bitmaps[i]);
                            }
                        } else {
                            // 일반 폼 필드 전송
                            addFormField(writer, key, params.get(key).toString());
                        }
                    }

                    writer.flush();
                    writer.close();

                } else if (mNetData.getMethodType().name().equals("GET")
                        || mNetData.getMethodType().name().equals("DELETE")) { // OutputStreamWriter로 데이터를 보내게 되면 MethodType이 POST로 자동으로 변경되기 때문에 처리해준다

                } else if (mNetData.getMethodType().name().equals("POST")
                        || mNetData.getMethodType().name().equals("PUT")) {

                    conn.setDoOutput(true);
                    OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                    PrintWriter writer = new PrintWriter(outStream);

                    StringBuffer buffer = createRequestParamsBuffer(mNetData.getParams());
                    writer.write(buffer.toString());
                    writer.flush();
                    writer.close();
                }

                if (checkResponse(conn.getResponseCode()) == false) {
                    return null;
                }

                InputStreamReader inStream = new InputStreamReader(conn.getInputStream(), "UTF-8");
                String jsonstr = "";

                BufferedReader reader = new BufferedReader(inStream);
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                System.out.println(result.toString());
                jsonstr = result.toString();

                inStream.close();

                try {
                    resultObject = new JSONObject(jsonstr);
                } catch (Exception e) {
                    e.printStackTrace();
                    return getJSONErrorObject(104, "json parser error");
                }
                return resultObject;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return getJSONErrorObject(105, "url error");
            } catch (IOException e) {
                e.printStackTrace();
                return getJSONErrorObject(106, "IO exception");
            }
        }
    }

    private JSONObject getJSONErrorObject(int result, String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("result", result);
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void addFormField(DataOutputStream writer, String fieldName, String value) {
        try {
            writer.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            writer.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + this.crlf + this.crlf);
            writer.write(value.getBytes("UTF-8"));
            writer.writeBytes(this.crlf);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addFilePartBitmap(DataOutputStream writer, String fieldName, Bitmap bitmap) {
        try {
            writer.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            writer.writeBytes("Content-Disposition: form-data; name=\"img_list\";filename=\"" + fieldName + ".png\"" + this.crlf);
            writer.writeBytes(this.crlf);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            writer.write(imageBytes);

            writer.writeBytes(this.crlf);
            writer.writeBytes(this.twoHyphens + this.boundary + this.crlf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static StringBuffer createRequestParamsBuffer(HashMap<String, Object> params) {

        StringBuffer buffer = new StringBuffer();

        Log.d("check ",params.toString());
        for (String key : params.keySet()) {
            buffer.append(key).append("=").append(params.get(key).toString()).append("&");
        }
        if (buffer.length() != 0)
            buffer.deleteCharAt(buffer.length() - 1);

        return buffer;
    }

    private void errorAlert(String title, String content) {
        try {
            if (!getIsShowAlertDialog()) {
                setIsShowAlertDialog(true);
                android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder((Activity) mContext);
                dialog.setTitle(title);
                dialog.setMessage(content);
                dialog.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setIsShowAlertDialog(false);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        } catch (ClassCastException e) {
            Log.d("ClassCastException", e.toString());
        }
    }

    private synchronized void setIsShowAlertDialog(boolean isShow) {
        isShowAlertDialog = isShow;
    }

    private synchronized boolean getIsShowAlertDialog() {
        return isShowAlertDialog;
    }

    // TODO: 2016-10-25 에러 발생하는 지점. 나중에 고치기
    /*
    FATAL EXCEPTION: AsyncTask #3
    Process: com.dmedia.dlimited, PID: 29517
    java.lang.RuntimeException: An error occurred while executing doInBackground()
        at android.os.AsyncTask$3.done(AsyncTask.java:309)
        at java.util.concurrent.FutureTask.finishCompletion(FutureTask.java:354)
        at java.util.concurrent.FutureTask.setException(FutureTask.java:223)
        at java.util.concurrent.FutureTask.run(FutureTask.java:242)
        at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:234)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)
        at java.lang.Thread.run(Thread.java:818)
     Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
        at android.os.Handler.<init>(Handler.java:200)
        at android.os.Handler.<init>(Handler.java:114)
        at android.app.Dialog.<init>(Dialog.java:119)
        at android.app.AlertDialog.<init>(AlertDialog.java:214)
        at android.app.AlertDialog$Builder.create(AlertDialog.java:1158)
        at android.app.AlertDialog$Builder.show(AlertDialog.java:1183)
        at com.dmedia.dlimited.network.NetManager$override.checkResponse(NetManager.java:269)
        at com.dmedia.dlimited.network.NetManager$override.doInBackground(NetManager.java:135)
        at com.dmedia.dlimited.network.NetManager$override.access$dispatch(NetManager.java)
        at com.dmedia.dlimited.network.NetManager.doInBackground(NetManager.java:0)
        at com.dmedia.dlimited.network.NetManager.doInBackground(NetManager.java:34)
        at android.os.AsyncTask$2.call(AsyncTask.java:295)
        at java.util.concurrent.FutureTask.run(FutureTask.java:237)
        at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:234) 
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113) 
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588) 
        at java.lang.Thread.run(Thread.java:818) 
     */
    private boolean checkResponse(int responseCode) {

        Log.d(TAG, "The response code is: " + responseCode);

        if (responseCode == 403) {
            final Activity activity = (Activity) mContext;
            AlertDialog.Builder alert = new AlertDialog.Builder(activity)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //어플 종료
                            activity.moveTaskToBack(true);
                            ActivityCompat.finishAffinity(activity);
                            activity.finish();
                        }
                    }).setMessage("비정상적 접근입니다.");
            alert.show();
            return false;
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
//        super.onPreExecute();
        if (mNetData.getProgressType() == NetData.ProgressType.NONE) {

        } else if (mNetData.getProgressType() == NetData.ProgressType.MESSAGE) {
            LoadingProgressDialog.showProgress(mContext, "데이터 전송 중입니다.");
        } else if (mNetData.getProgressType() == NetData.ProgressType.SPLASH) {
            LoadingProgressDialog.showProgress(mContext);
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        //super.onPostExecute(jsonObject);
        if (jsonObject == null) {
            errorAlert(mContext.getString(R.string.error_internet_connection_title),
                    mContext.getString(R.string.error_internet_connection_content));
        } else {
            if (jsonObject.length() == 0) {
                return;
            } else {
                try {
                    int resultCode = jsonObject.getInt("result");
                    if (resultCode != 1) {
                        //콜백받은 에러
                        if (resultCode == 101) {
                            Log.d("Network error", "Bad parameter");
                        } else if (resultCode == 102) {
                            Log.d("Network error", "Internal server error");
                        } else if (resultCode == 103) {
                            Log.d("Network error", "Invalid token");
                        }

                        //클라이언트에서 발생한 에러
                        else if (resultCode == 104) {
                            Log.d("Client error", "JSON parser error");
                        } else if (resultCode == 105) {
                            Log.d("Client error", "Url error");
                        } else if (resultCode == 106) {
                            Log.d("Client error", "IO exception");
                        }

                        // TODO: 2016-10-27 에러 메세지 각각의 액티비티에서 처리
                        //errorAlert(mContext.getString(R.string.error_internet_connection_title),mContext.getString(R.string.error_internet_connection_content));
                    }
                } catch (JSONException e) {
                    Log.d("JSON Exception occur", e.toString());
                }
            }
        }
        if (mCallback != null)
            mCallback.result(jsonObject);
        LoadingProgressDialog.hideProgress();
    }

    public static boolean isNetWorkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == TYPE_WIFI)
                return true;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }
}
