package com.softgyan.findcallers.database.spam;

import android.net.Uri;

public final class SpamContract implements ISpamContract{
    public static final String BLOCK_LIST_TABLE = "block_table";
    public static final String BLOCK_ID = "block_id";
    public static final String BLOCK_COLUMN_NUMBER = "block_number";
    public static final String BLOCK_COLUMN_BLOCK_TYPE = "block_type";
    public static final String BLOCK_COLUMN_NAME = "block_name";

    public static final int SPAM_TYPE = 1;
    public static final int BLOCK_TYPE = 2;

    public static final String PATH_BLOCK = BLOCK_LIST_TABLE;
    public static final Uri CONTENT_BLOCK_URI = Uri.withAppendedPath(SPAM_BASE_CONTENT_URI, PATH_BLOCK);
}
