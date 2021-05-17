package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public abstract class CallStateReceiver extends BroadcastReceiver {

    private static final String TAG = CallStateReceiver.class.getName();
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;

    private boolean isIncoming;
    private String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            int callingSIM = intent.getExtras().getInt("simId", -1);
            Log.d(TAG, "onReceive: sim_id : " + callingSIM);

            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                Log.d(TAG, "onReceive: outgoing number : " + savedNumber);

            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (number == null) {
                    //just skip the state when extra_incoming_number is null;
                    return;
                }

                int state = -1;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                } else {
                    return;
                }

                onCallStateChanged(context, state, number, callingSIM);
            }
        } catch (Exception e) {
            Log.d(TAG, "onReceive: exception : " + e.getMessage());
        }

    }

    abstract void onCallStart(final Context ctx, final String number, final Date startDate, final int callSimId);

    abstract void onCallEnded(Context ctx, String number, Date endDate, boolean isIncoming);

    abstract void onCallReceived(Context ctx, String savedNumber, boolean isIncoming);


    public void onCallStateChanged(Context context, int state, final String number, final int callSimId) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                savedNumber = number;
                onCallStart(context, savedNumber, new Date(), callSimId);
                Log.d(TAG, "onCallStateChanged: state : CALL_STATE_RINGING with number : " + number);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                savedNumber = number;
                if (isIncoming) {
                    onCallReceived(context, savedNumber, true);
                    Log.d(TAG, "onCallStateChanged: state : CALL_STATE_OFF_HOOK");
                } else {
                    onCallReceived(context, savedNumber, false);
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                savedNumber = number;
                if (isIncoming) {
                    onCallEnded(context, savedNumber, new Date(), true);
                    Log.d(TAG, "onCallStateChanged: state : CALL_STATE_IDLE");
                } else {
                    onCallEnded(context, savedNumber, new Date(), false);
                }
                break;
        }
        lastState = state;
    }

}

