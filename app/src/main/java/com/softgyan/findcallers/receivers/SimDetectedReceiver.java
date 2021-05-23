package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.services.SimDetectorService;

public class SimDetectedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serInt = new Intent(context, SimDetectorService.class);
        ContextCompat.startForegroundService(context, serInt);
    }


}