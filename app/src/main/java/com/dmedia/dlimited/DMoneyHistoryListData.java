package com.dmedia.dlimited;

public class DMoneyHistoryListData {
    private int id;
    private String date;
    private String place;//매장
    private String label;//구분
    private String dmoney;

    public DMoneyHistoryListData(int id, String date, String place, String label, String dmoney) {
        this.id = id;
        this.date = date;
        this.place = place;
        this.label = label;
        this.dmoney = dmoney;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDmoney() {
        return dmoney;
    }

    public void setDmoney(String dmoney) {
        this.dmoney = dmoney;
    }
}
