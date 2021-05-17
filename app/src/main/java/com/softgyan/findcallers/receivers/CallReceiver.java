package com.softgyan.findcallers.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.softgyan.findcallers.temp.TempCallActivity;

import java.util.Date;

public class CallReceiver extends CallStateReceiver {
    private static final String TAG = "my_tag";


    @Override
    protected void onCallStart(final Context ctx, final String number, final Date start, final int callSimId) {
        Toast.makeText(ctx, " Incoming Call " + number, Toast.LENGTH_LONG).show();

        Log.d(TAG, "onCallStart: incoming ..." + number);
        //        new Handler().postDelayed(() -> {
//            Intent intent = new Intent(ctx, BackgroundCallServices.class);
//            intent.putExtra(BackgroundCallServices.CALL_NUMBER, number);
//            intent.putExtra(BackgroundCallServices.CALL_DATE, start);
//            intent.putExtra(BackgroundCallServices.CALL_SIM_ID, callSimId);
//            intent.putExtra(BackgroundCallServices.CALL_TYPE, CommVar.INCOMING_TYPE);
//            intent.putExtra("callState", BackgroundCallServices.callStart);
//            ctx.startService(intent);
//        }, 2000);
    }

    @Override
    protected void onCallEnded(Context ctx, String number, Date end, boolean isIncoming) {
        Toast.makeText(ctx, "Bye Bye" + number, Toast.LENGTH_LONG).show();

//        Intent intent = new Intent(ctx, BackgroundCallServices.class);
//        intent.putExtra("callState", BackgroundCallServices.callEnd);
//        ctx.startService(intent);
        Log.d(TAG, "onCallEnded: bye bye : "+number);

    }

    @Override
    protected void onCallReceived(Context context, String number, boolean isIncoming) {
        Toast.makeText(context, "Received " + number, Toast.LENGTH_LONG).show();
    }


}
