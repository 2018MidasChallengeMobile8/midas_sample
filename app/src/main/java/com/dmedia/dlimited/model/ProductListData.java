package com.dmedia.dlimited.model;

public class ProductListData {
    private int id;
    private String title;
    private String date;
    private String state;

    public ProductListData(int id,String title, String date, String state) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
