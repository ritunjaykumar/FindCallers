package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CallerInfoModel implements Serializable {
    private String name;
    private String number;
    private int simId;
    private String profileUri;
    private boolean isContactSaved;
    private boolean isIncoming;
    private String message;

    private CallerInfoModel(String name, @NonNull String number, int simId, String profileUri,
                            boolean isContactSaved, boolean isIncoming) {
        this.name = name;
        this.number = number;
        this.simId = simId;
        this.profileUri = profileUri;
        this.isContactSaved = isContactSaved;
        this.isIncoming = isIncoming;
        this.message = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setSimId(int simId) {
        this.simId = simId;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public void setContactSaved(boolean contactSaved) {
        isContactSaved = contactSaved;
    }

    public void setIncoming(boolean incoming) {
        isIncoming = incoming;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                "\nmessage=" + message +
                '}';
    }

    public static CallerInfoModel getInstance(String name, @NonNull String number, int simId, String profileUri,
                                              boolean isContactSaved, boolean isIncoming) {
        return new CallerInfoModel(name, number, simId, profileUri, isContactSaved, isIncoming);
    }
}
