package com.softgyan.findcallers.database.spam;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.Locale;

public class SpamNumberProvider extends ContentProvider {

    private static final String TAG = "SpamNumberProvider";
    private static final int SPAM = 102;
    private static final int SPAM_ID = 103;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ISpamContract.SPAM_CONTENT_AUTHORITY, SpamContract.PATH_BLOCK, SPAM);
        sUriMatcher.addURI(ISpamContract.SPAM_CONTENT_AUTHORITY, SpamContract.PATH_BLOCK + "/#", SPAM_ID);
    }

    private SpamDbHelper mSpamDb;

    public SpamNumberProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SPAM_ID: {
                int id = (int) ContentUris.parseId(uri);
                selection = SpamContract.BLOCK_ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                return deleteBlockList(selection, selectionArgs);
            }
            case SPAM: {
                return deleteBlockList(selection, selectionArgs);
            }
        }
        throw new UnsupportedOperationException("invalid operation for uri : " + uri);
    }

    private int deleteBlockList(String selection, String[] selectionArgs) {
        SQLiteDatabase database = mSpamDb.getWritableDatabase();
        return database.delete(SpamContract.BLOCK_LIST_TABLE, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sUriMatcher.match(uri);
        if (match == SPAM) {
            return insertBlockList(uri, values);
        }
        throw new UnsupportedOperationException("invalid operation for uri : " + uri);
    }

    private Uri insertBlockList(Uri uri, ContentValues values) {

        if (values.getAsString(SpamContract.BLOCK_COLUMN_NAME) == null) {
            throw new IllegalArgumentException("invalid name");
        }

        if (values.getAsString(SpamContract.BLOCK_COLUMN_NUMBER) == null) {
            throw new IllegalArgumentException("invalid number");
        }

        try {
            final int type = values.getAsInteger(SpamContract.BLOCK_COLUMN_BLOCK_TYPE);
            if (type != SpamContract.BLOCK_TYPE && type != SpamContract.SPAM_TYPE) {
                throw new IllegalArgumentException("invalid type"); //todo see logic
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("invalid type");
        }


        final SQLiteDatabase database = mSpamDb.getWritableDatabase();
        long insert = database.insert(SpamContract.BLOCK_LIST_TABLE, null, values);
        if (insert == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, insert);
    }

    @Override
    public boolean onCreate() {
        mSpamDb = new SpamDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mSpamDb.getWritableDatabase();
        switch (match) {
            case SPAM: {
                String call_sql_id = String.format(Locale.getDefault(), "SELECT * FROM %S ",
                        SpamContract.BLOCK_LIST_TABLE);
                if (selection != null && selectionArgs != null) {
                    call_sql_id = call_sql_id + " " + selection;
                }
                if (sortOrder != null) {
                    call_sql_id = call_sql_id + sortOrder;
                }

                return database.rawQuery(call_sql_id, selectionArgs);

            }
            case SPAM_ID: {
                int id = (int) ContentUris.parseId(uri);
                String call_sql_id = String.format(Locale.getDefault(), "SELECT * FROM %S WHERE %s = %d",
                        SpamContract.BLOCK_LIST_TABLE, SpamContract.BLOCK_COLUMN_NUMBER, id);
                return database.rawQuery(call_sql_id, selectionArgs);
            }
        }

        throw new UnsupportedOperationException("invalid uri : " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        /*do not perform any update perform for BlockList table*/
        throw new UnsupportedOperationException("Not yet implemented");
    }
}