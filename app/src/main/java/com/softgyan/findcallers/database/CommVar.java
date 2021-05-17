package com.softgyan.findcallers.database;

import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.ContactModel;

import java.util.ArrayList;
import java.util.List;

public final class CommVar {

    public final static List<ContactModel> contactsList = new ArrayList<>();
    public final static List<CallModel> callList = new ArrayList<>();


    public static final int FAILED = -1;
    public static final int INVALID_ARG = -2;
    public static final int INVALID_INDEX = -3;
    public static final int SUCCESS = 2;
    public static final int DATA_NOT_FOUND = -4;


    public static final int SIM_ONE = 1;
    public static final int SIM_TWO = 2;

    public static final int INCOMING_TYPE = 1;
    public static final int OUTGOING_TYPE = 2;
    public static final int MISSED_TYPE = 3;
    public static final int VOICEMAIL_TYPE = 4;
    public static final int REJECTED_TYPE = 5;
    public static final int BLOCKED_TYPE = 6;

}
