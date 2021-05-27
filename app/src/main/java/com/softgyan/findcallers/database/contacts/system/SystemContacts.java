package com.softgyan.findcallers.database.contacts.system;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public final class SystemContacts {

    public static final Uri CONTACT_BASE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    public static final String ID = ContactsContract.CommonDataKinds.Phone._ID;
    public static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    public static final String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String TAG = SystemContacts.class.getName();
    private static final String PHOTO_URI = ContactsContract.CommonDataKinds.Photo.PHOTO_URI;

    private final static List<ContactModel> contactsList = new ArrayList<>();
    public final static List<UploadContactModel> uploadContactList = new ArrayList<>();

    private static void getContactsList(final Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Read and write contacts permission is denied", Toast.LENGTH_SHORT).show();
            return;
        }


        String[] projection = new String[]{ID, DISPLAY_NAME, NUMBER, PHOTO_URI};

        Cursor people = context.getContentResolver().query(CONTACT_BASE_URI, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int indexId = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
        int indexPhoto = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);

        while (people.moveToNext()) {
            String name = people.getString(indexName);
            String number = Utils.trimNumber(people.getString(indexNumber));
            int id = people.getInt(indexId);
            final String imageUri = people.getString(indexPhoto);

            // for upload contact on server =>start
            UploadContactModel uploadContactModel = new UploadContactModel(
                    null, number, null, null, name, false
            );

            uploadContactList.add(uploadContactModel);

            //=> end


            //adding contacts to main list

            ContactNumberModel mobile = new ContactNumberModel(number);

            boolean flag = false;

            for (ContactModel m : contactsList) {
                if (name.equals(m.getName())) {
                    m.setContactNumbers(mobile);
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                ContactModel model = new ContactModel(name);
                model.setContactNumbers(mobile);
                model.setImage(imageUri);
                contactsList.add(model);
            }

        }
        Log.d(TAG, "getContactsList: got all contacts");
        people.close();

    }

    public static List<ContactModel> getSystemContactsList(Context context) {
        if (contactsList.size() == 0) {
            getContactsList(context);
        }
        return contactsList;
    }


}
