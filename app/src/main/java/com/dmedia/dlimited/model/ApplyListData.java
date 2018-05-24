package com.dmedia.dlimited.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xema0 on 2016-10-17.
 */

public class ApplyListData implements Parcelable {
    int id;//event_user 테이블 인덱스
    int eventId;//event 인덱스
    int userId;//user테이블 인덱스
    String userName;//유저 닉네임
    String instagram;//인스타 numeric 아이디
    String instagramName;//인스타그램 아이디
    String phoneNumber;//
    String applyComment;//신청시 작성한 한마디
    String status;//상태(complete,request...)
    String cDate;//신청 날짜
    String profileUrl;//프로필 주소 url

    public ApplyListData(int id, int eventId, int userId, String userName, String instagram, String instagramName, String phoneNumber, String applyComment, String status, String cDate, String profileUrl) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.instagram = instagram;
        this.instagramName = instagramName;
        this.phoneNumber = phoneNumber;
        this.applyComment = applyComment;
        this.status = status;
        this.cDate = cDate;
        this.profileUrl = profileUrl;
    }

    protected ApplyListData(Parcel in) {
        id = in.readInt();
        eventId = in.readInt();
        userId = in.readInt();
        userName = in.readString();
        instagram = in.readString();
        instagramName = in.readString();
        phoneNumber = in.readString();
        applyComment = in.readString();
        status = in.readString();
        cDate = in.readString();
        profileUrl = in.readString();
    }

    public static final Creator<ApplyListData> CREATOR = new Creator<ApplyListData>() {
        @Override
        public ApplyListData createFromParcel(Parcel in) {
            return new ApplyListData(in);
        }

        @Override
        public ApplyListData[] newArray(int size) {
            return new ApplyListData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getInstagramName() {
        return instagramName;
    }

    public void setInstagramName(String instagramName) {
        this.instagramName = instagramName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getApplyComment() {
        return applyComment;
    }

    public void setApplyComment(String applyComment) {
        this.applyComment = applyComment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(eventId);
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeString(instagram);
        dest.writeString(instagramName);
        dest.writeString(phoneNumber);
        dest.writeString(applyComment);
        dest.writeString(status);
        dest.writeString(cDate);
        dest.writeString(profileUrl);
    }
}
