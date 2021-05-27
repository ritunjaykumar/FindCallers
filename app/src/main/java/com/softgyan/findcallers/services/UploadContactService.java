package com.softgyan.findcallers.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.database.contacts.system.SystemContacts;
import com.softgyan.findcallers.firebase.FirebaseUserData;
import com.softgyan.findcallers.models.UploadContactModel;

import java.util.ArrayList;
import java.util.List;

public class UploadContactService extends Service {
    private static final int CALL_SERVICE_ID = 6;
    public static final String UPLOAD_CONTACT_LIST = "UploadContactList";
    private final List<UploadContactModel> uploadContactList = new ArrayList<>();
    private static final String TAG = "UploadContactService";

    @Override
    public void onCreate() {
        super.onCreate();
        uploadContactList.addAll(SystemContacts.uploadContactList);
        Log.d(TAG, "onCreate: service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        if (uploadContactList.size() == 0) {
            stopSelf();
            return START_NOT_STICKY;
        }
        FirebaseUserData.MobileNumberInfo.uploadContacts(uploadContactList, this::stopSelf);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Find Caller is active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        startForeground(CALL_SERVICE_ID, notification);
    }
}
