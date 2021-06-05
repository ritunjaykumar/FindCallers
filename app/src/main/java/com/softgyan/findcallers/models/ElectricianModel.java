package com.softgyan.findcallers.models;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class ElectricianModel extends BusinessModel implements Serializable {
    public ElectricianModel(String area, String pinCode, String state, String district, GeoPoint point,
                            String mapLocation, String name, String gender, String contact) {
        super(area, pinCode, state, district, point, mapLocation, name, gender, contact);
    }

    public Address getAddress() {
        return this;
    }

    public BusinessModel getBusinessDetails() {
        return this;
    }
}
