package com.softgyan.findcallers.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreference {
    private static final String APP_NAME = "FindCallers";
    private final static String IS_START_FIRST_TIME = "isStartFirstTime";

    synchronized static private SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
    }

    synchronized static private SharedPreferences.Editor getSharedEditor(Context context) {
        SharedPreferences sharedPref = getSharedPref(context);
        return sharedPref.edit();
    }


    private static final String ACCOUNT_ACTIVITY_SET_KEY = "AccountActivitySetKey";

    public static synchronized void setAccountActivitySet(Context context, boolean accountActivitySet) {
        getSharedEditor(context).putBoolean(ACCOUNT_ACTIVITY_SET_KEY, accountActivitySet).apply();
    }

    public static synchronized boolean isAccountActivitySet(Context context) {
        return getSharedPref(context).getBoolean(ACCOUNT_ACTIVITY_SET_KEY, false);
    }

    synchronized public static boolean isWelcomeActivitySet(Context context) {
        return getSharedPref(context).getBoolean(IS_START_FIRST_TIME, false);
    }

    synchronized public static void setWelcomeActivity(Context context, boolean isWelcomeActivitySet) {

        final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
        sharedEditor.putBoolean(IS_START_FIRST_TIME, isWelcomeActivitySet);
        sharedEditor.apply();
    }


    public static final class SimPreference {
        public static final String SIM_ICC_KEY = "simIccCode_";
        public static final String SIM_ICC_SIZE_KEY = "simIccSize";
        public static final String SIM_INFO_MESSAGE = "simInfoMessage";
        public static final String FIRST_NUMBER_KEY = "firstNumberKey";
        public static final String SECOND_NUMBER_KEY = "secondNumberKey";

        public static void setSimIcc(final Context context, final String... simIccS) {
            for (int i = 0; i < simIccS.length; i++) {
                final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
                sharedEditor.putString(SIM_ICC_KEY + i, simIccS[i]);
                sharedEditor.apply();
            }
            final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
            sharedEditor.putInt(SIM_ICC_SIZE_KEY, simIccS.length);
            sharedEditor.apply();

        }

        public static String[] getSimIccS(Context context) {
            final int simIccSize = getSharedPref(context).getInt(SIM_ICC_SIZE_KEY, -1);
            String[] simIccS = new String[simIccSize];
            for (int i = 0; i < simIccSize; i++) {
                simIccS[i] = getSharedPref(context).getString(SIM_ICC_KEY + i, null);
            }
            return simIccS;
        }

        public static String getSimMessage(Context context) {
            return getSharedPref(context).getString(SIM_INFO_MESSAGE, null);
        }

        public static void setSimMessage(Context context, String message) {
            final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
            sharedEditor.putString(SIM_INFO_MESSAGE, message);
            sharedEditor.apply();
        }

        public static void setFirstNumber(Context context, String firstNumber) {
            final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
            sharedEditor.putString(FIRST_NUMBER_KEY, firstNumber);
            sharedEditor.apply();
        }

        public static void setSecondNumber(Context context, String secondNumber) {
            final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
            sharedEditor.putString(SECOND_NUMBER_KEY, secondNumber);
            sharedEditor.apply();
        }

        public static String getFirstNumber(Context context) {
            return getSharedPref(context).getString(FIRST_NUMBER_KEY, null);

        }

        public static String getSecondNumber(Context context) {
            return getSharedPref(context).getString(SECOND_NUMBER_KEY, null);
        }

    }


    public static synchronized void clearPreference(Context context) {
        getSharedEditor(context).clear().apply();
    }


}
