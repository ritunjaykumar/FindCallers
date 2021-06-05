package com.softgyan.findcallers.models;

import java.io.Serializable;
import java.util.List;

public class BusinessRecord implements Serializable {

    public static final int DOCTOR_TYPE = 1;
    public static final int ELECTRICIAN_TYPE = 2;


    private List<DoctorModel> doctorList;
    private List<ElectricianModel> electricianList;
    private final int type;

    public BusinessRecord(int type, List<DoctorModel> doctorList) {
        this(type);
        this.doctorList = doctorList;
    }

    public BusinessRecord(List<ElectricianModel> electricianList, int type) {
        this(type);
        this.electricianList = electricianList;

    }

    private BusinessRecord(int type) {
        this.type = type;
    }

    public List<DoctorModel> getDoctorList() {
        return doctorList;
    }

    public List<ElectricianModel> getElectricianList() {
        return electricianList;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BusinessRecord{" +
                "doctorList=" + doctorList +
                ", electricianList=" + electricianList +
                ", type=" + type +
                '}';
    }
}








