package com.softgyan.findcallers.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.database.call.system.SystemCalls;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.firebase.FirebaseUserData;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.utils.CallerDialog;
import com.softgyan.findcallers.utils.Utils;

public class CallManagerServices extends Service {

    private static final int CALL_SERVICE_ID = 5;
    private static final String TAG = "CallManagerServices";
    public static final String CALL_NOTIFIER_RECEIVER_ACTION = "com.softgyan.findcallers.CALL_NOTIFY_RECEIVER";
    public static final String CALL_KEY = "callKey";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String INCOMING_DATE = "incomingDate";
    public static final String SIM_ID = "simId";
    public static final String IS_OUT_GOING = "isOutGoing";

    public static final int CALL_INITIATE = 1;
    public static final int CALL_HOOKED = 2;
    public static final int CALL_END = 3;


    private volatile boolean isOutgoing = false;
    private volatile boolean isHooked = false;
    private volatile boolean isCallInitiate = false;
    private volatile Intent callIntent = null;
    private CountDownTimer countDownTimer;
    private int lastUpdate = -1;
    private String mobNumber;
    private CallerDialog callerDialog;

    private CallerInfoModel callerInfoModels;

    @Override
    public void onCreate() {
        super.onCreate();
        registerCallReceiver();
        callerDialog = new CallerDialog(getApplicationContext());
        Log.d(TAG, "onCreate: service created");
        countDownTimer = new CountDownTimer(25000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        callIntent = intent;
        if (callIntent != null) {
            callThread.start();
        } else {
            stopService();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerCallReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CALL_NOTIFIER_RECEIVER_ACTION);
        registerReceiver(bReceiver, filter);
        Log.d(TAG, "registerCallReceiver: broadcast receiver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
        Log.d(TAG, "onDestroy: broadcast receiver unregistered");
    }

    private void startServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Find Caller is active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        startForeground(CALL_SERVICE_ID, notification);
    }

    private void stopService() {
        stopSelf();
    }

    private void showDialogOverCall(CallerInfoModel callerInfoModel, boolean isViewUpdated) {
        Handler handler = new Handler(getApplicationContext().getMainLooper());

        Runnable runnable = () -> callerDialog.showDialog(callerInfoModel, isViewUpdated);
//        Runnable runnable = () -> Utils.showDialogOverCall(getApplicationContext(), callerInfoModel, isViewUpdated);
        handler.post(runnable);
    }

    private final Thread callThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                int callValue = callIntent.getIntExtra(CALL_KEY, -1);
//                Log.d(TAG, "run: callValue : "+callValue);
                if (callValue == lastUpdate) {
                    continue;
                }
                if (callValue == CALL_INITIATE) {
                    /*
                     * show dialog on call screen+
                     */
                    Log.d(TAG, "run: callValue : " + "CALL_INITIATE");
                    isCallInitiate = true;
                    lastUpdate = callValue;
                    isOutgoing = callIntent.getBooleanExtra(IS_OUT_GOING, false);
                    mobNumber = callIntent.getStringExtra(MOBILE_NUMBER);
                    searchNumber(mobNumber);
                } else if (callValue == CALL_HOOKED) {
                    /*
                     * start call recording if enabled
                     */
                    Log.d(TAG, "run: called hooked");
                    lastUpdate = callValue;
                    isHooked = true;

                } else if (callValue == CALL_END) {
                    /*
                     * stop recording if started
                     * save number in DB+
                     * stop service+
                     * break the loop+
                     * */
                    Log.d(TAG, "run: call end");
                    lastUpdate = callValue;
                    if (!isOutgoing && !isHooked) {
                        //it means called 'missed call'
                        callerInfoModels.setMessage("Missed Called");
                    } else {
                        callerInfoModels.setMessage(null);
                    }

                    saveLastCallHistory(getApplicationContext(), mobNumber);
                    showDialogOverCall(callerInfoModels, true);
                    stopService();
                }


            }
        }
    };

    private void searchNumber(final String mobNum) {
        final ContactModel contactModel = Utils.advanceSearch(getApplicationContext(), mobNum);
        if (contactModel != null) {
            callerInfoModels = Utils.getCallerInfoModel(contactModel);
            showDialogOverCall(callerInfoModels, false);
        } else {
            if (Utils.isInternetConnectionAvailable(getApplicationContext())) {

                FirebaseUserData.MobileNumberInfo.getMobileNumber(mobNum, new OnResultCallback<ContactModel>() {
                    @Override
                    public void onSuccess(@NonNull ContactModel contactModel) {
                        callerInfoModels = Utils.getCallerInfoModel(contactModel);
                        showDialogOverCall(callerInfoModels, false);

                    }

                    @Override
                    public void onFailed(String failedMessage) {
                        callerInfoModels = CallerInfoModel.getInstance(null, mobNum, -1,
                                null, false, false);
                        showDialogOverCall(callerInfoModels, false);
                    }
                });
            } else {
                callerInfoModels = CallerInfoModel.getInstance(null, mobNum, -1,
                        null, false, false);
                showDialogOverCall(callerInfoModels, false);
            }

        }
    }

    private void saveLastCallHistory(Context context, String number) {
        final CallModel lastCallHistory = SystemCalls.getLastCallHistory(context);
        Log.d(TAG, "onReceive: lastCallHistory : " + lastCallHistory);
        if (lastCallHistory == null) {
            return;
        }
        final CallModel callModel = CallQuery.searchCallHistoryByNumber(context, number);
        if (callModel != null) {
            lastCallHistory.setNameId(callModel.getNameId());
            lastCallHistory.getCallNumberList().get(0).setNameRefId(callModel.getNameId());
        } else {
            CallQuery.insertCallNumberLog(context, lastCallHistory.getCallNumberList().get(0));
            return;
        }

        if (lastCallHistory.getCacheName() == null) {
            try {
                lastCallHistory.setCacheName(callerInfoModels.getName());
            } catch (Exception e) {
                Log.d(TAG, "saveCallLog: error : " + e.getMessage());
            }
        }

        CallQuery.insertCallLog(context, lastCallHistory);
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int callKey = intent.getIntExtra(CALL_KEY, -1);
                callIntent.putExtra(CALL_KEY, callKey);
            } else {
                Log.d(TAG, "onReceive: intent value : null");
            }
        }
    };

}