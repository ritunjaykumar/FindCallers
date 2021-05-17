package com.softgyan.findcallers.services;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.Date;

public class BackgroundCallServices extends Service {

    public static final String CALL_NUMBER = "callNumber";
    public static final String CALL_TYPE = "callType";
    public static final String CALL_DATE = "callType";
    public static final String CALL_SIM_ID = "callIccId";
    public static final int callEnd = 1;
    public static final int callStart = 0;
    private static final String TAG = "my_tag";
    private Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: called");
        ctx = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String number = intent.getStringExtra(CALL_NUMBER);
        Date callDate = Utils.stringToDate(intent.getStringExtra(CALL_DATE));
        int simId = intent.getIntExtra(CALL_SIM_ID, -1);
        int callType = intent.getIntExtra(CALL_TYPE, -1);
        if (intent.getIntExtra("callState", -1) == callStart)
            startServiceNotification();
        new Thread() {
            @Override
            public void run() {
                ContactModel contact = ContactsQuery.searchByNumber(ctx, number);
                if (contact == null) {
                    Log.d(TAG, "onStartCommand: contact : null");
                } else {
                    Log.d(TAG, "run: contact : " + contact);
                }
            }
        }.start();
        if (intent.getIntExtra("callState", -1) == callEnd) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotification(CharSequence strNumber, String name) {
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx, App.CHANNEL_ID_1);
        new NotificationCompat.Builder(ctx, App.CHANNEL_ID_1);
        RemoteViews collapseView = new RemoteViews(ctx.getPackageName(), R.layout.collapse_notification_layout);

        RemoteViews expandedView = new RemoteViews(ctx.getPackageName(), R.layout.expanded_notification_layout);


        collapseView.setTextViewText(R.id.tvName, strNumber);
        expandedView.setTextViewText(R.id.tvNumber, strNumber);
        if (name != null) {
            collapseView.setTextViewText(R.id.tvName, name);
            expandedView.setTextViewText(R.id.tvName, name);
        }


        notification.setSmallIcon(R.drawable.ic_call)
                .setCustomContentView(collapseView)
                .setCustomBigContentView(expandedView);

    }

    private void startServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        startForeground(1, notification);
    }

}
