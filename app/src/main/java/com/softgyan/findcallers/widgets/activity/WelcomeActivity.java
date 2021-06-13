package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.call.system.SystemCalls;
import com.softgyan.findcallers.database.contacts.system.SystemContacts;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.services.UploadContactService;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = WelcomeActivity.class.getName();
    private static final int PERMISSION_ALL = 123;
    private static final String dialogMessage = "Wait for minute to configure the App";
    private static volatile boolean contactFlag = false;
    private static volatile boolean callFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        askPermission();
    }

    private void askPermission() {
        String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};

        final boolean isPermitted = Utils.hasPermissions(this, permissions);
        if (!isPermitted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }
        if (Utils.checkPermission(this, permissions)) {
            savingDetails();
            saveSimInfo();
        }


    }

    private void saveSimInfo() {
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " is not granted", Toast.LENGTH_LONG).show();
                    cancelPermissionMessage();
                }
            }
            savingDetails();
        }

    }


    private void cancelPermissionMessage() {
        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (contactFlag && callFlag) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "you can't go back", Toast.LENGTH_SHORT).show();
        }
    }

    private void savingDetails() {

        try {
            Log.d(TAG, "savingDetails: part_1");
            GettingContactBackgroundWork backgroundWork = new GettingContactBackgroundWork(this);
            backgroundWork.execute();
            Log.d(TAG, "savingDetails: part_2");
            GettingCallLogBackgroundWork callWork = new GettingCallLogBackgroundWork(this);
            callWork.execute();
        } catch (Exception e) {
            Log.d(TAG, "savingDetails: " + e.getMessage());
        }
    }


    // getting systemContacts data
    private static final class GettingContactBackgroundWork extends AsyncTask<Void, Void, List<ContactModel>> {

        final Context context;
        private final ProgressDialog progressDialog;

        public GettingContactBackgroundWork(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle(dialogMessage);
            Log.d(TAG, "GettingContactBackgroundWork: getting data from system");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected List<ContactModel> doInBackground(Void... voids) {
            return SystemContacts.getSystemContactsList(context);
        }

        @Override
        protected void onPostExecute(List<ContactModel> contactModels) {
            super.onPostExecute(contactModels);
            progressDialog.dismiss();
            //debugging here remove comment
            SavingBackgroundWork backgroundWork = new SavingBackgroundWork(context);
            backgroundWork.execute(contactModels);
            for (ContactModel contactModel : contactModels) {
                Log.d(TAG, "onPostExecute: contactModel : " + contactModel.toString());
            }

            Log.d(TAG, "onPostExecute: listSize : " + SystemContacts.uploadContactList.size());
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(context, UploadContactService.class);
                ContextCompat.startForegroundService(context, intent);
                Log.d(TAG, "onPostExecute: start background service");
            }, 0);
        }
    }

    //saving contact data
    private static final class SavingBackgroundWork extends AsyncTask<List<ContactModel>, Void, Void> {
        private final Context context;
        private final ProgressDialog progressDialog;

        public SavingBackgroundWork(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle(dialogMessage);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(List<ContactModel>... lists) {
            for (ContactModel contact : lists[0]) {
                ContactsQuery.insertContactsDetails(context, contact);
            }
            CommVar.contactsList.addAll(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressDialog.dismiss();
            contactFlag = true;
            openMainActivity(context);

        }
    }


    // getting system call data
    private static final class GettingCallLogBackgroundWork extends AsyncTask<Void, Void, List<CallModel>> {

        final Context context;
        private final ProgressDialog progressDialog;

        public GettingCallLogBackgroundWork(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle(dialogMessage);
            Log.d(TAG, "GettingCallLogBackgroundWork: getting call log data from system");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected List<CallModel> doInBackground(Void... voids) {
            return SystemCalls.getCallList(context);
        }

        @Override
        protected void onPostExecute(List<CallModel> callModels) {
            super.onPostExecute(callModels);
            progressDialog.dismiss();
            SavingCallLogBackgroundWork backgroundWork = new SavingCallLogBackgroundWork(context);
            backgroundWork.execute(callModels);

        }
    }

    //saving call data
    private static final class SavingCallLogBackgroundWork extends AsyncTask<List<CallModel>, Void, Void> {
        private final Context context;
        private final ProgressDialog progressDialog;

        public SavingCallLogBackgroundWork(Context context) {
            this.context = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressTitle(dialogMessage);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(List<CallModel>... lists) {
            for (CallModel callModel : lists[0]) {
                CallQuery.insertCallLog(context, callModel);
            }
            CommVar.callList.addAll(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressDialog.dismiss();
            callFlag = true;
            openMainActivity(context);
        }
    }

    private static void openMainActivity(Context context) {
        Log.d(TAG, "openMainActivity: start account activity" + callFlag + " " + contactFlag);
        if (callFlag && contactFlag) {
            AppPreference.setWelcomeActivity(context, true);
            Intent intent = new Intent(context, UserAccountSettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(UserAccountSettingActivity.IS_SAVE_DATA, true);
            context.startActivity(intent);
        }
    }

}