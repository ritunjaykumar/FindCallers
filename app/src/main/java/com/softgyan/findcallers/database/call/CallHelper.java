package com.softgyan.findcallers.database.call;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.softgyan.findcallers.database.call.CallContract.CallDetails;

public class CallHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FindCallersContact.DB";
    private static final String TAG = CallHelper.class.getName();


    public CallHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        tableQuery(db);
        Log.d(TAG, "onCreate: table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_CALL_HISTORY_TABLE = "DROP TABLE IF EXISTS " + CallContract.CallDetails.CALL_HISTORY_TABLE;
        final String DROP_CACHE_NAME_TABLE = "DROP TABLE IF EXISTS " + CallContract.CallDetails.CACHE_NAME_TABLE;
        db.execSQL(DROP_CALL_HISTORY_TABLE);
        db.execSQL(DROP_CACHE_NAME_TABLE);
        onCreate(db);
        Log.d(TAG, "onUpgrade: " + String.format("table is upgrading from %d to %d ", oldVersion, newVersion));
    }

    private void tableQuery(final SQLiteDatabase database) {
        final String callNameTable = "CREATE TABLE " + CallDetails.CACHE_NAME_TABLE + " ( " +
                CallDetails.CACHE_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallDetails.CACHE_NAME + " INTEGER NOT NULL " +
                ")";

        final String callTable = "CREATE TABLE " + CallDetails.CALL_HISTORY_TABLE + " ( " +
                CallDetails.CALL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallDetails.CALL_COLUMN_NAME_REF_ID + " INTEGER NOT NULL, " +
                CallDetails.CALL_COLUMN_NUMBER + " TEXT NOT NULL, " +
                CallDetails.CALL_COLUMN_DATE + " TEXT NOT NULL, " +
                CallDetails.CALL_COLUMN_TYPE + " INTEGER NOT NULL, " +
                CallDetails.CALL_COLUMN_DURATION + " INTEGER NOT NULL, " +
                CallDetails.CALL_COLUMN_SUBSCRIPTION_ID + " INTEGER NOT NULL, " +
//                CallDetails.CALL_COLUMN_SUBSCRIPTION_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + CallDetails.CALL_COLUMN_NAME_REF_ID + ") REFERENCES " + CallDetails.CACHE_NAME_TABLE + "(" + CallDetails.CACHE_NAME_ID + ")" +
                ")";


        // for supporting foreign key
        final String supportForeignKey = "PRAGMA foreign_keys = ON;";

        database.execSQL(callNameTable);
        Log.d(TAG, "tableQuery: call table created : name");
        database.execSQL(callTable);
        Log.d(TAG, "tableQuery: call table created : number");


        //for enable foreign key supporting
        database.execSQL(supportForeignKey);
    }
}
