package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.models.SimCardInfoModel;

import java.util.ArrayList;
import java.util.List;

public final class CallUtils {
    public synchronized static void sendMessage(Context context, String message, ArrayList<String> sendNumbers) {
        final String[] messagePermission = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};
        if (!Utils.checkPermission(context, messagePermission)) {
            return;
        }
        Intent sendMessageIntent = new Intent();


        //todo do later
    }

    public static int getSubscriptionId(final Context context, @NonNull final String iccId) {
        SimCardInfoModel.getSimInfoS(context);
        final List<SimCardInfoModel> simCardInfoList = SimCardInfoModel.getSimInfoS(context);
        if (simCardInfoList == null) return -1;
        for (SimCardInfoModel simInfo : simCardInfoList) {
            if (iccId.equals(simInfo.getIccId())) {
                return simInfo.getSubscriptionId();
            }
        }
        return -1;
    }


}
