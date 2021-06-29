package com.softgyan.findcallers.database.contacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.softgyan.findcallers.database.call.CallContract;
import com.softgyan.findcallers.database.contacts.ContactContracts.ContactsDetails;

public class ContactHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FindCallersContact.DB";
    private static final String TAG = ContactHelper.class.getName();

    public ContactHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
        Log.d(TAG, "onCreate: table created");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTable(db);
        Log.d(TAG, "onUpgrade: " + String.format("table is upgrading from %d to %d ", oldVersion, newVersion));
        onCreate(db);
    }


    private void createAllTables(SQLiteDatabase db) {
        final String userTable = "CREATE TABLE " + ContactsDetails.TABLE_NAME + " ( " +
                ContactsDetails.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContactsDetails.COLUMN_USER_NAME + " TEXT, " +
                ContactsDetails.COLUMN_USER_ADDRESS + " TEXT, " +
                ContactsDetails.COLUMN_USER_EMAIL_ID + " TEXT, " +
                ContactsDetails.COLUMN_USER_TAG + " TEXT, " +
                ContactsDetails.COLUMN_USER_IMAGE + " TEXT" +
                ");";

        final String mobileTable = "CREATE TABLE " + ContactsDetails.MOBILE_NUMBER_TABLE + " ( " +
                ContactsDetails.COLUMN_MOBILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContactsDetails.COLUMN_USER_REF_ID + " INTEGER NOT NULL, " +
                ContactsDetails.COLUMN_MOBILE_NUMBER + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + ContactsDetails.COLUMN_USER_REF_ID + ") REFERENCES " + ContactsDetails.MOBILE_NUMBER_TABLE + "(" + ContactsDetails.COLUMN_USER_ID + ")" +
                ");";

        final String callNameTable = "CREATE TABLE " + CallContract.CallDetails.CACHE_NAME_TABLE + " ( " +
                CallContract.CallDetails.CACHE_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallContract.CallDetails.CACHE_NAME + " TEXT " +
                ")";

        final String callTable = "CREATE TABLE " + CallContract.CallDetails.CALL_HISTORY_TABLE + " ( " +
                CallContract.CallDetails.CALL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallContract.CallDetails.CALL_COLUMN_NAME_REF_ID + " INTEGER NOT NULL, " +
                CallContract.CallDetails.CALL_COLUMN_NUMBER + " TEXT NOT NULL, " +
                CallContract.CallDetails.CALL_COLUMN_DATE + " TEXT NOT NULL, " +
                CallContract.CallDetails.CALL_COLUMN_TYPE + " INTEGER NOT NULL, " +
                CallContract.CallDetails.CALL_COLUMN_DURATION + " INTEGER NOT NULL, " +
                CallContract.CallDetails.CALL_COLUMN_SUBSCRIPTION_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + CallContract.CallDetails.CALL_COLUMN_NAME_REF_ID + ") REFERENCES " + CallContract.CallDetails.CACHE_NAME_TABLE + "(" + CallContract.CallDetails.CACHE_NAME_ID + ")" +
                ")";


        final String supportForeignKey = "PRAGMA foreign_keys = ON;";
        db.execSQL(supportForeignKey);
        //for user table
        db.execSQL(userTable);

        db.execSQL(mobileTable);

        db.execSQL(callNameTable);
        Log.d(TAG, "tableQuery: call table created : name");
        db.execSQL(callTable);
        Log.d(TAG, "tableQuery: call table created : number");
    }

    private void dropAllTable(SQLiteDatabase db) {
        final String DROP_CONTACTS_TABLE = "DROP TABLE IF EXISTS " + ContactsDetails.TABLE_NAME;
        final String DROP_MOBILE_NUMBER_TABLE = "DROP TABLE IF EXISTS " + ContactsDetails.MOBILE_NUMBER_TABLE;


        db.execSQL(DROP_MOBILE_NUMBER_TABLE);
        db.execSQL(DROP_CONTACTS_TABLE);
    }

}
