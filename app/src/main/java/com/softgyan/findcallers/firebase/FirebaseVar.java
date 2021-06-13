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
        public static final String DOCTOR = "Doctor";
        public static final String ELECTRICIAN = "Electrician";
        public static final String POLICE_STATION = "PoliceStation";

    }

    private interface Common {
        String NAME = "name";
        String GEO_POINT = "point";
        String CONTACT = "contact";
        String AREA = "area";
        String PIN_CODE = "pinCode";
        String STATE = "state";
        String DISTRICT = "district";
        String MAP_LOCATION = "mapLocation";
        String GENDER = "gender";
    }

    public static final class DoctorType {
        public static final String PHYSICIAN = "Physicians";
        public static final String GENERAL_SURGEON = "General Surgeon";
        public static final String DENTIST = "Dentist";
        public static final String PEDIATRICIAN = "Pediatricians";

    }

    public static final class Doctor implements Common {
        public static final String DB_NAME = "Doctor";
        public static final String DOCTOR_TYPE = "doctorType";
    }

    public static final class Electrician implements Common {
        public static final String NAME = "Electrician Name";
        public static final String DB_NAME = "Electrician";
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
