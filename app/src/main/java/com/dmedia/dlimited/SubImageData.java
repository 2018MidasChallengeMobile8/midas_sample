package com.dmedia.dlimited;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xema0 on 2016-11-15.
 */

public class SubImageData implements Parcelable{
    private int id;//이미지 테이블 아이디
    private int eventOrDBoxId;//이벤트 아이디
    private String filePath;
    private String imageUrl;
    private boolean file;
    private boolean event;

    public SubImageData(int eventOrDBoxId, String filePath, boolean file) {
        this.eventOrDBoxId = eventOrDBoxId;
        this.filePath = filePath;
        this.file = file;
    }

    public SubImageData(int id, int eventOrDBoxId, String imageUrl, boolean file, boolean event) {
        this.id = id;
        this.eventOrDBoxId = eventOrDBoxId;
        this.imageUrl = imageUrl;
        this.file = file;
        this.event = event;
    }

    protected SubImageData(Parcel in) {
        id = in.readInt();
        eventOrDBoxId = in.readInt();
        filePath = in.readString();
        imageUrl = in.readString();
        file = in.readByte() != 0;
        event = in.readByte() != 0;
    }

    public static final Creator<SubImageData> CREATOR = new Creator<SubImageData>() {
        @Override
        public SubImageData createFromParcel(Parcel in) {
            return new SubImageData(in);
        }

        @Override
        public SubImageData[] newArray(int size) {
            return new SubImageData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventOrDBoxId() {
        return eventOrDBoxId;
    }

    public void setEventOrDBoxId(int eventOrDBoxId) {
        this.eventOrDBoxId = eventOrDBoxId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(eventOrDBoxId);
        dest.writeString(filePath);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (file ? 1 : 0));
        dest.writeByte((byte) (event ? 1 : 0));
    }
}
