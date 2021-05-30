package com.softgyan.findcallers.services;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.widgets.activity.FindMobileActivity;

import java.util.ArrayList;
import java.util.List;

public class SimDetectorService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SimDetectorService";
    public static final String TASK_KEY = "taskKey";
    private boolean isServiceStarted = false;
    private final SimDetectorServiceBinder mServiceBinder = new SimDetectorServiceBinder();

    private String firstNumber;
    private String secondNumber;
    private String defaultMessage;
    private String[] previousSimInfo;
    private int lastTaskKey = -1;
    public static int INIT_TASK_VALUE = 1;
    public static int CHECK_SIM_STATUS_TASK = 2;
    public static int SEND_MESSAGE_TASK = 3;
    private static final int STOP_SERVICE_TASK = 4;
    private Intent mIntent;
    private int startId;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(tickBroadcastReceiver, filter);
        Log.d(TAG, "onCreate: called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        isServiceStarted = true;
        this.startId = startId;
        this.mIntent = intent;
        backgroundThread.start();
        Log.d(TAG, "onStartCommand: onStartCommand");
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceStarted = false;
        unregisterReceiver(tickBroadcastReceiver);
        Log.d(TAG, "onDestroy: called");
    }

    private void startServiceNotification() {
        Intent intent = new Intent(getApplicationContext(), FindMobileActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Intent actionIntent = new Intent(getApplicationContext(), tickBroadcastReceiver.getClass());
        actionIntent.putExtra(TASK_KEY, STOP_SERVICE_TASK);

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, actionIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_2)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Doing some background task")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_power, "Stop Service", actionPendingIntent)
                .build();
        startForeground(1, notification);
        //todo click event for opening activity
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case AppPreference.SimPreference.FIRST_NUMBER_KEY:
                firstNumber = sharedPreferences.getString(key, firstNumber);
                break;
            case AppPreference.SimPreference.SECOND_NUMBER_KEY:
                secondNumber = sharedPreferences.getString(key, secondNumber);
                break;
            case AppPreference.SimPreference.SIM_INFO_MESSAGE:
                defaultMessage = sharedPreferences.getString(key, defaultMessage);
                break;
            default:
                Log.d(TAG, "onSharedPreferenceChanged: not required any thing");
                break;
        }
    }

    private final Thread backgroundThread = new Thread() {

        @Override
        public void run() {
            ArrayList<String> sendMessageList = new ArrayList<>();
            while (true) {
                int taskKey = mIntent.getIntExtra(TASK_KEY, -1);
                if (taskKey == lastTaskKey) {
                    continue;
                }
                if (taskKey == INIT_TASK_VALUE) {
                    firstNumber = AppPreference.SimPreference.getFirstNumber(getApplicationContext());
                    secondNumber = AppPreference.SimPreference.getSecondNumber(getApplicationContext());
                    defaultMessage = AppPreference.SimPreference.getSimMessage(getApplicationContext());
                    previousSimInfo = AppPreference.SimPreference.getSimIccS(getApplicationContext());
                } else if (taskKey == CHECK_SIM_STATUS_TASK) {
                    List<SimCardInfoModel> currentSimCardInfo = SimCardInfoModel.getSimInfoS(getApplicationContext());
                    if (currentSimCardInfo != null && previousSimInfo != null) {

                        for (SimCardInfoModel cardInfo : currentSimCardInfo) {
                            boolean isEqual = false;
                            for (String preSimInfo : previousSimInfo) {
                                if (preSimInfo.equals(cardInfo.getIccId())) {
                                    isEqual = true;
                                    break;
                                }
                            }
                            if (!isEqual) {
                                sendMessageList.add(cardInfo.getMobileNumber());
                            }
                        }
                        mIntent.putExtra(TASK_KEY, SEND_MESSAGE_TASK);
                    }

                } else if (taskKey == SEND_MESSAGE_TASK) {
                    if (sendMessageList.size() != 0) {
                        CallUtils.sendMessage(getApplicationContext(), defaultMessage, sendMessageList);
                    }
                } else if (taskKey == STOP_SERVICE_TASK) {
                    stopSelf();
                    break;
                }
                lastTaskKey = taskKey;
            }
        }
    };


    private final BroadcastReceiver tickBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: tick broadcast received");
            int taskValue = intent.getIntExtra(TASK_KEY, -1);
            if (taskValue == STOP_SERVICE_TASK) {
                mIntent.putExtra(TASK_KEY, taskValue);
            } else {
                mIntent.putExtra(TASK_KEY, CHECK_SIM_STATUS_TASK);
            }
        }
    };

    public void stopSimDetectorService() {
        mIntent.putExtra(TASK_KEY, STOP_SERVICE_TASK);
    }

    public boolean isServiceStart() {
        return isServiceStarted;
    }


    public class SimDetectorServiceBinder extends Binder {
        public SimDetectorService getSimDetectorService() {
            return SimDetectorService.this;
        }
    }
}
