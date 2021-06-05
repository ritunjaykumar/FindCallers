package com.softgyan.findcallers.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

public final class UserInfoModel implements Serializable {
    private String userName;
    private String userEmail;
    private String userProfile;
    private String userTag;
    private boolean emailVerify;
    private String userAddress;
    private String accountMobileNumber;
    private boolean businessAccount;
    private static UserInfoModel mModel;

    private UserInfoModel(String userName, String userEmail, String userProfile, String userTag,
                          boolean emailVerify, String userAddress, boolean businessAccount) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfile = userProfile;
        this.userTag = userTag;
        this.emailVerify = emailVerify;
        this.userAddress = userAddress;
        this.businessAccount = businessAccount;
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null)
            this.accountMobileNumber = currentUser.getPhoneNumber();
        else
            accountMobileNumber = null;
    }

    private UserInfoModel(String userName, String userEmail, String userProfile, String userTag,
                          boolean emailVerify, String userAddress, String accountMobileNumber, boolean businessAccount) {
        this(userName, userEmail, userProfile, userTag, emailVerify, userAddress, businessAccount);
        this.accountMobileNumber = accountMobileNumber;
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

    public String getAccountMobileNumber() {
        return accountMobileNumber;
    }

    public boolean isBusinessAccount() {
        return businessAccount;
    }

    public void setBusinessAccount(boolean businessAccount) {
        this.businessAccount = businessAccount;
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
                ", accountMobileNumber='" + accountMobileNumber + '\'' +
                ", businessAccount=" + businessAccount +
                '}';
    }

    public static UserInfoModel getInstance(String userName, String userEmail, String userProfile,
                                            String userTag, boolean isEmailVerify, String userAddress,
                                            String accountMobileNumber, boolean businessAccount) {

        if (mModel == null) {
            mModel = new UserInfoModel(userName, userEmail, userProfile, userTag, isEmailVerify,
                    userAddress, accountMobileNumber, businessAccount);
        }
        return mModel;
    }

    public static UserInfoModel getInstance(String userName, String userEmail, String userProfile,
                                            String userTag, boolean isEmailVerify, String userAddress, boolean businessAccount) {

        if (mModel == null) {
            mModel = new UserInfoModel(userName, userEmail, userProfile, userTag, isEmailVerify, userAddress, businessAccount);
        }
        return mModel;
    }


}
