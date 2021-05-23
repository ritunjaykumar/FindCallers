package com.softgyan.findcallers.database.query;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.call.CallContract.CallDetails;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallNumberModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public final class CallQuery {
    private static final List<CallModel> CALL_NUMBER_MODEL_LIST = new ArrayList<>();
    private static final String TAG = CallQuery.class.getName();

    private synchronized static void getAllCallLog(@Nullable Context context) {
        if (context == null) {
            return;
        }

        final Cursor query = context.getContentResolver()
                .query(CallDetails.CONTENT_CALL_URI, null, null, null, CallDetails.CALL_COLUMN_DATE);
        while (query.moveToNext()) {

            int id = query.getInt(query.getColumnIndex(CallDetails.CALL_COLUMN_ID));
            int refId = query.getInt(query.getColumnIndex(CallDetails.CALL_COLUMN_NAME_REF_ID));
            int cacheId = query.getInt(query.getColumnIndex(CallDetails.CACHE_NAME_ID));
            String name = query.getString(query.getColumnIndex(CallDetails.CACHE_NAME));
            String number = query.getString(query.getColumnIndex(CallDetails.CALL_COLUMN_NUMBER));
            String date = query.getString(query.getColumnIndex(CallDetails.CALL_COLUMN_DATE));
            Date callDate = new Date(Long.parseLong(date));
            long dur = query.getLong(query.getColumnIndex(CallDetails.CALL_COLUMN_DURATION));
            String sim_id = query.getString(query.getColumnIndex(CallDetails.CALL_COLUMN_SUBSCRIPTION_ID));
            int call_type = query.getInt(query.getColumnIndex(CallDetails.CALL_COLUMN_TYPE));


            CallNumberModel callNumberModel = new CallNumberModel(
                    id, refId, number, callDate, call_type, sim_id, -1, (int) dur
            );
            boolean flag = true;
            for (CallModel callModel : CALL_NUMBER_MODEL_LIST) {
                if (callModel.getNameId() == refId) {
                    callModel.setCallNumber(callNumberModel);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                CallModel callModel = new CallModel(cacheId, name, callNumberModel);
                CALL_NUMBER_MODEL_LIST.add(callModel);
            }

        }
        query.close();
    }

    public static List<CallModel> getCallList(Context context) {
        if (CALL_NUMBER_MODEL_LIST.size() == 0) {
            getAllCallLog(context);
        }
        return CALL_NUMBER_MODEL_LIST;
    }

    private synchronized static int insertCallNumberLog(@Nullable Context context, final CallNumberModel callNumberModel) {
        if (context != null) {
            ContentValues values = getContentValues(callNumberModel);
            if (values == null) return CommVar.INVALID_ARG;

            final Uri insert = context.getContentResolver().insert(CallDetails.CONTENT_CALL_URI, values);
            if(insert == null){
                return CommVar.FAILED;
            }
            final long l = ContentUris.parseId(insert);
            return (int) l;
        } else return CommVar.FAILED;
    }

    public synchronized static void insertCallLog(@NonNull Context context, final CallModel callModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CallDetails.CACHE_NAME, callModel.getCacheName());
        final Uri insertUri = context.getContentResolver().insert(CallDetails.CONTENT_CACHE_NAME_URI, contentValues);
        if(insertUri == null){
            return;
        }
        final int insertResult = (int) ContentUris.parseId(insertUri);
        if (insertResult > -1) {
            callModel.setNameId(insertResult);
//            Log.d(TAG, "insertCallLog: call Name table : inserted");
            for (final CallNumberModel numberModel : callModel.getCallNumberList()) {
                numberModel.setNameRefId(insertResult);
                final int callResult = insertCallNumberLog(context, numberModel);
                if (callResult > -1) {
                    numberModel.setCallModelId(callResult);
//                    Log.d(TAG, "insertCacheName: inserted : " + numberModel);
                } else {
//                    Log.d(TAG, "insertCacheName: not inserted : " + numberModel);
                }
            }
        }
    }

    private static ContentValues getContentValues(final CallNumberModel callNumberModel) {
        if (callNumberModel == null) return null;
        ContentValues cValues = new ContentValues();
        cValues.put(CallDetails.CALL_COLUMN_NAME_REF_ID, callNumberModel.getNameRefId());
        cValues.put(CallDetails.CALL_COLUMN_NUMBER, callNumberModel.getNumber());
        cValues.put(CallDetails.CALL_COLUMN_DATE, callNumberModel.getDate().getTime());
        cValues.put(CallDetails.CALL_COLUMN_TYPE, callNumberModel.getType());
        cValues.put(CallDetails.CALL_COLUMN_DURATION, callNumberModel.getDuration());
        cValues.put(CallDetails.CALL_COLUMN_SUBSCRIPTION_ID, callNumberModel.getIccId());
        return cValues;

    }


    public synchronized static int deleteSingleCallLog(@Nullable Context context, final int id) {
        if (context == null) {
            return 0;
        }
        return context.getContentResolver().delete(ContentUris.withAppendedId(CallDetails.CONTENT_CALL_URI, id),
                null, null);
    }

    public synchronized static int deleteAllCallLog(@Nullable Context context, @NonNull String number) {
        if (context == null) {
            return 0;
        }
        String selection = CallDetails.CALL_COLUMN_NUMBER + " =?";
        String[] selectionArgs = new String[]{number};
        return context.getContentResolver().delete(CallDetails.CONTENT_CALL_URI, selection, selectionArgs);
    }
}
