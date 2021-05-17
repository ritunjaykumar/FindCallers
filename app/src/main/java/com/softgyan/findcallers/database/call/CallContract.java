package com.softgyan.findcallers.database.call;

import android.net.Uri;
import android.provider.BaseColumns;

import com.softgyan.findcallers.database.contacts.IContactContract;

public class CallContract {

    public static final class CallDetails implements BaseColumns {
        public static final String CALL_HISTORY_TABLE = "call_history";
        public static final String CALL_COLUMN_ID = "call_id";
        public static final String CALL_COLUMN_NAME_REF_ID = "name_ref_id";
        public static final String CALL_COLUMN_NUMBER = "call_number";
        public static final String CALL_COLUMN_DATE = "call_date";
        public static final String CALL_COLUMN_TYPE = "call_type";
        public static final String CALL_COLUMN_DURATION = "call_duration";
        public static final String CALL_COLUMN_SUBSCRIPTION_ID = "subscription_id";

        public static final String PATH_CALL = CALL_HISTORY_TABLE;
        public static final Uri CONTENT_CALL_URI = Uri.withAppendedPath(ICallContract.CALL_BASE_CONTENT_URI, PATH_CALL);
//        public static final Uri CONTENT_CALL_URI = Uri.withAppendedPath(IContactContract.BASE_CONTENT_URI, PATH_CALL);


        public static final String CACHE_NAME_TABLE = "cache_name_table";
        public static final String CACHE_NAME = "cache_name";
        public static final String CACHE_NAME_ID = "name_id";

        public static final String PATH_CACHE_NAME = CACHE_NAME_TABLE;
        public static final Uri CONTENT_CACHE_NAME_URI = Uri.withAppendedPath(ICallContract.CALL_BASE_CONTENT_URI, PATH_CACHE_NAME);
//        public static final Uri CONTENT_CACHE_NAME_URI = Uri.withAppendedPath(IContactContract.BASE_CONTENT_URI, PATH_CACHE_NAME);


    }

}
