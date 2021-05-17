package com.softgyan.findcallers.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingPreference {
    private static final String APP_NAME = "FindCallers";
    private final static String IS_START_FIRST_TIME = "isStartFirstTime";

    synchronized static private SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
    }

    synchronized static private SharedPreferences.Editor getSharedEditor(Context context) {
        SharedPreferences sharedPref = getSharedPref(context);
        return sharedPref.edit();
    }

    synchronized public static boolean isWelcomeActivitySet(Context context) {
        return getSharedPref(context).getBoolean(IS_START_FIRST_TIME, false);
    }

    synchronized public static void setWelcomeActivity(Context context, boolean isWelcomeActivitySet) {

        final SharedPreferences.Editor sharedEditor = getSharedEditor(context);
        sharedEditor.putBoolean(IS_START_FIRST_TIME, isWelcomeActivitySet);
        sharedEditor.apply();
    }


}
