package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class DoctorModel extends BusinessModel implements Serializable {
    private String doctorType;

    public DoctorModel(String area, String pinCode, String state, String district, GeoPoint point,
                       String mapLocation, String name, String gender, String contact, String doctorType) {
        super(area, pinCode, state, district, point, mapLocation, name, gender, contact);
        this.doctorType = doctorType;
    }

    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    @NonNull
    @Override
    public String toString() {
        return "DoctorModel{" +
                "doctorType='" + doctorType + '\'' +
                '}';
    }
}
