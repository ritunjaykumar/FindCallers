package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CallModel implements Serializable {
    private  int nameId;
    private  String cacheName;
    private final List<CallNumberModel> callNumberList;

    public CallModel(int nameId, String cacheName) {
        this.nameId = nameId;
        this.cacheName = cacheName;
        this.callNumberList = new ArrayList<>();
    }

    public CallModel(int nameId,@NonNull String cacheName, final CallNumberModel callNumber) {
        this.nameId = nameId;
        this.cacheName = cacheName;
        this.callNumberList = new ArrayList<>();
        callNumberList.add(callNumber);
    }

    public CallModel(@NonNull String number) {
        this.nameId = -1;
        this.cacheName = "Unknown_" + number;
        this.callNumberList = new ArrayList<>();
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public int getNameId() {
        return nameId;
    }

    public String getCacheName() {
        return cacheName;
    }

    public List<CallNumberModel> getCallNumberList() {
        return callNumberList;
    }

    public void setCallNumber(CallNumberModel callNumber) {
        callNumberList.add(callNumber);
    }

    public int getTotalCallNumberList() {
        return callNumberList.size();
    }

    public void addNumberAtTop(CallNumberModel callNumberModel) {
        callNumberList.add(0, callNumberModel);
    }

    public CallNumberModel getFirstCall() {
        if (getTotalCallNumberList() != 0) {
            return callNumberList.get(0);
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "CallModel{" +
                "\nnameId=" + nameId +
                "\ncacheName='" + cacheName + '\'' +
                "\ncallNumberList=" + callNumberList +
                '}';
    }
}
