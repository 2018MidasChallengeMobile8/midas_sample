package com.dmedia.dlimited;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by xema0 on 2016-11-02.
 */

public class GalleryTabContentData implements Parcelable{
    int id;//이미지 인덱스
    String url;
    String type;//'place', 'party','celeb'
    int sort;//순서 조작 할 경우 display될 순서
    String cDate;//이미지 추가된 날짜

    public GalleryTabContentData(int id, String url, String type, String cDate) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.cDate = cDate;
    }

    public GalleryTabContentData(int id, String url, String type, int sort, String cDate) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.sort = sort;
        this.cDate = cDate;
    }

    protected GalleryTabContentData(Parcel in) {
        id = in.readInt();
        url = in.readString();
        type = in.readString();
        sort = in.readInt();
        cDate = in.readString();
    }

    public static final Creator<GalleryTabContentData> CREATOR = new Creator<GalleryTabContentData>() {
        @Override
        public GalleryTabContentData createFromParcel(Parcel in) {
            return new GalleryTabContentData(in);
        }

        @Override
        public GalleryTabContentData[] newArray(int size) {
            return new GalleryTabContentData[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(type);
        dest.writeInt(sort);
        dest.writeString(cDate);
    }
}
