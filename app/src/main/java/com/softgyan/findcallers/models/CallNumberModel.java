package com.softgyan.findcallers.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class CallNumberModel implements Serializable {
    private int callModelId;
    private int nameRefId;
    private final String number;
    private final Date date;
    private final int type;
    private final String iccId;
    private final int ringTime;
    private final long duration;

    public CallNumberModel(@NonNull String number, Date date, int type,@NonNull String iccId,
                           int ringTime, long duration) {
        this.callModelId = -1;
        this.nameRefId = -1;
        this.number = number;
        this.date = date;
        this.type = type;
        this.iccId = iccId;
        this.ringTime = ringTime;
        this.duration = duration;
    }

    public CallNumberModel(int callModelId, int nameRefId, String number, Date date, int type,
                           String iccId, int ringTime, long duration) {
        this.callModelId = callModelId;
        this.nameRefId = nameRefId;
        this.number = number;
        this.date = date;
        this.type = type;
        this.iccId = iccId;
        this.ringTime = ringTime;
        this.duration = duration;
    }

    public void setCallModelId(int callModelId) {
        this.callModelId = callModelId;
    }

    public void setNameRefId(int nameRefId) {
        this.nameRefId = nameRefId;
    }

    public int getCallModelId() {
        return callModelId;
    }

    public int getNameRefId() {
        return nameRefId;
    }

    public String getNumber() {
        return number;
    }

    public Date getDate() {
        return date;
    }

    public int getType() {
        return type;
    }

    public String getIccId() {
        return iccId;
    }

    public int getRingTime() {
        return ringTime;
    }

    public long getDuration() {
        return duration;
    }

    @NonNull
    @Override
    public String toString() {
        return "CallNumberModel{" +
                "\ncallModelId=" + callModelId +
                "\nnameRefId=" + nameRefId +
                "\nnumber='" + number + '\'' +
                "\ndate=" + date +
                "\ntype=" + type +
                "\nsubscriptionId='" + iccId + '\'' +
                "\nringTime=" + ringTime +
                "\nduration=" + duration +
                "\n}";
    }
}
