package com.softgyan.findcallers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.softgyan.findcallers.database.contacts.system.SystemContacts;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.database.query.SpamQuery;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.models.BlockNumberModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;

public class SaveContactReceiver extends BroadcastReceiver {
    public final static String MODE = "saveMode";
    private final static int SYSTEM_CONTACT = 1;
    private final static int LOCAL_CONTACT = 2;
    private final static int BOTH = 3;
    public final static int BLOCK_NUMBER = 4;
    public final static String NUMBER_KEY = "mobileNumber";
    public static final String NAME = "name";
    public static final String EMAIL_ID = "emailId";
    public static final String ADDRESS = "address";
    public static final String IMAGE = "image";
    public static final String TAG = "SaveContactReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: called");
        if (intent == null) return;
        ContactModel contactModel;

        String name = intent.getStringExtra(NAME);
        if (name == null) {
            contactModel = new ContactModel("unknown_");
        } else {
            contactModel = new ContactModel(name);
        }

        String strNumber = intent.getStringExtra(NUMBER_KEY);
        if(strNumber == null){
            return;
        }

        contactModel.setContactNumbers(new ContactNumberModel(strNumber));


        String strEmail = intent.getStringExtra(EMAIL_ID);
        if(strEmail != null){
            contactModel.setEmailId(strEmail);
        }

        String strAddress = intent.getStringExtra(ADDRESS);
        if(strAddress != null){
            contactModel.setAddress(strAddress);
        }

        String strImage = intent.getStringExtra(IMAGE);
        if(strImage != null){
            contactModel.setImage(strImage);
        }

        String strTag = intent.getStringExtra(TAG);
        if(strTag != null){
            contactModel.setTag(strTag);
        }

        int saveMode = intent.getIntExtra(MODE, 0);
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
            } case BLOCK_NUMBER:{
                insertBlockNumber(context, strNumber, name);
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

    private void insertBlockNumber(Context context, String number, String name){
        BlockNumberModel blockModel = new BlockNumberModel();
        blockModel.setNumber(number);
        blockModel.setName(name);
        blockModel.setType(SpamContract.BLOCK_TYPE);
        final int i = SpamQuery.insertBlockList(context, blockModel);
        if(i == -1){
            Toast.makeText(context, "Already exits", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        }
    }
}
