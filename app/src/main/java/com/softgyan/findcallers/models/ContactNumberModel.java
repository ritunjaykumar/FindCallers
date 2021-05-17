package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public final class ContactNumberModel implements Serializable {
    private int numberId;
    private int userRefId;
    private String mobileNumber;

    public ContactNumberModel(String mobileNumber) {
        this.mobileNumber = mobileNumber;
        numberId = -1;
        userRefId = -1;
    }

    public ContactNumberModel(int numberId, int userRefId, @NonNull String mobileNumber) {
        this.numberId = numberId;
        this.userRefId = userRefId;
        this.mobileNumber = mobileNumber;
    }

    public int getNumberId() {
        return numberId;
    }

    public void setNumberId(int numberId) {
        this.numberId = numberId;
    }

    public int getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(int userRefId) {
        this.userRefId = userRefId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String toString() {
        return "ContactNumberModel{" +
                "\nnumberId=" + numberId +
                "\nuserRefId=" + userRefId +
                "\nmobileNumber='" + mobileNumber + '\'' +
                "\n}";
    }
}
