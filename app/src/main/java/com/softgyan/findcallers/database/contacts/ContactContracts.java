package com.softgyan.findcallers.database.contacts;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ContactContracts {

    public static abstract class ContactsDetails implements BaseColumns{

        public static final String TABLE_NAME = "Contacts";

        public static final String COLUMN_USER_ID = "contact"+_ID;

        public static final String COLUMN_USER_NAME = "name";
        public static final String COLUMN_USER_EMAIL_ID = "emailId";
        public static final String COLUMN_USER_ADDRESS = "address";
        public static final String COLUMN_USER_IMAGE = "image";
        public static final String COLUMN_USER_TAG = "tag";
        public static final String COLUMN_DEFAULT_NUMBER = "default_number"; //todo



        public static final String PATH_USER = TABLE_NAME;

        public static final Uri CONTENT_USER_URI = Uri.withAppendedPath(IContactContract.BASE_CONTENT_URI, PATH_USER);


        public static final String MOBILE_NUMBER_TABLE = "mobileNumber";
        public static final String COLUMN_MOBILE_ID = "mobile"+_ID;
        public static final String COLUMN_USER_REF_ID = "contact_ref"+_ID;
        public static final String COLUMN_MOBILE_NUMBER = "mobileNumber";

        public static final String PATH_MOBILE = MOBILE_NUMBER_TABLE;
        public static final Uri CONTENT_MOBILE_URI = Uri.withAppendedPath(IContactContract.BASE_CONTENT_URI, PATH_MOBILE);
    }

}
