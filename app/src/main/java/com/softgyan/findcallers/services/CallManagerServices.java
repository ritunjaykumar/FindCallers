package com.softgyan.findcallers.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.application.App;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.database.call.system.SystemCalls;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.database.query.SpamQuery;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.BlockNumberModel;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallNumberModel;
import com.softgyan.findcallers.models.CallerInfoModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.CallRecorder;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.CallerDialog;

import java.util.HashMap;
import java.util.List;

public class CallManagerServices extends Service {

    private static final int CALL_SERVICE_ID = 5;
    private static final String TAG = "CallManagerServices";
    public static final String CALL_NOTIFIER_RECEIVER_ACTION = "com.softgyan.findcallers.CALL_NOTIFY_RECEIVER";
    public static final String CALL_KEY = "callKey";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String IS_OUT_GOING = "isOutGoing";

    public static final int CALL_INITIATE = 1;
    public static final int CALL_HOOKED = 2;
    public static final int CALL_END = 3;


    private volatile boolean isOutgoing = false;
    private volatile boolean isHooked = false;
    private volatile boolean isCallInitiate = false;

    boolean isSpam = false;


    private volatile Intent callIntent = null;
    private int lastUpdate = -1;
    private String mobNumber;
    private CallerDialog callerDialog;

    private CallerInfoModel callerInfoModels;
    private ContactModel mContactModel;


    //for recording call
    private CallRecorder callRecorder;
    private boolean isCallRecorderEnable = false;
    //


    @Override
    public void onCreate() {
        super.onCreate();
        registerCallReceiver();
        callerDialog = new CallerDialog(getApplicationContext());
        Log.d(TAG, "onCreate: service created");
        isCallRecorderEnable = AppPreference.isCallRecordingEnable(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServiceNotification();
        callIntent = intent;

        if (callIntent != null) {
            if (isCallRecorderEnable)
                callRecorder = new CallRecorder(this);
            callThread.start();
        } else {
            stopService();
        }

        //callRecording

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerCallReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CALL_NOTIFIER_RECEIVER_ACTION);
        registerReceiver(bReceiver, filter);
        Log.d(TAG, "registerCallReceiver: broadcast receiver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
        Log.d(TAG, "onDestroy: broadcast receiver unregistered");
    }

    private void startServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Find Caller is active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();
        startForeground(CALL_SERVICE_ID, notification);
    }

    private void stopService() {
        stopSelf();
    }

    private void showDialogOverCall(CallerInfoModel callerInfoModel, boolean isViewUpdated) {
        Handler handler = new Handler(getApplicationContext().getMainLooper());
        Runnable runnable = () -> callerDialog.showDialog(callerInfoModel, isViewUpdated);
        handler.post(runnable);
    }

    private final Thread callThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                int callValue = callIntent.getIntExtra(CALL_KEY, -1);
//                Log.d(TAG, "run: callValue : "+callValue);
                if (callValue == lastUpdate) {
                    continue;
                }
                if (callValue == CALL_INITIATE) {
                    /*
                     * show dialog on call screen+
                     */

                    Log.d(TAG, "run: callValue : " + "CALL_INITIATE");
                    isCallInitiate = true;
                    lastUpdate = callValue;
                    isOutgoing = callIntent.getBooleanExtra(IS_OUT_GOING, false);
                    mobNumber = callIntent.getStringExtra(MOBILE_NUMBER);
                    searchNumber(mobNumber);

                } else if (callValue == CALL_HOOKED) {
                    /*
                     * start call recording if enabled
                     */
                    if (isCallRecorderEnable) {
                        if (callRecorder != null)
                            callRecorder.startRecording();
                    }
                    Log.d(TAG, "run: called hooked");
                    lastUpdate = callValue;
                    isHooked = true;


                } else if (callValue == CALL_END) {
                    /*
                     * stop recording if started
                     * save number in DB+
                     * stop service+
                     * break the loop+
                     * */
                    if (isCallRecorderEnable) {
                        if (callRecorder != null)
                            callRecorder.stopRecording();
                    }

                    Log.d(TAG, "run: call end");
                    lastUpdate = callValue;
                    if (!isSpam) {
                        if (!isOutgoing && !isHooked) {
                            //it means called 'missed call'
                            if (callerInfoModels != null)
                                callerInfoModels.setMessage("Missed Called");
                        } else {
                            callerInfoModels.setMessage(null);
                        }
                    }

                    SystemClock.sleep(2000);

                    showDialogOverCall(callerInfoModels, true);
                    saveLastCallHistory(getApplicationContext(), mobNumber);
                    stopService();
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        }
    };

    private void searchNumber(final String mobNum) {
        Log.d(TAG, "searchNumber: mobileNumber : " + mobNum);
        Log.d(TAG, "searchNumber: is outgoing :" + isOutgoing);
        if (!isOutgoing) {
            final HashMap<String, Object> blockList = SpamQuery.getBlockList(getApplicationContext(),
                    Utils.trimNumber(mobNum));
            Log.d(TAG, "searchNumber: blockList : " + blockList);
            final Boolean tempIsPresent = (Boolean) blockList.get(SpamQuery.IS_PRESENT);
            Log.d(TAG, "searchNumber: tempIsPresent : " + tempIsPresent);
            if (tempIsPresent != null && tempIsPresent) {
                List<BlockNumberModel> blockNumberModels = (List<BlockNumberModel>) blockList.get(SpamQuery.BLOCK_NUMBER_LIST);
                BlockNumberModel bn = blockNumberModels.get(0);
                callerInfoModels = CallerInfoModel.getInstance(
                        bn.getName(), bn.getNumber(), -1, null, true, isOutgoing
                );
                if (bn.getType() == SpamContract.BLOCK_TYPE) {
                    callerInfoModels.setMessage("Blocked number");
                } else {
                    callerInfoModels.setMessage("Spam number");
                }
                isSpam = true;
                showDialogOverCall(callerInfoModels, false);
                final boolean cutTheCall = CallHardware.cutTheCall(getApplicationContext());
                Log.d(TAG, "searchNumber: cutTheCall : " + cutTheCall);
                return;
            }
        }

        boolean flagLocalContact = false;
        boolean flagSystemContact = false;

        mContactModel = ContactsQuery.searchByNumber(getApplicationContext(), mobNumber);
        if (mContactModel == null) {
            if (Utils.checkPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)) {
                mContactModel = Utils.searchNumberFromSystem(getApplicationContext(), mobNumber);
                if (mContactModel != null) {
                    flagSystemContact = true;
                }
            }
        } else {
            flagLocalContact = true;
        }
        if (flagLocalContact || flagSystemContact) {
            callerInfoModels = Utils.getCallerInfoModel(mContactModel);
            showDialogOverCall(callerInfoModels, false);
        } else {
            if (Utils.isInternetConnectionAvailable(getApplicationContext())) {

                FirebaseDB.MobileNumberInfo.getMobileNumber(mobNum, new OnResultCallback<ContactModel>() {
                    @Override
                    public void onSuccess(@NonNull ContactModel contactModel) {
                        mContactModel = contactModel;
                        callerInfoModels = Utils.getCallerInfoModel(contactModel);
                        showDialogOverCall(callerInfoModels, false);
                    }

                    @Override
                    public void onFailed(String failedMessage) {
                        callerInfoModels = CallerInfoModel.getInstance(null, mobNum, -1,
                                null, false, false);
                        showDialogOverCall(callerInfoModels, false);
                    }
                });
            } else {
                callerInfoModels = CallerInfoModel.getInstance(null, mobNum, -1,
                        null, false, false);
                showDialogOverCall(callerInfoModels, false);
            }
        }

    }

    private void saveLastCallHistory(Context context, String number) {
        final CallModel lastCallHistory = SystemCalls.getLastCallHistory(context);
//        Log.d(TAG, "onReceive: lastCallHistory : " + lastCallHistory);
        if (lastCallHistory == null) {
            return;
        }
//        Log.d(TAG, "saveLastCallHistory: searched number : " + mContactModel);

        final CallModel callModel = CallQuery.searchCallHistoryByNumber(context, number);
//        Log.d(TAG, "saveLastCallHistory: searched number from Local : " + callModel);
        if (callModel != null) {

            CallNumberModel lastCallNumberModel = lastCallHistory.getFirstCall();
            lastCallNumberModel.setNameRefId(callModel.getNameId());
            final int i = CallQuery.insertCallNumberLog(getApplicationContext(), lastCallNumberModel);
            if (i == 0) {
//                Log.d(TAG, "saveLastCallHistory: number not save : " + lastCallNumberModel);
                lastCallNumberModel.setCallModelId(i);
                Utils.setCallLogToList(lastCallHistory);
            } else {
//                Log.d(TAG, "saveLastCallHistory: number save : " + lastCallNumberModel);
            }
        } else {
            if (mContactModel != null) {
                String name = mContactModel.getName();
                if (lastCallHistory.getCacheName() == null) {
                    lastCallHistory.setCacheName(name);
                }
            } else {
                lastCallHistory.setCacheName("unknown_" + number);
            }
            CallQuery.insertCallLog(getApplicationContext(), lastCallHistory);
//            Log.d(TAG, "saveLastCallHistory: after save operation : " + lastCallHistory);

        }
        Utils.setCallLogToList(lastCallHistory);
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int callKey = intent.getIntExtra(CALL_KEY, -1);
                callIntent.putExtra(CALL_KEY, callKey);
            } else {
                Log.d(TAG, "onReceive: intent value : null");
            }
        }
    };

}
