package com.softgyan.findcallers.receivers;

import static com.softgyan.findcallers.services.CallManagerServices.INCOMING_DATE;
import static com.softgyan.findcallers.services.CallManagerServices.MOBILE_NUMBER;
import static com.softgyan.findcallers.services.CallManagerServices.SIM_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.services.CallManagerServices;
import com.softgyan.findcallers.utils.Utils;

import java.util.Date;

public class CallReceiverTemp extends CallStateReceiverTemp {
    private static final String TAG = "CallReceiverTemp";

    @Override
    protected void onCallStart(Context context, String mobNumber, Date startCallDate, int simId, boolean isOutGoing) {
        Log.d(TAG, "onCallStart: incoming ..." + mobNumber);

        Intent intent = new Intent(context, CallManagerServices.class);
        intent.putExtra(MOBILE_NUMBER, mobNumber);
        intent.putExtra(INCOMING_DATE, startCallDate);
        intent.putExtra(SIM_ID, simId);
        intent.putExtra(CallManagerServices.IS_OUT_GOING, isOutGoing);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        ContextCompat.startForegroundService(context, intent);
                    }
                }, 0
        );
    }

    @Override
    protected void onCallReceive(Context context, String mobNumber, boolean isOutGoing) {

    }

    @Override
    protected void onCallEnd(Context context, String mobNumber, boolean isOutGoing, Date endCallDate, int simId) {
        Toast.makeText(context, "Bye Bye" + mobNumber, Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCallEnded: bye bye : " + mobNumber);

        Intent intent = new Intent();
        intent.setAction(CallManagerServices.CALL_NOTIFIER_RECEIVER_ACTION);
        intent.putExtra(CallManagerServices.CALL_KEY, CallManagerServices.END_CALL_CODE);
        context.sendBroadcast(intent);
    }
}
