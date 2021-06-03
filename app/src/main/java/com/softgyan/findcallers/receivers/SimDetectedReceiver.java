package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.services.SimDetectorService;

public class SimDetectedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent serInt = new Intent(context, SimDetectorService.class);
                serInt.putExtra(SimDetectorService.TASK_KEY, SimDetectorService.INIT_TASK_VALUE);
                ContextCompat.startForegroundService(context, serInt);
            }
        }, 1000);
    }


}