package com.softgyan.findcallers.database.contacts;

import android.net.Uri;

public interface IContactContract {
    String CONTENT_AUTHORITY = "com.softgyan.findcalllers";
    Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
