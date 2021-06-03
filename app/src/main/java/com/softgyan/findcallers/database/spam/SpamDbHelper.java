package com.softgyan.findcallers.database.spam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SpamDbHelper extends SQLiteOpenHelper {
    public static final String SPAM_DB = "SpamDb.db";
    private static final int SPAM_DB_VERSION = 1;

    public SpamDbHelper(@Nullable Context context) {
        super(context, SPAM_DB, null, SPAM_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        final String DROP_BLOCK_TABLE = "DROP TABLE IF EXISTS " + SpamContract.BLOCK_LIST_TABLE;
        db.execSQL(DROP_BLOCK_TABLE);
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        final String blockListTable = "CREATE TABLE " + SpamContract.BLOCK_LIST_TABLE + " ( " +
                SpamContract.BLOCK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SpamContract.BLOCK_COLUMN_NUMBER + " TEXT NOT NULL, " +
                SpamContract.BLOCK_COLUMN_BLOCK_TYPE + " INTEGER NOT NULL, " +
                SpamContract.BLOCK_COLUMN_NAME + " TEXT " +
                ");";
        db.execSQL(blockListTable);
    }
}
