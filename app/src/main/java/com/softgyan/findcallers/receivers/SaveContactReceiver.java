package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.softgyan.findcallers.database.contacts.system.SystemContacts;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.models.ContactModel;

public class SaveContactReceiver extends BroadcastReceiver {
    private final static String SAVE_MODE = "saveMode";
    private final static int SYSTEM_CONTACT = 1;
    private final static int LOCAL_CONTACT = 2;
    private final static int BOTH = 3;
    public static final String NAME = "name";
    public static final String EMAIL_ID = "emailId";
    public static final String ADDRESS = "address";
    public static final String IMAGE = "image";
    public static final String TAG = "tag";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        ContactModel contactModel;
        String name = intent.getStringExtra(NAME);
        if (name == null) {
            contactModel = new ContactModel("unknown person");
        } else {
            contactModel = new ContactModel(name);
        }
        contactModel.setEmailId(EMAIL_ID);
        contactModel.setAddress(ADDRESS);
        contactModel.setImage(IMAGE);
        contactModel.setTag(TAG);

        int saveMode = intent.getIntExtra(SAVE_MODE, 0);
        switch (saveMode) {
            case SYSTEM_CONTACT: {
                saveSystemContact(context, contactModel);
                break;
            }
            case LOCAL_CONTACT: {
                saveLocalContact(context, contactModel);
                break;
            }
            case BOTH: {
                saveSystemContact(context, contactModel);
                saveLocalContact(context, contactModel);
                break;
            }
        }

    }

    private void saveLocalContact(Context context, ContactModel contactModel) {
        final int i = ContactsQuery.insertContactsDetails(context, contactModel);
        if (i == 1) {
            Toast.makeText(context, "contact saved : " + contactModel.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "contact not saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSystemContact(Context context, ContactModel contactModel) {
        SystemContacts.saveContactToSystem(context, contactModel);
    }
}
