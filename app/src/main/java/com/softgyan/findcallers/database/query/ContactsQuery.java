package com.softgyan.findcallers.database.query;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.contacts.ContactContracts.ContactsDetails;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ContactsQuery {


    private static final String TAG = ContactsQuery.class.getName();

    private synchronized static int insertContacts(@NonNull Context context, final ContentValues contactValues) {
        final Uri insertUri = context.getContentResolver().insert(ContactsDetails.CONTENT_USER_URI, contactValues);
        if (insertUri != null) {
            final long l = ContentUris.parseId(insertUri);
            return (int) l;
        }
        return CommVar.FAILED;
    }


    private synchronized static int insertMobileNumber(@NonNull Context context, final ContentValues numberValues) {
        final Uri insertUri = context.getContentResolver().insert(ContactsDetails.CONTENT_MOBILE_URI, numberValues);
        if (insertUri != null) {
            final long l = ContentUris.parseId(insertUri);
            return (int) l;
        }
        return CommVar.FAILED;
    }


    public synchronized static int insertContactsDetails(@NonNull Context context, @NonNull final ContactModel contactModel) {
        int numberListSize = contactModel.getContactNumbers().size();
        if (numberListSize == 0) {
            return CommVar.INVALID_INDEX;
        }
        final String userName;

        if (contactModel.getName() == null) {
            userName = "Contact_" + contactModel.getContactNumbers().get(0).getMobileNumber();
            contactModel.setName(userName);
        } else {
            userName = contactModel.getName();
        }

        //contactValues
        final ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsDetails.COLUMN_USER_NAME, userName);
        if (contactModel.getAddress() != null) {
            contentValues.put(ContactsDetails.COLUMN_USER_ADDRESS, contactModel.getAddress());
        }
        if (contactModel.getEmailId() != null) {
            contentValues.put(ContactsDetails.COLUMN_USER_EMAIL_ID, contactModel.getEmailId());
        }

        if (contactModel.getImage() != null) {
            contentValues.put(ContactsDetails.COLUMN_USER_IMAGE, contactModel.getImage());
        }
        if (contactModel.getDefaultNumber() != null) {
            contentValues.put(ContactsDetails.COLUMN_DEFAULT_NUMBER, contactModel.getDefaultNumber());
        }
        if (contactModel.getTag() != null) {
            contentValues.put(ContactsDetails.COLUMN_USER_TAG, contactModel.getTag());
        }
        final int insertResult = insertContacts(context, contentValues);
        if (insertResult == CommVar.FAILED) {
            Log.d(TAG, "insertContactsDetails: Failed to save");
            return CommVar.FAILED;
        }
        contactModel.setId(insertResult);


        //content values for mobile numbers;

        for (int i = 0; i < contactModel.getContactNumbers().size(); i++) {
            contentValues.clear();
            ContactNumberModel contactNumberModel = contactModel.getContactNumbers().get(i);
            contactNumberModel.setUserRefId(insertResult);

            if (contactNumberModel.getMobileNumber() == null) {
                continue;
            }
            contentValues.put(ContactsDetails.COLUMN_MOBILE_NUMBER, contactNumberModel.getMobileNumber());
            contentValues.put(ContactsDetails.COLUMN_USER_REF_ID, insertResult);

            final int insertMobileNumberResult = insertMobileNumber(context, contentValues);
            if (insertMobileNumberResult == CommVar.FAILED) {
                continue;
            }
            contactNumberModel.setNumberId(insertMobileNumberResult);
            Log.d(TAG, "insertContactsDetails: saved data : " + contactModel.toString());

        }

        return CommVar.SUCCESS;

    }

    public synchronized static List<ContactModel> getContactModels(@NonNull Context context) {
        Log.d(TAG, "getContactModels: getting from local database");
        final List<ContactModel> contactList = new ArrayList<>();
        final Cursor contactQuery = context.getContentResolver().query(ContactsDetails.CONTENT_USER_URI,
                null, null,
                null, null);
        if (contactQuery.getCount() == 0) {
            return null;
        }

        while (contactQuery.moveToNext()) {
            final String name = contactQuery.getString(contactQuery.getColumnIndex(ContactsDetails.COLUMN_USER_NAME));
            ContactNumberModel numberModel = getCursorFromContactNumberModel(contactQuery);

            boolean flag = false;
            for (ContactModel m : contactList) {
                if (name.equals(m.getName())) {
                    m.setContactNumbers(numberModel);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                ContactModel model = getCursorFromContactModel(contactQuery);
                model.setContactNumbers(numberModel);
                contactList.add(model);
            }
        }

        contactQuery.close();
        return contactList;

    }


    public synchronized static ContactModel searchByNumber(@NonNull Context context, final String number) {
        String tempNumber = Utils.trimNumber(number);
        String selection = "WHERE " + ContactsDetails.COLUMN_MOBILE_NUMBER + "=?";
        final Cursor searchQuery = context.getContentResolver().query(ContactsDetails.CONTENT_USER_URI,
                null, selection, new String[]{tempNumber}, null);

        if (searchQuery.getCount() == 0) {
            return null;
        }
        searchQuery.moveToFirst();

        ContactNumberModel numberModel = getCursorFromContactNumberModel(searchQuery);

        ContactModel model = getCursorFromContactModel(searchQuery);
        model.setContactNumbers(numberModel);

        ContactModel cModel = new ContactModel("dfds");
        searchQuery.close();
        return cModel;
    }


    private static ContactNumberModel getCursorFromContactNumberModel(final Cursor cursor) {
        return new ContactNumberModel(
                cursor.getInt(cursor.getColumnIndex(ContactsDetails.COLUMN_MOBILE_ID)),
                cursor.getInt(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_REF_ID)),
                cursor.getString(cursor.getColumnIndex(ContactsDetails.MOBILE_NUMBER_TABLE))

        );
    }

    private static ContactModel getCursorFromContactModel(final Cursor cursor) {
        final String name = cursor.getString(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_NAME));
        ContactModel model = new ContactModel(name);
        model.setId(cursor.getInt(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_ID)));
        model.setImage(cursor.getString(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_IMAGE)));
        model.setEmailId(cursor.getString(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_EMAIL_ID)));
        model.setAddress(cursor.getString(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_ADDRESS)));
        model.setTag(cursor.getString(cursor.getColumnIndex(ContactsDetails.COLUMN_USER_TAG)));
        return model;
    }

}
