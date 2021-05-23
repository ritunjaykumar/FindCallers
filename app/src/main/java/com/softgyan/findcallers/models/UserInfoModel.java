package com.softgyan.findcallers.models;

public final class UserInfoModel {
    private String userName;
    private String userEmail;
    private String userProfile;
    private String userTag;
    private boolean emailVerify;
    private String userAddress;

    private UserInfoModel(String userName, String userEmail, String userProfile, String userTag,
                          boolean emailVerify, String userAddress) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfile = userProfile;
        this.userTag = userTag;
        this.emailVerify = emailVerify;
        this.userAddress = userAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public String getUserTag() {
        return userTag;
    }

    public boolean isEmailVerify() {
        return emailVerify;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public void setUserTag(String userTag) {
        this.userTag = userTag;
    }

    public void setEmailVerify(boolean emailVerify) {
        this.emailVerify = emailVerify;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    @Override
    public String toString() {
        return "UserInfoModel{" +
                "userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userProfile='" + userProfile + '\'' +
                ", userTag='" + userTag + '\'' +
                ", emailVerify=" + emailVerify +
                ", userAddress='" + userAddress + '\'' +
                '}';
    }

    public static UserInfoModel getInstance(String userName, String userEmail, String userProfile,
                                            String userTag, boolean isEmailVerify, String userAddress) {
        return new UserInfoModel(userName, userEmail, userProfile, userTag, isEmailVerify, userAddress);
    }
}
