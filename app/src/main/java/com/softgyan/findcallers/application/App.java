package com.softgyan.findcallers.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.widgets.activity.AccountActivity;

import java.util.List;

public class App extends Application {
    private static final String TAG = App.class.getName();

    public static final String CHANNEL_ID_1 = "channel-1";
    public static final String CHANNEL_ID_2 = "channel-2";
    public static final String MAIN_SERVICE_ID = "mainServiceId";

    @Override
    public void onCreate() {
        super.onCreate();
        saveSimInfo();
        getContacts();
        createNotificationChannels();
        Log.d(TAG, "onCreate: called");
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel1 = new NotificationChannel(
                    CHANNEL_ID_1, "getContacts", NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationChannel notificationChannel2 = new NotificationChannel(
                    CHANNEL_ID_2, "SimDetection", NotificationManager.IMPORTANCE_LOW
            );
            NotificationChannel mainService = new NotificationChannel(
                    MAIN_SERVICE_ID, "mainService", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);
            notificationManager.createNotificationChannel(mainService);
        }
    }

    private void saveSimInfo() {

        if (AppPreference.SimPreference.getSimIccsSize(this) != -1) {
            return;
        }


        final List<SimCardInfoModel> simInfoS = SimCardInfoModel.getSimInfoS(this);
        if (simInfoS == null) {
            Toast.makeText(this, "There are no sims detected", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] tempIccCode = new String[simInfoS.size()];
        for (int i = 0; i < tempIccCode.length; i++) {
            tempIccCode[i] = simInfoS.get(i).getIccId();
        }
        AppPreference.SimPreference.setSimIcc(this, tempIccCode);
    }

    /*get all local contacts*/

    private void getContacts() {
        if (AppPreference.isWelcomeActivitySet(this)) {
            if (CommVar.contactsList.size() == 0) {
                ContactLoad contactLoad = new ContactLoad(this);
                contactLoad.execute();
            }
            if (CommVar.callList.size() == 0) {
                CallLogLoad callLogLoad = new CallLogLoad(this);
                callLogLoad.execute();
            }

        } else {
            Log.d(TAG, "getContacts: false");
        }


    }

    private static final class ContactLoad extends AsyncTask<Void, Void, List<ContactModel>> {
        private Context context;


        public ContactLoad(Context context) {
            this.context = context;
        }

        @Override
        protected List<ContactModel> doInBackground(Void... voids) {
            return ContactsQuery.getContactModels(context);
        }

        @Override
        protected void onPostExecute(List<ContactModel> contactModels) {
            super.onPostExecute(contactModels);
            if (contactModels != null) {
                CommVar.contactsList.addAll(contactModels);
            } else {
                Log.d(TAG, "onPostExecute: null value");
            }
        }
    }


    private static final class CallLogLoad extends AsyncTask<Void, Void, List<CallModel>> {

        private final Context context;

        public CallLogLoad(Context context) {
            this.context = context;
        }

        @Override
        protected List<CallModel> doInBackground(Void... voids) {
            return CallQuery.getCallList(context);
        }

        @Override
        protected void onPostExecute(List<CallModel> callModels) {
            super.onPostExecute(callModels);
            if (callModels != null) {
                CommVar.callList.addAll(callModels);
            } else {
                Log.d(TAG, "onPostExecute: callList is null");
            }
        }
    }


}
