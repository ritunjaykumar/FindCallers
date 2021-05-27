package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public abstract class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiverTemp";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static final String OUT_GOING_CALL_ACTION = "android.intent.action.NEW_OUTGOING_CALL";
    private static final String OUT_GOING_CALL_NUMBER_KEY = "android.intent.extra.PHONE_NUMBER";
    private static final String SIM_ID_KEY = "simId";
    private boolean isOutGoing = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            int simId = intent.getExtras().getInt(SIM_ID_KEY, -1);
            String callNumber;
            if (intent.getAction().equals(OUT_GOING_CALL_ACTION)) {
                String tempCallNumber = intent.getExtras().getString(OUT_GOING_CALL_NUMBER_KEY);
                if (tempCallNumber == null) return;
                callNumber = tempCallNumber;
                isOutGoing = true;
                onCallStart(context, callNumber, new Date(), simId, isOutGoing);
                Log.d(TAG, String.format("onReceive: outGoingCallNumber : %s, isOutGoing : %s, simId : %d", tempCallNumber, "true", simId));
                return;
            }

            String incomingTempNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (incomingTempNumber == null) return;
            callNumber = incomingTempNumber;
            String callingState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int callState = -1;
            if (callState == lastState) {
                return;
            }
            if (callingState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                //'call' when incoming call
                callState = TelephonyManager.CALL_STATE_RINGING;
                onCallStart(context, callNumber, new Date(), simId, isOutGoing);
                Log.d(TAG, "onReceive: callState : CALL_STATE_RINGING");
            } else if (callingState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                //'call' when call receive
                callState = TelephonyManager.CALL_STATE_OFFHOOK;
                onCallReceive(context, callNumber, isOutGoing);
                Log.d(TAG, "onReceive: callState : CALL_STATE_OFFHOOK");
            } else if (callingState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //'call' when call is disconnected
                callState = TelephonyManager.CALL_STATE_IDLE;
                onCallEnd(context, callNumber,isOutGoing, new Date(), simId);
                Log.d(TAG, "onReceive: callState : CALL_STATE_IDLE");
            }
            lastState = callState;


        } catch (Exception e) {
            Log.d(TAG, "onReceive: exception : " + e.getMessage());
        }
    }


    protected abstract void onCallStart(final Context context, final String mobNumber,
                                        final Date startCallDate, final int simId, final boolean isOutGoing);

    protected abstract void onCallReceive(final Context context, final String mobNumber, final boolean isOutGoing);

    protected abstract void onCallEnd(final Context context, final String mobNumber, final boolean isOutGoing,
                                      final Date endCallDate, final int simId);
}
