package com.softgyan.findcallers.firebase;

public final class FirebaseVar {
    public static final class User {
        public static final String USER_DB_NAME = "userDatabase";
        public static final String USER_NAME = "userName";
        public static final String USER_EMAIL = "userEmail";
        public static final String USER_PROFILE = "userProfile";
        public static final String USER_TAG = "userTag";
        public static final String EMAIL_VERIFY = "emailVerify";
        public static final String USER_ADDRESS = "userAddress";
    }

    public static final class MobileNumber {
        public static final String MOBILE_DB_NAME = "MobileDB";
        public static final String ADDRESS = "address";
        public static final String MOBILE_NUMBER = "mobileNumber";
        public static final String PROFILE_URL = "profileUrl";
        public static final String TOTAL_NAME = "totalName";
        public static final String USER_EMAIL = "userEmail";
        public static final String USER_NAME = "userName";
        public static final String USER_NAME_SET = "userSetName";
    }

    public static final class SpamDB {
        public static final String SPAM_DB_NAME = "spamDB";
        public static final String MOBILE_NUMBER = "mobileNumber";
        public static final String SPAM_TYPE_KEY = "spamType_";
        public static final String TOTAL_SPAM_VOTE = "totalVote";
        public static final String TOTAL_NAME = "totalName";
        public static final String NAME = "name_";
    }
}
