package com.softgyan.findcallers.receivers;

import static com.softgyan.findcallers.services.CallManagerServices.MOBILE_NUMBER;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.services.CallManagerServices;

import java.util.Date;

public class CallReceiver extends CallStateReceiver {
    private static final String TAG = "CallReceiverTemp";


    @Override
    protected void onCallStart(Context context, String mobNumber, Date startCallDate, int simId, boolean isOutGoing) {
        final boolean login = AppPreference.isLogin(context);
        Log.d(TAG, "onCallStart: login : "+login);
        if (!login) {
            return;
        }

        Log.d(TAG, "onCallStart: incoming ..." + mobNumber);

        Intent intent = new Intent(context, CallManagerServices.class);
        intent.putExtra(MOBILE_NUMBER, mobNumber);
        intent.putExtra(CallManagerServices.IS_OUT_GOING, isOutGoing);
        intent.putExtra(CallManagerServices.CALL_KEY, CallManagerServices.CALL_INITIATE);

        new Handler().postDelayed(
                () -> ContextCompat.startForegroundService(context, intent), 0
        );
    }

    @Override
    protected void onCallReceive(Context context, String mobNumber, boolean isOutGoing) {
        final boolean login = AppPreference.isLogin(context);
        if (!login) {
            return;
        }

        sendBroadcastForService(context, CallManagerServices.CALL_HOOKED);
    }

    @Override
    protected void onCallEnd(Context context, String mobNumber, boolean isOutGoing, Date endCallDate, int simId) {
        final boolean login = AppPreference.isLogin(context);
        if (!login) {
            return;
        }

        Log.d(TAG, "onCallEnded: bye bye : " + mobNumber);

        sendBroadcastForService(context, CallManagerServices.CALL_END);
    }


    private void sendBroadcastForService(Context context, int id) {
        Intent intent = new Intent();
        intent.setAction(CallManagerServices.CALL_NOTIFIER_RECEIVER_ACTION);
        intent.putExtra(CallManagerServices.CALL_KEY, id);
        context.sendBroadcast(intent);
    }
}
