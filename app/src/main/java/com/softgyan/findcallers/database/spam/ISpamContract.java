package com.softgyan.findcallers.database.spam;

import android.net.Uri;

public interface ISpamContract {
    String SPAM_CONTENT_AUTHORITY = "com.softgyan.findcallers.spam";
    Uri SPAM_BASE_CONTENT_URI = Uri.parse("content://" + SPAM_CONTENT_AUTHORITY);
}
