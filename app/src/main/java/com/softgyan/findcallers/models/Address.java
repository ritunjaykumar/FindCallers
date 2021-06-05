package com.softgyan.findcallers.models;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public abstract class Address implements Serializable {
    private String area;
    private String pinCode;
    private String state;
    private String district;
    private GeoPoint point;
    private String mapLocation;

    public Address(String area, String pinCode, String state, String district, GeoPoint point,
                   String mapLocation) {
        this.area = area;
        this.pinCode = pinCode;
        this.state = state;
        this.district = district;
        this.point = point;
        this.mapLocation = mapLocation;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(String mapLocation) {
        this.mapLocation = mapLocation;
    }


    @NonNull
    @Override
    public String toString() {
        return "Address{" +
                "area='" + area + '\'' +
                ", pinCode='" + pinCode + '\'' +
                ", state='" + state + '\'' +
                ", district='" + district + '\'' +
                ", point=" + point +
                ", mapLocation='" + mapLocation + '\'' +
                '}';
    }
}
