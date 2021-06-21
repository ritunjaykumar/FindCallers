package com.softgyan.findcallers.models;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public abstract class BusinessModel extends Address implements Serializable {
    private String name;
    private String contact;
    private String gender;
    private double distance;

    public BusinessModel(String area, String pinCode, String state, String district, GeoPoint point,
                         String mapLocation, String name, String gender,  String contact, double distance) {
        super(area, pinCode, state, district, point, mapLocation);
        this.name = name;
        this.contact = contact;
        this.gender = gender;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private Address getAddress() {
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "BusinessModel{" +
                "name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
