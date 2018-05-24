package com.dmedia.dlimited;

import android.graphics.drawable.Drawable;

/**
 * Created by min on 2016-09-23.
 */
public class HomeTabContentsListData {
    private String imageUrl;
    private String title;
    private String date;
    private String location;
    private String limit;
    private int id;//인덱스

    public HomeTabContentsListData(String title, String date, String location, String limit, String url, int id) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.limit = limit;
        this.imageUrl = url;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
