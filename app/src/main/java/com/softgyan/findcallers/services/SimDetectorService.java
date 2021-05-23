package com.softgyan.findcallers.services;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.CallUtils;

import java.util.ArrayList;
import java.util.List;

public class SimDetectorService extends Service {

    public volatile static boolean isSimDetectorServiceRunning = false;
    private static final String TAG = "my_tag";
    private Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: called");
        ctx = getApplicationContext();
        isSimDetectorServiceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        ArrayList<String> newNumbers = new ArrayList<>();

//        if(ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED)
            //todo check permission
        new Thread() {
            @Override
            public void run() {
                final List<SimCardInfoModel> currentSimCardInfos = SimCardInfoModel.getSimInfoS(ctx);
                if (currentSimCardInfos == null) {
                    stopSelf();
                    return;
                }

                String[] oldSimCardIccS = AppPreference.SimPreference.getSimIccS(ctx);

                for (SimCardInfoModel currentSimInfo : currentSimCardInfos) {
                    boolean isIccFind = false;
                    for (String oldIcc : oldSimCardIccS) {
                        if (currentSimInfo.getIccId().equals(oldIcc)) {
                            isIccFind = true;
                            break;
                        }
                    }
                    if (!isIccFind) {
                        newNumbers.add(currentSimInfo.getMobileNumber());
                    }
                }

                if(newNumbers.size() == 0){
                    stopSelf();
                    return;
                }

                String simMessage = AppPreference.SimPreference.getSimMessage(ctx);
                final String firstNumber = AppPreference.SimPreference.getFirstNumber(ctx);
                final String secondNumber = AppPreference.SimPreference.getSecondNumber(ctx);

                StringBuilder sb = new StringBuilder();
                if (simMessage == null) {
                    sb.append("Mr **** your phone has been lost. ***** has got your phone, their phone numbers are..\n\n");
                }
                for(String num : newNumbers){
                    sb.append(num).append("\n");
                }

                String newMessageWithNumber = sb.toString();

                CallUtils.sendMessage(ctx, newMessageWithNumber, firstNumber, secondNumber);
                stopSelf();

            }
        }.start();


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSimDetectorServiceRunning = false;
    }

    private void startServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Doing some background task")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        startForeground(1, notification);
    }

}
