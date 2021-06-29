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
        public static final String BUSINESS_ACCOUNT = "businessAccount";
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


    public static final class Business {
        public static final String DB_NAME = "Business";
        public static final String DB_DOCTOR = "Doctor";
        public static final String DB_ELECTRICIAN = "Electrician";
        public static final String DB_POLICE_STATION = "Police";
        public static final String DB_TYPE_KEY = "dbTypeKey";

        public static final String NAME = "Name";
        public static final String GEO_POINT = "Point";
        public static final String CONTACT = "Contact";
        public static final String AREA = "Area";
        public static final String PIN_CODE = "PinCode";
        public static final String STATE = "State";
        public static final String DISTRICT = "District";
        public static final String MAP_LOCATION = "MapLocation";
        public static final String GENDER = "Gender";
        public static final String SHOP_NAME = "ShopName";
        public static final String DOCTOR_TYPE = "DoctorType";
        public static final String DISTANCE_KEY = "Distance";

        public static final class DoctorType {
            public static final String PHYSICIAN = "Physicians";
            public static final String GENERAL_SURGEON = "General Surgeon";
            public static final String DENTIST = "Dentist";
            public static final String PEDIATRICIAN = "Pediatricians";

        }

        public static final class PoliceInfo {
            public static final String CONTACT = "Contact";
            public static final String COUNTRY = "Country";
            public static final String INSPECTOR_NAME = "InspectorName";
            public static final String POLICE_STATION_NAME = "PoliceStationName";
            public static final String GEO_POINT = "Point";
            public static final String PIN_CODE = "PinCode";
            public static final String STATE = "State";


        }
    }


    public static final class CallNotification {
        public static final String DB_NAME = "CallNotification";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String END_TIME = "endTime";
        public static final String START_TIME = "startTime";
        public static final String MESSAGE = "message";
    }
}
