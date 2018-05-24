package com.dmedia.dlimited.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dmedia.dlimited.common.Const;
import com.dmedia.dlimited.model.CommonData;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by min on 2016-09-18.
 */
public class Utils {
    //문자열이 숫자로 구성되어있는지 판별
    public static boolean isDigit(String input) {
        if (Pattern.matches("^[0-9]+$", input)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPhoneValid(String phone) {
        return Utils.isDigit(phone) && (phone.length() >= 9 && phone.length() <= 12);
    }

    public static boolean isPasswordValid(String password) {
        final String PATTERN = "^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{8,16}$";
        //final String PATTERN = "^[a-zA-Z0-9]{8,16}$";
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isNameValid(String name) {
        return (name.length() != 0 && name.length() <= 20);
    }

    public static boolean isInstaIdValid(String instaId) {
        return (instaId.length() != 0 && instaId.length() <= 20);
    }

    public static boolean isNicknameValid(String nick) {
        return (nick.length() >= 2 && nick.length() <= 14);
    }

    public static String getUDID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void kakaoShare(Context context, String messege, String imageUrl, String type, int id) {
        try {
            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
            final KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
            kakaoBuilder.addText(messege);
            // TODO: 2016-11-14 이미지 사이즈 관련한 이슈들 정리
            kakaoBuilder.addImage(imageUrl, 1000, 800);
            kakaoBuilder.addAppButton("앱으로 이동",
                    new AppActionBuilder().
                            addActionInfo(AppActionInfoBuilder
                                    .createAndroidActionInfoBuilder()
                                    .setExecuteParam("share=true" + "&type=" + type + "&id=" + id)
                                    .build()).build()
            );
            kakaoLink.sendMessage(kakaoBuilder, context);
        } catch (KakaoParameterException e) {
            e.printStackTrace();
            Toast.makeText(context, "메인이미지가 없는 이벤트는 공유가 불가능합니다.\n메인이미지를 추가해주세요", Toast.LENGTH_SHORT).show();
        }
    }

    //grayscale 이미지 메소드
    public static Bitmap getGrayImage(Bitmap bmpOriginal) {

        int width, height;

        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);


        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);

        return bmpGrayscale;

    }

    //drawble 객체를 bitmap 객체로
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isInstaValid() {
        boolean tmp = true;
        if (CommonData.LoginUserData.instagramId.equals("null")) {
            tmp = false;
        }
        if (CommonData.LoginUserData.instagramId == null) {
            tmp = false;
        }
        if (CommonData.LoginUserData.instagramId.equals("")) {
            tmp = false;
        }
        if (CommonData.LoginUserData.instagramId.equals(" ")) {
            tmp = false;
        }
        return tmp;
    }


    //레벨 검사후 권한 없으면 차단후 다이얼로그 생성 + instagram 인증했는지도 검사
    /*
    public static boolean showInvalidLevelAndInstagramDialog(final Context context) {
        boolean isValidLevel = false;
        boolean isValidInsta = false;

        if (isLevelValid(Const.LEVEL_GUEST)) {
            isValidLevel = true;
        }
        if (isInstaValid()) {
            isValidInsta = true;
        }

        if (isValidInsta && isValidLevel) {
            return true;
        } else {
            String message = "";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("권한 부족");
            if (!isValidLevel) {
                message += "D-Code 인증\n";
            }
            if (!isValidInsta) {
                message += "Instagram 연동\n";
            }
            message += "\n참가신청을 하기 위해 위 항목을 마이페이지에서 설정해주세요.";
            builder.setMessage(message);
            builder.setPositiveButton("확인",null);
            builder.show();

            return false;
        }
    }
    */


    //레벨 검사후 권한 없으면 차단후 다이얼로그 생성
    public static void showInvalidLevelDialog(Context context) {
        String koreanLevel = "";
        String message = "";
        switch (CommonData.LoginUserData.level) {
            case "looker":
                koreanLevel = "비회원";
                message = "로그인이 필요합니다";
                break;
            case "peeker":
                koreanLevel = "D-Code 미인증 회원";
                message = "D-Code 인증이 필요합니다";
                break;
            case "guest":
                koreanLevel = "게스트";
                break;
            case "host":
                koreanLevel = "호스트";
                break;
            case "dgod":
                koreanLevel = "D-God";
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("권한 부족");
        builder.setMessage("해당 기능을 실행할 권한이 없습니다.\n고객님의 현재 계급 : " + koreanLevel + "\n" + message);
        builder.setPositiveButton("확인", null);
        builder.show();
    }

    public static boolean isLevelValid(int needLevel) {
        String cntLevelStr = CommonData.LoginUserData.level;
        int cntLevel;
        switch (cntLevelStr) {
            case "looker":
                cntLevel = Const.LEVEL_LOOKER;
                break;
            case "peeker":
                cntLevel = Const.LEVEL_PEEKER;
                break;
            case "guest":
                cntLevel = Const.LEVEL_GUEST;
                break;
            case "host":
                cntLevel = Const.LEVEL_HOST;
                break;
            case "dgod":
                cntLevel = Const.LEVEL_DGOD;
                break;
            default:
                cntLevel = Const.LEVEL_LOOKER;
                break;
        }

        if (cntLevel < needLevel) {
            //권한 부족
            return false;
        } else {
            return true;
        }
    }

    public static final int UPLOAD_IMAGE_MAXIMUM_WIDTH = 1024;
    public static final int UPLOAD_IMAGE_MAXIMUM_HEIGHT = 1024;

    public static Bitmap decodeSampledBitmapFromResource(String bitmapFilePath) {
        return decodeSampledBitmapFromResource(bitmapFilePath, UPLOAD_IMAGE_MAXIMUM_WIDTH, UPLOAD_IMAGE_MAXIMUM_HEIGHT);
    }

    public static Bitmap decodeSampledBitmapFromResource(String bitmapFilePath, int reqWidth, int reqHeight) {
        File image = new File(bitmapFilePath);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(image.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Bitmap to File
    public static File saveBitmapToFileCache(Bitmap bitmap, String strFilePath, String filename) {
        File file = new File(strFilePath);

        // If no folders
        if (!file.exists()) {
            file.mkdirs();
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        File fileCacheItem = new File(strFilePath + filename);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileCacheItem;
    }

    public static String getAppVersion(Context context) {
        String ver = "";
        try {
            ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ver;
    }
}
