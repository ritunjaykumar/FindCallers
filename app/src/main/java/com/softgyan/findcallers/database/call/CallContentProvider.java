package com.softgyan.findcallers.database.call;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.softgyan.findcallers.database.call.CallContract.CallDetails;
import com.softgyan.findcallers.utils.Utils;

public class CallContentProvider extends ContentProvider {

    private static final int CACHE_NAME = 102;
    private static final int CACHE_NAME_ID = 103;

    private static final int CALL = 104;
    private static final int CALL_ID = 105;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String TAG = CallContentProvider.class.getName();

    static {
        sUriMatcher.addURI(ICallContract.CALL_CONTENT_AUTHORITY, CallDetails.PATH_CALL, CALL);
        sUriMatcher.addURI(ICallContract.CALL_CONTENT_AUTHORITY, CallDetails.PATH_CALL + "/#", CALL_ID);

        sUriMatcher.addURI(ICallContract.CALL_CONTENT_AUTHORITY, CallDetails.PATH_CACHE_NAME, CACHE_NAME);
        sUriMatcher.addURI(ICallContract.CALL_CONTENT_AUTHORITY, CallDetails.PATH_CACHE_NAME + "/#", CACHE_NAME_ID);
    }


    private CallHelper callHelper;

    public CallContentProvider() {
    }

    @Override
    public boolean onCreate() {
        callHelper = new CallHelper(getContext());
        return false;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CALL_ID: {
                int id = (int) ContentUris.parseId(uri);
                selection = CallDetails.CALL_COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(id)};
                return deleteCallLog(selection, selectionArgs);
            }
            case CALL: {
                return deleteCallLog(selection, selectionArgs);
            }
            default: {
                throw new UnsupportedOperationException("unable to delete for uri" + uri);
            }
        }
    }

    private int deleteCallLog(String selection, String[] selectionArgs) {
        SQLiteDatabase database = callHelper.getWritableDatabase();
        return database.delete(CallDetails.CALL_HISTORY_TABLE, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = callHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long insert;
        switch (match) {
            case CACHE_NAME: {
                insert = database.insert(CallDetails.CACHE_NAME_TABLE, null, values);
                break;
            }
            case CALL: {
                insert = database.insert(CallDetails.CALL_HISTORY_TABLE, null, values);
                break;
            }
            default: {
                throw new UnsupportedOperationException("unable to fetch data for uri : " + uri.toString());
            }
        }
        if (insert == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, insert);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = callHelper.getReadableDatabase();
        switch (match) {

            case CALL: {

                if (selection != null && selectionArgs != null) {
                    if (selectionArgs.length == Utils.getPlaceHolderCount(selection)) {
                        String sql = String.format("SELECT * FROM %s u INNER JOIN %s m ON u.%s = m.%s ",
                                CallDetails.CACHE_NAME_TABLE, CallDetails.CALL_HISTORY_TABLE,
                                CallDetails.CACHE_NAME_ID, CallDetails.CALL_COLUMN_NAME_REF_ID);

                        sql = sql+ selection;
                        return database.rawQuery(sql, selectionArgs);
                    }
                    throw new UnsupportedOperationException("invalid query : " + uri.toString());
                }


                String call_sql = String.format("SELECT * FROM %s u INNER JOIN %s m ON u.%s = m.%s ORDER BY %S DESC;",
                        CallDetails.CACHE_NAME_TABLE, CallDetails.CALL_HISTORY_TABLE, CallDetails.CACHE_NAME_ID,
                        CallDetails.CALL_COLUMN_NAME_REF_ID, CallDetails.CALL_COLUMN_DATE);

                /*if (selectionArgs != null && selectionArgs.length == 1) {
                    call_sql = String.format("SELECT * FROM %s WHERE %s = %s",
                            FindCallerEntry.CALL_HISTORY_TABLE, FindCallerEntry.CALL_COLUMN_NUMBER, selectionArgs[0]);
                    call_sql = String.format("SELECT * FROM %s WHERE %s = ?",
                            CallDetails.CALL_HISTORY_TABLE, CallDetails.CALL_COLUMN_NUMBER);
                }*/

                return database.rawQuery(call_sql, null);

            }

            case CALL_ID: {
                int id = (int) ContentUris.parseId(uri);
                /*String call_sql_id = String.format(Locale.getDefault(), "SELECT * FROM %S WHERE %s = %d",
                        CallDetails.CALL_HISTORY_TABLE, CallDetails.CALL_COLUMN_ID, id);*/

                String call_sql = String.format("SELECT * FROM %s u INNER JOIN %s m ON u.%s = m.%s WHERE %s = %s ORDER BY %S ASC;",
                        CallDetails.CACHE_NAME_TABLE, CallDetails.CALL_HISTORY_TABLE, CallDetails.CACHE_NAME_ID,
                        CallDetails.CALL_COLUMN_NAME_REF_ID, CallDetails.CALL_COLUMN_ID, id, CallDetails.CALL_COLUMN_DATE);

                return database.rawQuery(call_sql, null);
            }
            default: {
                throw new UnsupportedOperationException("unable to fetch data for uri : " + uri.toString());
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_NAME_ID: {
                SQLiteDatabase database = callHelper.getWritableDatabase();
                final long id = ContentUris.parseId(uri);

                selection = String.format("WHERE %s = ?", CallDetails.CACHE_NAME);
                selectionArgs = new String[]{String.valueOf(id)};

                return database.update(CallDetails.CALL_HISTORY_TABLE, values, selection, selectionArgs);
            }
            default: {
                throw new UnsupportedOperationException("invalid uri : " + uri);
            }
        }
    }
}