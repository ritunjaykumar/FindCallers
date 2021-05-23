package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CallerInfoModel implements Serializable {
    private final String name;
    private final String number;
    private final int simId;
    private final String profileUri;
    private final boolean isContactSaved;
    private final boolean isIncoming;

    private CallerInfoModel(String name, @NonNull String number, int simId, String profileUri,
                            boolean isContactSaved, boolean isIncoming) {
        this.name = name;
        this.number = number;
        this.simId = simId;
        this.profileUri = profileUri;
        this.isContactSaved = isContactSaved;
        this.isIncoming = isIncoming;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public int getSimId() {
        return simId;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public boolean isContactSaved() {
        return isContactSaved;
    }

    public boolean isIncoming() {
        return isIncoming;
    }

    @NonNull
    @Override
    public String toString() {
        return "CallerInfoModel{" +
                "\nname='" + name + '\'' +
                "\nnumber='" + number + '\'' +
                "\nsimId=" + simId +
                "\nprofileUri='" + profileUri + '\'' +
                "\nisContactSaved=" + isContactSaved +
                "\nisIncoming=" + isIncoming +
                '}';
    }

    public static CallerInfoModel getInstance(String name,@NonNull String number, int simId, String profileUri,
                                              boolean isContactSaved, boolean isIncoming) {
        return new CallerInfoModel(name, number, simId, profileUri, isContactSaved, isIncoming);
    }
}
