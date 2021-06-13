package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.services.SimDetectorService;

public class SimDetectedReceiver extends BroadcastReceiver {
    private static final String TAG = "SimDetectedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "phone restarted", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onReceive: phone restarted");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent serInt = new Intent(context, SimDetectorService.class);
                serInt.putExtra(SimDetectorService.TASK_KEY, SimDetectorService.INIT_TASK_VALUE);
                ContextCompat.startForegroundService(context, serInt);
                Toast.makeText(context, "service started", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "run: service started");
            }
        }, 1000);
    }


}