package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnSuccessfulCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public static String getUriExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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

    public static void toastMessage(Context context, @NonNull String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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

    public static Date stringToDate(String aDate) {

        if (aDate == null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy");
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

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

    public static boolean isNull(Object object) {
        return object == null;
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


    public synchronized static List<HashMap<String, Object>> getAllContactForBackup(Context context) {
        if (context == null) return null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Read and write contacts permission is denied", Toast.LENGTH_SHORT).show();
            return null;
        }
        final List<HashMap<String, Object>> backupContactList = new ArrayList<>();
        final Uri CONTACT_BASE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(CONTACT_BASE_URI, null, null, null, null);

        final String[] columnNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            HashMap<String, Object> mapContact = new HashMap<>();
            for (String columnName : columnNames) {
                mapContact.put(columnName, cursor.getColumnIndex(columnName));
            }
            backupContactList.add(mapContact);

        }
        cursor.close();
        return backupContactList;
    }


    public static boolean requestOverlayPermission(Context context) {
        return !Settings.canDrawOverlays(context);
    }


    public static ContactModel advanceSearch(Context context, String mobileNumber) {
        ContactModel contactModel = ContactsQuery.searchByNumber(context, mobileNumber);
        if (contactModel != null) {
            return contactModel;
        }
        if (checkPermission(context, Manifest.permission.READ_CONTACTS)) {
            return searchNumberFromSystem(context, mobileNumber);

        }
        return null;
    }


    public static final Uri CONTACT_SEARCH_URI = ContactsContract.PhoneLookup.CONTENT_FILTER_URI;
    private static final String PHOTO_URI = ContactsContract.CommonDataKinds.Photo.PHOTO_URI;

    public static ContactModel searchNumberFromSystem(@NonNull Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(CONTACT_SEARCH_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, PHOTO_URI};

        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor.getCount() == 0) {
            return null;
        }

        String contactName = null;
        String profileUri = null;
        String number = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            profileUri = cursor.getString(cursor.getColumnIndex(PHOTO_URI));
            number = phoneNumber;
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        ContactModel contactModel = new ContactModel(contactName);
        contactModel.setImage(profileUri);
        contactModel.setContactNumbers(new ContactNumberModel(number));

        return contactModel;
    }


    public static CallerInfoModel getCallerInfoModel(final ContactModel contactModel) {
        if (contactModel == null) {
            return CallerInfoModel.getInstance(null, "invalid", -1
                    , null, true, false);
        }
        return CallerInfoModel.getInstance(
                contactModel.getName(), contactModel.getContactNumbers().get(0).getMobileNumber(), -1
                , contactModel.getImage(), true, false
        );
    }

    /**
     * @param context can't be null
     * @return if connection available return true otherwise false;
     */
    public static boolean isInternetConnectionAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network network = conMgr.getActiveNetwork();
        NetworkCapabilities networkCapabilities = conMgr.getNetworkCapabilities(network);
        if (networkCapabilities != null) {
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else return false;
    }


    public static synchronized void setCallLogToList(CallModel callModel) {
        final List<CallModel> callList = CommVar.callList;
        Log.d(TAG, "setCallLogToList: size : " + callList.size());
        if (callList.size() == 0) {
            return;
        }
        CallModel tempCallModel = null;
        boolean isFind = false;
        for (CallModel cModel : callList) {
            if (cModel.getNameId() == callModel.getNameId()) {
                cModel.addNumberAtTop(callModel.getCallNumberList().get(0));
                isFind = true;
                tempCallModel = cModel;
                break;
            }
        }
        if (isFind) {
            callList.remove(tempCallModel);
            callList.add(0, tempCallModel);
        } else {
            callList.add(0, callModel);
        }

    }


    public static synchronized void setContactToList(final ContactModel contactModel) {
        final List<ContactModel> contactsList = CommVar.contactsList;
        if (contactsList.size() == 0) return;
        ContactModel tempContactModel = null;
        boolean isFind = false;
        int location = -1;
        for (ContactModel cModel : contactsList) {
            if (cModel.getId() == contactModel.getId()) {
                tempContactModel = cModel;
                location++;
                isFind = true;
                break;
            }
        }
        Log.d(TAG, "setContactToList: isFind : " + isFind);
        Log.d(TAG, "setContactToList: location : " + location);
        if (isFind) {
            tempContactModel.setName(contactModel.getName());
            tempContactModel.setAddress(contactModel.getAddress());
            tempContactModel.setEmailId(contactModel.getEmailId());
            tempContactModel.setImage(contactModel.getImage());
            tempContactModel.setTag(contactModel.getTag());
            tempContactModel.updateContactNumber(contactModel.getContactNumbers());
            contactsList.set(location, tempContactModel);
        } else {
            contactsList.add(contactModel);
        }


    }

    public static double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }


    public static String getDate(Date currentDateTime) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDateTime);
    }

    public static String getTime(Date currentDateTime) {
        return new SimpleDateFormat("H:mm", Locale.getDefault()).format(currentDateTime);
    }



}
