package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.models.SimCardInfoModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class CallUtils {
    private static final String TAG = "CallUtils";

    public synchronized static void sendMessage(Context context, String message, ArrayList<String> sendNumbers) {
        final String[] messagePermission = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS};
        if (!Utils.checkPermission(context, messagePermission)) {
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (String sendNumber : sendNumbers) {
                message(smsManager, sendNumber, message);
            }
        } catch (Exception e) {
            Log.d(TAG, "sendMessage: error : " + e.getMessage());
        }


        //todo do later
    }

    private static synchronized void message(SmsManager smsManager, String sendNumber, String message) {
        smsManager.sendTextMessage(sendNumber, null, message, null, null);
        Log.d(TAG, "sendMessage: message send to " + sendNumber);
    }

    public static synchronized void sendMessageIntent(Context context, String number) {
        Uri uriSms = Uri.parse("smsto:" + number);
        Intent intentSMS = new Intent(Intent.ACTION_SENDTO, uriSms);
        intentSMS.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentSMS);
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

    public synchronized static void getCallNotification(Context context, String mobileNumber, OnUploadCallback callback) {
        if (!Utils.isInternetConnectionAvailable(context)) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        final Date today = calendar.getTime();


        FirebaseDB.CallNotification.getCallNotification(context, mobileNumber, new OnResultCallback<HashMap<String, Object>>() {
            @Override
            public void onSuccess(@NonNull HashMap<String, Object> stringObjectHashMap) {
                String startDate = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.START_DATE);
                String startTime = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.START_TIME);
                String endDate = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.END_DATE);
                String endTime = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.END_TIME);
                String sMessage = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.MESSAGE);

                if (startDate != null && startTime != null && endDate != null && endTime != null && sMessage != null) {

                    final boolean compareDate = compareDate(Utils.getTime(today), Utils.getDate(today), endTime, endDate);
                    Log.d(TAG, "onSuccess: compare date : "+compareDate);
                    if (compareDate) {
                        callback.onUploadSuccess(sMessage + " till " + endTime);
                    } else {
                        callback.onUploadFailed("something wrong");
                    }
                } else {
                    callback.onUploadFailed("getting null value");
                }

            }

            @Override
            public void onFailed(String failedMessage) {
                callback.onUploadFailed(failedMessage);
                Log.d(TAG, "onFailed: failed Message : " + failedMessage);
            }
        });
    }

    private static boolean compareDate(String startTime, String startDate, String endTime, String endDate) {
        Log.d(TAG, "compareDate: startTime : " + startTime);
        Log.d(TAG, "compareDate: startDate : " + startDate);
        Log.d(TAG, "compareDate: endTime : " + endTime);
        Log.d(TAG, "compareDate: endDate : " + endDate);
        try {
            String[] startTimeTemp = startTime.split(":");
            String[] startDateTemp = startDate.split("/");
            String[] endTimeTemp = endTime.split(":");
            String[] endDateTemp = endDate.split("/");

            int sDay = Integer.parseInt(startDateTemp[0]);
            int sMonth = Integer.parseInt(startDateTemp[1]);
            int sYear = Integer.parseInt(startDateTemp[2]);

            int sHour = Integer.parseInt(startTimeTemp[0]);
            int sMinute = Integer.parseInt(startTimeTemp[1]);

            int eDay = Integer.parseInt(endDateTemp[0]);
            int eMonth = Integer.parseInt(endDateTemp[1]);
            int eYear = Integer.parseInt(endDateTemp[2]);

            int eHour = Integer.parseInt(endTimeTemp[0]);
            int eMinute = Integer.parseInt(endTimeTemp[1]);

            if (eYear - sYear == 0 && eMonth - sMonth == 0 && eDay - sDay == 0) {
                Log.d(TAG, "compareDate: same day");
            }else{
                return false;
            }

            int sSecond = sHour * 3600 + sMinute * 60;
            int eSecond = eHour * 3600 + eMinute * 60;

            Log.d(TAG, "compareDate: eSecond : " + eSecond + " sSecond : " + sSecond);

            int finalSecond = 5 * 3600;

            int differenceSecond = sSecond - eSecond;

            if (differenceSecond <= finalSecond) {
                return true;
            }
            Log.d(TAG, "compareDate: difference : " + differenceSecond);


        } catch (Exception e) {
            Log.d(TAG, "compareDate: error : " + e.getMessage());
            return false;
        }

        return false;
    }


}
