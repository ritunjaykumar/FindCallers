package com.softgyan.findcallers.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.Date;

public class CallManagerServices extends Service {

    private static final int CALL_SERVICE_ID = 5;
    private static final String TAG = "CallManagerServices";
    public static final String CALL_NOTIFIER_RECEIVER_ACTION = "com.softgyan.findcallers.CALL_NOTIFY_RECEIVER";
    public static final String CALL_KEY = "endCallCode";
    public static final int END_CALL_CODE = 1;

    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String INCOMING_DATE = "incomingDate";
    public static final String SIM_ID = "simId";
    public static final String IS_OUT_GOING = "isOutGoing";

    @Override
    public void onCreate() {
        super.onCreate();
        registerCallReceiver();
        Log.d(TAG, "onCreate: service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        if (intent != null) {
            Log.d(TAG, "onStartCommand: service started");
            String mobNum = intent.getStringExtra(MOBILE_NUMBER);
            int simId = intent.getIntExtra(SIM_ID, -1);
            Date date = (Date) intent.getSerializableExtra(INCOMING_DATE);
            final CallerInfoModel[] callerInfoModel = new CallerInfoModel[1];
           new Thread(){
               @Override
               public void run() {
                   Log.d(TAG, "run: service start");
                    final ContactModel contactModel = ContactsQuery.searchByNumber(getApplicationContext(), mobNum);
                    if (contactModel == null) {
                        Log.d(TAG, "run: contact model : null");
                        callerInfoModel[0] = CallerInfoModel.getInstance(
                                null, mobNum, simId, null,
                                true, true
                        );
                    } else {
                        Log.d(TAG, "run: contact model : " + contactModel);
                        callerInfoModel[0] = CallerInfoModel.getInstance(
                                contactModel.getName(), mobNum, simId, contactModel.getImage(),
                                false, true
                        );
                    }

                   Handler handler = new Handler(getApplicationContext().getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Utils.showDialogOverCall(getApplicationContext(), callerInfoModel[0]);
                        }
                    };
                    handler.post(runnable);
               }
           }.start();
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
                .build();
        startForeground(CALL_SERVICE_ID, notification);
    }

    private void stopService() {
        this.stopSelf();
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int endCallCode = intent.getIntExtra(CALL_KEY, -1);
                Log.d(TAG, "onReceive: endCallCode : " + endCallCode);
                if (endCallCode == END_CALL_CODE) {
                    stopService();
                }
            } else {
                Log.d(TAG, "onReceive: intent value : null");
            }
        }
    };


}
