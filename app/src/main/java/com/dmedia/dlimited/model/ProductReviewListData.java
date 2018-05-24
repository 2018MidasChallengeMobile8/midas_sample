package com.dmedia.dlimited.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductReviewListData implements Parcelable {
    private int id;
    private int dboxId;
    private int userId;
    private String userNickname;
    private String phoneNumber;
    private String missionUrl;
    private String reviewText;

    public ProductReviewListData(int id, int dboxId, int userId, String userNickname, String phoneNumber, String missionUrl, String reviewText) {
        this.id = id;
        this.dboxId = dboxId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.phoneNumber = phoneNumber;
        this.missionUrl = missionUrl;
        this.reviewText = reviewText;
    }

    protected ProductReviewListData(Parcel in) {
        id = in.readInt();
        dboxId = in.readInt();
        userId = in.readInt();
        userNickname = in.readString();
        phoneNumber = in.readString();
        missionUrl = in.readString();
        reviewText = in.readString();
    }

    public static final Creator<ProductReviewListData> CREATOR = new Creator<ProductReviewListData>() {
        @Override
        public ProductReviewListData createFromParcel(Parcel in) {
            return new ProductReviewListData(in);
        }

        @Override
        public ProductReviewListData[] newArray(int size) {
            return new ProductReviewListData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDboxId() {
        return dboxId;
    }

    public void setDboxId(int dboxId) {
        this.dboxId = dboxId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMissionUrl() {
        return missionUrl;
    }

    public void setMissionUrl(String missionUrl) {
        this.missionUrl = missionUrl;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(dboxId);
        dest.writeInt(userId);
        dest.writeString(userNickname);
        dest.writeString(phoneNumber);
        dest.writeString(missionUrl);
        dest.writeString(reviewText);
    }
}
