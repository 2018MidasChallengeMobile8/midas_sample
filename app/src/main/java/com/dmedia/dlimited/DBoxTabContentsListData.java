package com.dmedia.dlimited;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by min on 2016-09-23.
 */
public class DBoxTabContentsListData{
    private int id;
    private int categoryId;
    private String phone;
    private String title;
    private String date;
    private String location;
    private String limit;
    private String url;

    public DBoxTabContentsListData(int id, int categoryId, String phone, String title, String date, String location, String limit, String url) {
        this.id = id;
        this.categoryId = categoryId;
        this.phone = phone;
        this.title = title;
        this.date = date;
        this.location = location;
        this.limit = limit;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
