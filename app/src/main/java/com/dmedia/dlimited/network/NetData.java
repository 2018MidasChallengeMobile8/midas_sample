package com.dmedia.dlimited.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by jeonghoy on 2016. 7. 13..
 */

public class NetData {
    public enum ProtocolType {
        USER_AUTH,//회원가입시 핸드폰 인증(문자전송)
        USER_AUTH_DCODE,
        USER_SIGNUP,//회원가입
        USER_SIGNIN,//로그인
        USER_WITHDRAW,
        USER_FIND_PASSWORD,
        USER_CHANGE_PASSWORD,
        USER_INFO,
        USER_UPDATE,
        USER_ALARM,
        USER_QNA,
        USER_FAQ,
        USER_DEVICE_PUT,
        USER_INSTA_LOGIN,
        USER_INSTA_LOGIN_URL,//??

        USER_EVENT_LIST,
        //server 오타 -> api상이랑 다름
        //USER_DMONEY_HISTORY,
        DMONEY_HISTORY,
        INFO_VERSION,
        USER_DBOX_LIST,

        EVENT_LIST,
        EVENT_REQUEST,
        EVENT_DETAIL,
        EVENT_IMAGE_PUT,//멀티파트 이미지 업로드
        EVENT_IMAGE_ADD,//이미지 URL?
        EVENT_IMAGE_REMOVE,
        EVENT_GUEST_ADD,
        EVENT_GUEST_REMOVE,
        EVENT_USER_REMOVE,
        EVENT_USER_PENDING_LIST,
        EVENT_USER_DETAIL,
        EVENT_USER_APPLY,
        EVENT_EDIT,
        EVENT_USER_REMOVE_ALL,
        EVENT_USER_REQUEST,
        EVENT_SEARCH,
        EVENT_USER_REQUEST_LIST,
        EVENT_IMAGE_SORT,

        GALLERY_LIST,

        DBOX_CATEGORY_LIST,
        DBOX_PLACE_LIST,
        DBOX_BANNER_LIST,
        DBOX_LIST,
        DBOX_DETAIL,
        DBOX_REQUEST,
        DBOX_IMAGE_PUT,//멀티파트 이미지 업로드
        DBOX_IMAGE_ADD,//이미지 URL?
        DBOX_IMAGE_REMOVE,
        DBOX_MODIFY,
        DBOX_SEARCH,
        DBOX_MODEL_ADD,//메인 게스트?모델? 추가
        DBOX_GUEST_PENDING_LIST,
        DBOX_GUEST_APPROVE,
        DBOX_REVIEW_LIST,
        DBOX_REVIEW_DETAIL,
        DBOX_REVIEW_PUT,
        DBOX_GUEST_APPLY,
        DBOX_GUEST_CHANGE,
        DBOX_USER_REQUEST_LIST,
        DBOX_USER_REMOVE,
        DBOX_MODEL_REMOVE,
        DBOX_USER_REMOVE_ALL,;
    }

    public enum MethodType {
        POST,
        GET,
        PUT,
        DELETE
    }

    public enum ProgressType {
        NONE,
        MESSAGE,
        SPLASH
    }

    private ProtocolType protocolType;
    private MethodType methodType;
    private ProgressType progressType;
    private HashMap<String, Object> params;
    private boolean isMultipartform;

    public NetData(@NonNull ProtocolType protocolType, @NonNull MethodType methodType, @NonNull ProgressType progressType, @Nullable HashMap<String, Object> params) {
        this.protocolType = protocolType;
        this.methodType = methodType;
        this.progressType = progressType;
        this.params = params;
    }

    public NetData(@NonNull ProtocolType protocolType, @NonNull MethodType methodType, @NonNull ProgressType progressType, @Nullable HashMap<String, Object> params, boolean isMultipartform) {
        this.protocolType = protocolType;
        this.methodType = methodType;
        this.progressType = progressType;
        this.params = params;
        this.isMultipartform = isMultipartform;
    }


    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    public ProgressType getProgressType() {
        return progressType;
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public boolean isMultipartform() {
        return isMultipartform;
    }

    public void setMultipartform(boolean multipartform) {
        isMultipartform = multipartform;
    }
}
