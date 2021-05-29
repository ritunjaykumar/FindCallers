package com.softgyan.findcallers.hardware;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

public final class CallHardware {
    public static final int INVALID_SIM_INFO = -1;
    public static final int SIM_ABSENT = 0;
    public static final int SIM_NETWORK_LOCKED = 1;
    public static final int SIM_PIN_REQUIRED = 2;
    public static final int SIM_PUK_REQUIRED = 3;
    public static final int SIM_UNKNOWN = 4;
    public static final int SIM_READY = 5;

    private static PackageManager getPackageManager(@NonNull Context context) {
        return context.getPackageManager();
    }

    public static boolean isPhoneSupportTelephony(@NonNull Context context) {
        return getPackageManager(context).hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean isGsmSupported(@NonNull Context context) {
        return getPackageManager(context).hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM);
    }

    public static boolean isCdmaSupported(@NonNull Context context) {
        return getPackageManager(context).hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA);
    }

    public static void makeCall(@NonNull Context context, @NonNull String number) {
        if (isPhoneSupportTelephony(context)) {
            if (SimDetails.getSimState(context) == SIM_READY) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
            } else {
                Toast.makeText(context, "Sim card is not ready to call", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "system don't have telephone support", Toast.LENGTH_SHORT).show();
        }
    }


    public static class SimDetails {
        public static final int INVALID_SIM_INFO = -1;
        public static final int SIM_ABSENT = 0;
        public static final int SIM_NETWORK_LOCKED = 1;
        public static final int SIM_PIN_REQUIRED = 2;
        public static final int SIM_PUK_REQUIRED = 3;
        public static final int SIM_UNKNOWN = 4;
        public static final int SIM_READY = 5;


        private static TelephonyManager getTelephonyManager(@NonNull Context context) {
            String srvName = Context.TELEPHONY_SERVICE;
            return (TelephonyManager) context.getSystemService(srvName);
        }

        public static String getPhoneType(@NonNull Context context) {
            String phoneTypeStr = "unknown";
            if (CallHardware.isPhoneSupportTelephony(context)) {
                int phoneType = getTelephonyManager(context).getPhoneType();
                switch (phoneType) {
                    case (TelephonyManager.PHONE_TYPE_CDMA):
                        phoneTypeStr = "CDMA";
                        break;

                    case (TelephonyManager.PHONE_TYPE_GSM):
                        phoneTypeStr = "GSM";
                        break;

                    case (TelephonyManager.PHONE_TYPE_SIP):
                        phoneTypeStr = "SIP";
                        break;

                    case (TelephonyManager.PHONE_TYPE_NONE):
                        phoneTypeStr = "NONE";
                        break;

                    default:
                        break;
                }
            }
            return phoneTypeStr;
        }

        public static int getSimState(@NonNull Context context) {
            final TelephonyManager telephonyManager = getTelephonyManager(context);
            int simState = telephonyManager.getSimState();
            if (simState == TelephonyManager.SIM_STATE_ABSENT) {
                return SIM_ABSENT;
            } else if (simState == TelephonyManager.SIM_STATE_NETWORK_LOCKED) {
                return SIM_NETWORK_LOCKED;
            } else if (simState == TelephonyManager.SIM_STATE_PIN_REQUIRED) {
                return SIM_PIN_REQUIRED;
            } else if (simState == TelephonyManager.SIM_STATE_PUK_REQUIRED) {
                return SIM_PUK_REQUIRED;
            } else if (simState == TelephonyManager.SIM_STATE_UNKNOWN) {
                return SIM_UNKNOWN;
            } else if (simState == TelephonyManager.SIM_STATE_READY) {
                return SIM_READY;
            }
            return INVALID_SIM_INFO;
        }


    }
}
