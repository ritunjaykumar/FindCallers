package com.softgyan.findcallers.database.call.system;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallNumberModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class SystemCalls {


    private static final String TAG = SystemCalls.class.getName();
    private static final List<CallModel> callList = new ArrayList<>();

    private static void getCallTempList(Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Read and write contacts permission is denied", Toast.LENGTH_SHORT).show();
            return;
        }


        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");

        final int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        final int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        final int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        final int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        final int name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

        final int subscription_id = cursor.getColumnIndex("subscription_id");
        final int ringTime = cursor.getColumnIndex("ring_time");

        Log.d(TAG, "getCallTempList: " + Arrays.toString(cursor.getColumnNames()));
        while (cursor.moveToNext()) {


            String phNumber = Utils.trimNumber(cursor.getString(number));
            final int callType = cursor.getInt(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            long callDuration = cursor.getLong(duration);
            final String simIdCall = cursor.getString(subscription_id);
//            final int ringingTime = cursor.getInt(ringTime);
            String userName = cursor.getString(name);
            final CallNumberModel callNumberTemp = new CallNumberModel(
                    phNumber, callDayTime, callType, simIdCall, -1, callDuration
            );

            Log.d(TAG, "getCallTempList: simIdCall : " + simIdCall);

            boolean flag = true;
            for (CallModel callModel : callList) {
                if(callModel.getCallNumberList().size() != 0 && phNumber.equals(callModel.getFirstCall().getNumber())){
                    callModel.setCallNumber(callNumberTemp);
                    flag = false;
                    break;
                }

                /*if (userName.equals(callModel.getCacheName())) {
                    callModel.setCallNumber(callNumberTemp);
                    flag = false;
                    break;
                }*/

            }

            if (flag) {
                CallModel callModel = new CallModel(-1, userName, callNumberTemp);
                callList.add(callModel);
            }

        }
        cursor.close();
    }

    public static List<CallModel> getCallList(Context context) {
        if (callList.size() == 0) {
            getCallTempList(context);
        }
        return callList;
    }


    public static synchronized CallModel getLastCallHistory(Context context) {
        if (!Utils.checkPermission(context, Manifest.permission.READ_CALL_LOG))
            return null;


        Uri contacts = CallLog.Calls.CONTENT_URI;
        try {

            Cursor cursor = context.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 1;");

            final int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            final int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
            final int date = cursor.getColumnIndex(CallLog.Calls.DATE);
            final int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
            final int name = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

            final int subscription_id = cursor.getColumnIndex("subscription_id");
            final int ringTime = cursor.getColumnIndex("ring_time");

            cursor.moveToFirst();

            String phNumber = Utils.trimNumber(cursor.getString(number));
            final int callType = cursor.getInt(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            long callDuration = cursor.getLong(duration);
            final String simIdCall = cursor.getString(subscription_id);
//            final int ringingTime = cursor.getInt(ringTime);
            String userName = cursor.getString(name);

            final CallNumberModel callNumberTemp = new CallNumberModel(
                    phNumber, callDayTime, callType, simIdCall, -1, callDuration
            );
            CallModel callModel = new CallModel(-1, userName, callNumberTemp);
            callList.add(callModel);
            cursor.close();
            return callModel;

        } catch (
                SecurityException e) {
            Log.e("Security Exception", "User denied call log permission");
            return null;

        }

    }
}
