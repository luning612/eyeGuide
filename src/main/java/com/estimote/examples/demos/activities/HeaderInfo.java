package com.estimote.examples.demos.activities;

import java.util.ArrayList;

public class HeaderInfo {

    private String categoryName;
    private ArrayList<DetailInfo> locationList = new ArrayList<DetailInfo>();;

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public ArrayList<DetailInfo> getLocationList() {
        return locationList;
    }
    public void setLocationList(ArrayList<DetailInfo> locationList) {
        this.locationList = locationList;
    }

}