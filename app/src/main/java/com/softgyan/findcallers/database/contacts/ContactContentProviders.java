package com.softgyan.findcallers.database.contacts;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.softgyan.findcallers.database.contacts.ContactContracts.ContactsDetails;
import com.softgyan.findcallers.utils.Utils;

public class ContactContentProviders extends ContentProvider {
    private static final int USER = 100;
    private static final int USER_ID = 101;
    private static final int MOBILE = 102;
    private static final int MOBILE_ID = 103;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = ContactContentProviders.class.getName();

    static {
        sUriMatcher.addURI(IContactContract.CONTENT_AUTHORITY, ContactsDetails.PATH_USER, USER);
        sUriMatcher.addURI(IContactContract.CONTENT_AUTHORITY, ContactsDetails.PATH_USER + "/#", USER_ID);

        sUriMatcher.addURI(IContactContract.CONTENT_AUTHORITY, ContactsDetails.PATH_MOBILE, MOBILE);
        sUriMatcher.addURI(IContactContract.CONTENT_AUTHORITY, ContactsDetails.PATH_MOBILE + "/#", MOBILE_ID);


    }


    private ContactHelper contactHelper;

    @Override
    public boolean onCreate() {
        contactHelper = new ContactHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER: {
                return insertUser(uri, values);
            }
            case MOBILE: {
                return insertMobile(uri, values);
            }
            default: {
                throw new UnsupportedOperationException("invalid uri : " + uri);
            }
        }
    }

    private Uri insertUser(Uri uri, ContentValues values) {
        final String userName = values.getAsString(ContactsDetails.COLUMN_USER_NAME);
        if (userName == null) {
            throw new IllegalArgumentException("invalid user user name");
        }
        final SQLiteDatabase database = contactHelper.getWritableDatabase();
        long insert = database.insert(ContactsDetails.TABLE_NAME, null, values);
        if (insert == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri, insert);
    }

    private Uri insertMobile(Uri uri, ContentValues values) {
        try {
            Integer userId = values.getAsInteger(ContactsDetails.COLUMN_USER_REF_ID);
            if (userId == null) {
                throw new IllegalArgumentException("invalid user Id");
            }
            String mobileNo = values.getAsString(ContactsDetails.COLUMN_MOBILE_NUMBER);
            if (mobileNo == null) {
                throw new IllegalArgumentException("invalid mobile number");
            }
            SQLiteDatabase database = contactHelper.getWritableDatabase();
            long id = database.insert(ContactsDetails.MOBILE_NUMBER_TABLE, null, values);
            if (id == -1) {
                Log.e(TAG, "Failed to insert row for " + uri);
                return null;
            }
            return ContentUris.withAppendedId(uri, id);
        } catch (Exception e) {
            return null;
        }
    }



    /*delete operation start from here*/

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = contactHelper.getReadableDatabase();
        switch (match) {
            case USER: {
                if (projection == null && selection == null) {
                    String sql = String.format("SELECT * FROM %s u INNER JOIN %s m ON u.%s = m.%s ORDER BY %s ASC;",
                            ContactsDetails.TABLE_NAME, ContactsDetails.MOBILE_NUMBER_TABLE,
                            ContactsDetails.COLUMN_USER_ID, ContactsDetails.COLUMN_USER_REF_ID, ContactsDetails.COLUMN_USER_NAME);

                    return database.rawQuery(sql, selectionArgs);
                    //for searching number
                } else if (selectionArgs != null && selection != null) {
                    int selSize = selectionArgs.length;
                    int selArgSize = Utils.getPlaceHolderCount(selection);
                    if (selSize == selArgSize) {
                        String sql = String.format("SELECT * FROM %s u INNER JOIN %s m ON u.%s = m.%s %s;",
                                ContactsDetails.TABLE_NAME, ContactsDetails.MOBILE_NUMBER_TABLE,
                                ContactsDetails.COLUMN_USER_ID, ContactsDetails.COLUMN_USER_REF_ID, selection);
                        return database.rawQuery(sql, selectionArgs);
                    }
                }

            }
        }

        throw new UnsupportedOperationException("invalid uri : " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}