package com.dmedia.dlimited;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

public class GroupMemberData implements Parcelable {
    int id;//event model 테이블 인덱스
    int eventId;//event 인덱스
    String username;//이름
    String level;//guest,host,celeb...
    String instagram;
    String profileImgUrl;

    public GroupMemberData(int id, int eventId, String username, String level, String instagram, String profileImgUrl) {
        this.id = id;
        this.eventId = eventId;
        this.username = username;
        this.level = level;
        this.instagram = instagram;
        this.profileImgUrl = profileImgUrl;
    }
    public GroupMemberData(int eventId, String username, String level, String instagram, String profileImgUrl) {
        this.eventId = eventId;
        this.username = username;
        this.level = level;
        this.instagram = instagram;
        this.profileImgUrl = profileImgUrl;
    }

    protected GroupMemberData(Parcel in) {
        id = in.readInt();
        eventId = in.readInt();
        username = in.readString();
        level = in.readString();
        instagram = in.readString();
        profileImgUrl = in.readString();
    }

    public static final Creator<GroupMemberData> CREATOR = new Creator<GroupMemberData>() {
        @Override
        public GroupMemberData createFromParcel(Parcel in) {
            return new GroupMemberData(in);
        }

        @Override
        public GroupMemberData[] newArray(int size) {
            return new GroupMemberData[size];
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(eventId);
        dest.writeString(username);
        dest.writeString(level);
        dest.writeString(instagram);
        dest.writeString(profileImgUrl);
    }
}
