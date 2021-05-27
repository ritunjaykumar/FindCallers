package com.softgyan.findcallers.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    /**
     * @param context         can't be null
     * @param callerInfoModel only user info to show
     */

    public static void showDialogOverCall(final Context context, CallerInfoModel callerInfoModel,
                                          boolean isViewUpdate) {
        //todo isViewUpdate
        /*checking permission for showing dialog*/

        Log.d(TAG, "showDialogOverCall: isViewUpdate : " + isViewUpdate);

        if (requestOverlayPermission(context)) return;
        if (callerInfoModel == null) return;
        Log.d(TAG, "showDialogOverCall: caller info details : " + callerInfoModel);
        WindowManager windowManager;
        WindowManager.LayoutParams params;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flags = WindowManager.LayoutParams.TYPE_PHONE;
        }
        int layoutParams = flags;


        View view = layoutInflater.inflate(R.layout.layout_caller_info, null);

        int wmFlag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutParams,
                wmFlag,
                PixelFormat.TRANSLUCENT
        );

        if (isViewUpdate) {
            LinearLayout linearLayout = view.findViewById(R.id.llOptionContainer);
            showViews(linearLayout);
        }

        if (!isNull(callerInfoModel.getMessage())) {
            TextView tvMessage = view.findViewById(R.id.tvNotification);
            tvMessage.setText(callerInfoModel.getMessage());
            showViews(tvMessage);
        }


        view.findViewById(R.id.ibClose).setOnClickListener(v -> {
            windowManager.removeView(view);

        });
        view.findViewById(R.id.tvCall).setOnClickListener(v -> {
            CallHardware.makeCall(context, callerInfoModel.getNumber());

        });
        TextView tvName = view.findViewById(R.id.tvName);
        if (!isNull(callerInfoModel.getName())) {
            tvName.setText(callerInfoModel.getName());
        } else {
            tvName.setText("unknown Caller");
        }
        if (!isNull(callerInfoModel.getProfileUri())) {
            ShapeableImageView ivProfile = view.findViewById(R.id.sivProfile);
            Glide.with(context).load(callerInfoModel.getProfileUri()).into(ivProfile);
        }

        TextView tvNumber = view.findViewById(R.id.tvNumber);
        tvNumber.setText(callerInfoModel.getNumber());


        if (isViewUpdate) {
            try {
                windowManager.removeView(view);
                windowManager.addView(view, params);
            } catch (Exception e) {
                Log.d(TAG, "showDialogOverCall: error : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            windowManager.addView(view, params);
        }
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
            searchNumberFromSystem(context, mobileNumber);

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

    public static boolean isInternetConnectionAvailable(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI ||
                    activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                isConnected = true;
            }
        }
        return isConnected;
    }
}
