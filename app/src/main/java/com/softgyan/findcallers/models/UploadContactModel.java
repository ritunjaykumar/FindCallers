package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class UploadContactModel implements Serializable {
    private String address;
    private String mobileNumber;
    private String profileUrl;
    private int totalName;
    private String userEmail;
    private String userName;
    private boolean isUserSetName;

    public UploadContactModel() {
        totalName = 0;
    }

    public UploadContactModel(String address, String mobileNumber, String profileUrl,
                              String userEmail, String userName, boolean isUserSetName) {
        this.address = address;
        this.mobileNumber = mobileNumber;
        this.profileUrl = profileUrl;
        this.totalName = 0;
        this.userEmail = userEmail;
        this.userName = userName;
        this.isUserSetName = isUserSetName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public int getTotalName() {
        return totalName;
    }

    public void setTotalName(int totalName) {
        this.totalName = totalName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUserSetName() {
        return isUserSetName;
    }

    public void setUserSetName(boolean userSetName) {
        isUserSetName = userSetName;
    }

    @NonNull
    @Override
    public String toString() {
        return "UploadContactModel{" +
                "\naddress='" + address + '\'' +
                "\nmobileNumber='" + mobileNumber + '\'' +
                "\nprofileUrl='" + profileUrl + '\'' +
                "\ntotalName=" + totalName +
                "\nuserEmail='" + userEmail + '\'' +
                "\nuserName='" + userName + '\'' +
                "\nisUserSetName=" + isUserSetName +
                '}';
    }
}
