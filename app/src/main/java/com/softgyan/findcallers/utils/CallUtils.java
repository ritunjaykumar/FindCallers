package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.utils.exception.InvalidException;

import java.util.List;

public final class CallUtils {
    public static void sendMessage(Context context, String message, String... sendNumbers) {
        final String[] messagePermission = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};
        if(!Utils.checkPermission(context, messagePermission)){
            return;
        }
        Intent sendMessageIntent = new Intent();


        //todo do later
    }

    public static int getSubscriptionId(final Context context, @NonNull final String iccId) {
        SimCardInfoModel.getSimInfoS(context);
        final List<SimCardInfoModel> simCardInfoList = SimCardInfoModel.getSimInfoS(context);
        if(simCardInfoList == null) return -1;
        for (SimCardInfoModel simInfo : simCardInfoList) {
            if (iccId.equals(simInfo.getIccId())) {
                return simInfo.getSubscriptionId();
            }
        }
        return -1;
    }

    public static List<SubscriptionInfo> getSimCardInfo(Context context) throws InvalidException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED) {
            final SubscriptionManager subscriptionManager =
                    (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            return subscriptionManager.getActiveSubscriptionInfoList();
        }
        throw new InvalidException("Permission Not Granted -> Manifest.permission.READ_PHONE_STATE");

    }

}
