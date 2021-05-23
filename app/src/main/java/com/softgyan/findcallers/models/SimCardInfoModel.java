package com.softgyan.findcallers.models;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.exception.InvalidException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class SimCardInfoModel implements Serializable {
    private final static List<SimCardInfoModel> simCardInfoList = new ArrayList<>();
    private final int subscriptionId;
    private final String mobileNumber;
    private final String displayName;
    private final String carrierName;
    private final String countryIso;
    private final String IccId;
    private final int cardId;

    private SimCardInfoModel(int subscriptionId, String mobileNumber, String displayName,
                             String carrierName, String countryIso, String iccId) {
        this.subscriptionId = subscriptionId;
        this.mobileNumber = mobileNumber;
        this.displayName = displayName;
        this.carrierName = carrierName;
        this.countryIso = countryIso;
        this.IccId = iccId;
        this.cardId = -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private SimCardInfoModel(int subscriptionId, String mobileNumber, String displayName,
                             String carrierName, String countryIso, String iccId, int cardId) {
        this.subscriptionId = subscriptionId;
        this.mobileNumber = mobileNumber;
        this.displayName = displayName;
        this.carrierName = carrierName;
        this.countryIso = countryIso;
        IccId = iccId;
        this.cardId = cardId;
    }


    public int getSubscriptionId() {
        return subscriptionId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getCountryIso() {
        return countryIso;
    }

    public String getIccId() {
        return IccId;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public int getCardId() {
        return cardId;
    }

    @NonNull
    @Override
    public String toString() {
        return "SimCardInfoModel{" +
                "\nsubscriptionId=" + subscriptionId +
                "\nmobileNumber='" + mobileNumber + '\'' +
                "\ndisplayName='" + displayName + '\'' +
                "\ncarrierName='" + carrierName + '\'' +
                "\ncountryIso='" + countryIso + '\'' +
                "\nIccId='" + IccId + '\'' +
                "\ncardId=" + cardId +
                '}';
    }


    public static List<SimCardInfoModel> getSimInfoS(final Context context) {
        if (simCardInfoList.size() != 0) {
            return simCardInfoList;
        }
        try {
            final List<SubscriptionInfo> simCardInfoS = CallUtils.getSimCardInfo(context);
            for (SubscriptionInfo si : simCardInfoS) {
                final SimCardInfoModel sci;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    sci = new SimCardInfoModel(
                            si.getSubscriptionId(), si.getNumber(), si.getDisplayName().toString(),
                            si.getCarrierName().toString(), si.getCountryIso(), si.getIccId(), si.getCardId()
                    );
                } else {
                    sci = new SimCardInfoModel(
                            si.getSubscriptionId(), si.getNumber(), si.getDisplayName().toString(),
                            si.getCarrierName().toString(), si.getCountryIso(), si.getIccId()
                    );
                }

                simCardInfoList.add(sci);
            }

        } catch (InvalidException e) {
            e.printStackTrace();
            return null;
        }
        return simCardInfoList;
    }


}
