package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.utils.exception.InvalidException;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class Utils {
    private static final String TAG = Utils.class.getName();

    public static String trimNumber(final String number) {
        String phoneNumber = number.replaceAll("[()\\s-]+", "");
        if (phoneNumber.startsWith("+91")) {
            phoneNumber = phoneNumber.replace("+91", "");

        }
        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.replace("0", "");
        }

        return phoneNumber;
    }


    public static String getUniqueId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static void hideViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static void showViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void enableViews(View... views) {
        for (View view : views) {
            view.setEnabled(true);
        }
    }

    public static void disableViews(View... views) {
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    public static void toastMessage(Context context, String tag, @NonNull String message) {

        Toast toast = new Toast(context);
        if (tag != null) {
            toast.setText(String.format("%s : %s", tag, message));
        } else {
            toast.setText(message);
        }
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkPermission(Context context, String... permissions) {
        boolean hasPermission = true;
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    break;
                }
            }
        }
        return hasPermission;
    }

    public static void openActivity(Context context, Class<?> cls, boolean clearTask) {
        Intent intent = new Intent(context, cls);
        if (clearTask)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static List<SubscriptionInfo> getSimCardInfo(Context context) throws InvalidException {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED) {
            final SubscriptionManager subscriptionManager =
                    (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            return subscriptionManager.getActiveSubscriptionInfoList();
        }
        throw new InvalidException("Permission Not Granted -> Manifest.permission.READ_PHONE_STATE");

    }


    public static Date stringToDate(String aDate) {

        if (aDate == null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy");
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

    }

    public static int getSubscriptionId(final Context context, @NonNull final String iccId) {
        SimCardInfoModel.getSimInfoS(context);
        for (SimCardInfoModel simInfo : SimCardInfoModel.simCardInfoList) {
            if (iccId.equals(simInfo.getIccId())) {
                return simInfo.getSubscriptionId();
            }
        }
        return -1;
    }

    public static int getCallTypeIcon(final int callTypeCode) {
        switch (callTypeCode) {

            case CommVar.INCOMING_TYPE:
            case CommVar.REJECTED_TYPE:
                return R.drawable.ic_in_coming;
            case CommVar.MISSED_TYPE:
                return R.drawable.ic_missed;
            case CommVar.OUTGOING_TYPE:
                return R.drawable.ic_out_going;
            case CommVar.BLOCKED_TYPE:
                return R.drawable.ic_block;
            default:
                return -1;

        }
    }

    public static int getPlaceHolderCount(final String selection) {
        final char[] charArray = selection.toCharArray();
        int count = 0;
        int front = 0;
        int rear = charArray.length - 1;
        while (front < rear) {
            if (charArray[front] == '?' && charArray[rear] == '?') {
                count += 2;
            } else if (charArray[front] == '?' || charArray[rear] == '?') {
                count++;
            }
            front++;
            rear--;
        }
        return count;
    }
}
