package com.softgyan.findcallers.database.call;

import android.net.Uri;

public interface ICallContract {
    String CALL_CONTENT_AUTHORITY = "com.softgyan.findcaller.call";
    Uri CALL_BASE_CONTENT_URI = Uri.parse("content://" + CALL_CONTENT_AUTHORITY);


}
