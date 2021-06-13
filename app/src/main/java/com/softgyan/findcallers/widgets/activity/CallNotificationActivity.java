package com.softgyan.findcallers.widgets.activity;

import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.HashMap;
import java.util.Objects;

public class CallNotificationActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvStartDate, tvEndDate, tvStartTime, tvEndTime;
    private EditText etMessage;
    private static final String TAG = "CallNotificationActivity";
    private Calendar calendar;
    String startTime, startDate, endTime, endDate, sMessage;
    String startTimeTemp, startDateTemp, endTimeTemp, endDateTemp;
    private Button btnReset;
    private String mobileNumber;


    private volatile boolean isDataFind = false;
    private boolean isInvalidDate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_notification);
        setSupportActionBar(findViewById(R.id.toolBar));
        Objects.requireNonNull(getSupportActionBar()).setTitle("Call Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mobileNumber = currentUser.getPhoneNumber();
        } else {
            Toast.makeText(this, "your are not login", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!Utils.isInternetConnectionAvailable(this)) {
            Toast.makeText(this, "check internet connection", Toast.LENGTH_SHORT).show();
            finish();
        }


        calendar = Calendar.getInstance();
        Log.d(TAG, "initViewComponent: current date time :" + calendar.getTime());
        startDateTemp = Utils.getDate(calendar.getTime());
        startTimeTemp = Utils.getTime(calendar.getTime());
        endDateTemp = startDateTemp;


        getCallNotification();
        initViewComponent();
    }

    private void initViewComponent() {
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        etMessage = findViewById(R.id.etMessage);
        btnReset = findViewById(R.id.btnResetData);
        findViewById(R.id.btnSaveData).setOnClickListener(this);
        btnReset.setOnClickListener(this);


        int totalSecond = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + 2 * 3600;

        int hour = totalSecond / 3600;
        totalSecond = totalSecond % 3600;
        int minute = totalSecond / 60;
        endTimeTemp = hour + ":" + minute;
        if (!isDataFind) {
            updateView(startDateTemp, startTimeTemp, endDateTemp, endTimeTemp, etMessage.getText().toString());
        }


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvEndTime) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY) + 2, calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else if (id == R.id.btnSaveData) {
            saveRecord();
        } else if (id == R.id.btnResetData) {
            reset();
        }
    }

    TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {

        int previousSecond = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60;
        int currentSecond = hourOfDay * 3600 + minute * 60;
        if (previousSecond > currentSecond || currentSecond - previousSecond > 5 * 3600) {
            Toast.makeText(this, "invalid end time", Toast.LENGTH_SHORT).show();
        } else {
            endTimeTemp = hourOfDay + ":" + minute;
            updateView(startDateTemp, startTimeTemp, endDateTemp, endTimeTemp, etMessage.getText().toString());
        }
    };


    private void updateView(String startDate, String startTime, String endDate, String endTime, String message) {
        tvStartDate.setText(startDate);
        tvStartTime.setText(startTime);
        if (endTime != null && endTime.length() > 0)
            tvEndDate.setText(endDate);
        tvEndTime.setText(endTime);
        if (message == null || message.length() > 0) {
            etMessage.setText(message);
        }

    }

    private void getCallNotification() {
        FirebaseDB.CallNotification.getCallNotification(this, mobileNumber, new OnResultCallback<HashMap<String, Object>>() {
            @Override
            public void onSuccess(@NonNull HashMap<String, Object> stringObjectHashMap) {
                startDate = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.START_DATE);
                startTime = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.START_TIME);
                endDate = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.END_DATE);
                endTime = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.END_TIME);
                sMessage = (String) stringObjectHashMap.get(FirebaseVar.CallNotification.MESSAGE);

                if (startDate != null && startTime != null && endDate != null && endTime != null && sMessage != null) {
                    isDataFind = true;
                    updateView(startDate, startTime, endDate, endTime, sMessage);
                    compareDate(startTimeTemp, startDateTemp, endTime, endDate);
                }

                Log.d(TAG, "onSuccess: start date " + startDate);
            }

            @Override
            public void onFailed(String failedMessage) {
                Log.d(TAG, "onFailed: failde Message : " + failedMessage);
            }
        });
    }

    /**
     * @param startTime current system time
     * @param startDate current system date
     * @param endTime   end previous time
     * @param endDate   end previous date
     */
    private void compareDate(String startTime, String startDate, String endTime, String endDate) {
        Log.d(TAG, "compareDate: startTime : " + startTime);
        Log.d(TAG, "compareDate: startDate : " + startDate);
        Log.d(TAG, "compareDate: endTime : " + endTime);
        Log.d(TAG, "compareDate: endDate : " + endDate);
        try {
            String[] startTimeTemp = startTime.split(":");
            String[] startDateTemp = startDate.split("/");
            String[] endTimeTemp = endTime.split(":");
            String[] endDateTemp = endDate.split("/");

            int sDay = Integer.parseInt(startDateTemp[0]);
            int sMonth = Integer.parseInt(startDateTemp[1]);
            int sYear = Integer.parseInt(startDateTemp[2]);

            int sHour = Integer.parseInt(startTimeTemp[0]);
            int sMinute = Integer.parseInt(startTimeTemp[1]);

            int eDay = Integer.parseInt(endDateTemp[0]);
            int eMonth = Integer.parseInt(endDateTemp[1]);
            int eYear = Integer.parseInt(endDateTemp[2]);

            int eHour = Integer.parseInt(endTimeTemp[0]);
            int eMinute = Integer.parseInt(endTimeTemp[1]);

            if (eYear - sYear == 0 && eMonth - sMonth == 0 && eDay - sDay == 0) {
                Log.d(TAG, "compareDate: same day");
            } else {
                tvEndTime.setOnClickListener(this);
                reset();
            }

            int sSecond = sHour * 3600 + sMinute * 60;
            int eSecond = eHour * 3600 + eMinute * 60;

            Log.d(TAG, "compareDate: eSecond : " + eSecond + " sSecond : " + sSecond);

            int finalSecond = 5 * 3600;

            int differenceSecond = sSecond - eSecond;

            if (differenceSecond <= finalSecond) {
                isInvalidDate = false;
            } else {
                tvEndTime.setOnClickListener(this);
                reset();
            }
            Log.d(TAG, "compareDate: difference : " + differenceSecond);


        } catch (Exception e) {
            Log.d(TAG, "compareDate: error : " + e.getMessage());
        }


    }

    private void saveRecord() {

        if (endTimeTemp == null || endTimeTemp.length() <= 0) {
            Toast.makeText(this, "invalid end time", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if (message.length() <= 0) {
            Toast.makeText(this, "invalid message", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> notifyData = new HashMap<>();


        notifyData.put(FirebaseVar.CallNotification.START_DATE, startDateTemp);
        notifyData.put(FirebaseVar.CallNotification.START_TIME, startTimeTemp);
        notifyData.put(FirebaseVar.CallNotification.END_DATE, endDateTemp);
        notifyData.put(FirebaseVar.CallNotification.END_TIME, endTimeTemp);
        notifyData.put(FirebaseVar.CallNotification.MESSAGE, message);
        FirebaseDB.CallNotification.setCallNotification(mobileNumber, notifyData, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                Toast.makeText(CallNotificationActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                Toast.makeText(CallNotificationActivity.this, failedMessage, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void reset() {

        if (!Utils.isInternetConnectionAvailable(this)) {
            Toast.makeText(this, "check internet connection", Toast.LENGTH_SHORT).show();
        }


        ProgressDialog dialog = new ProgressDialog(this);
        if (isInvalidDate)
            dialog.setProgressTitle("time expire, removing data....");
        dialog.show();
        FirebaseDB.CallNotification.deleteCallNotification(mobileNumber, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                updateView(startDateTemp, startTimeTemp, endDateTemp, endTimeTemp, null);
                Log.d(TAG, "onUploadSuccess: message : " + message);
                dialog.dismiss();
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                Log.d(TAG, "onUploadFailed: failed Message : " + failedMessage);
            }
        });

    }


}