package com.softgyan.findcallers.widgets.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.services.SimDetectorService;
import com.softgyan.findcallers.widgets.dialog.SingleValueDialog;

import java.util.List;
import java.util.Objects;

public class FindMobileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FindMobileActivity";
    private TextView tvFirstNumber, tvSecondNumber, tvMessage;
    private Button btnStartService;
    private SimDetectorService simDetectorService;
    private boolean mBind;


    final private List<SimCardInfoModel> simInfoS = SimCardInfoModel.getSimInfoS(this);
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBind = true;
            SimDetectorService.SimDetectorServiceBinder binder = (SimDetectorService.SimDetectorServiceBinder) service;
            simDetectorService = binder.getSimDetectorService();
            Log.d(TAG, "onServiceConnected: connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBind = false;
            simDetectorService = null;
            Log.d(TAG, "onServiceDisconnected: not connected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_mobile);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Find mobile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onCreate: simInfo :" + simInfoS.toString());

        try {
            String[] previousSimInfo = AppPreference.SimPreference.getSimIccS(getApplicationContext());
            Log.d(TAG, "onCreate: previous sim info size : " + previousSimInfo.length);
        } catch (Exception e) {
            Log.d(TAG, "onCreate: error :" + e.getMessage());
        }

        initViewComponent();
        registerBoundService();
    }

    private void initViewComponent() {
        tvFirstNumber = findViewById(R.id.tvFirstNumber);
        tvSecondNumber = findViewById(R.id.tvSecondNumber);
        tvMessage = findViewById(R.id.tvMessage);
        TextView tvSimInfo = findViewById(R.id.tvSimInfo);
        btnStartService = findViewById(R.id.btnStartService);
        tvFirstNumber.setOnClickListener(this);
        tvSecondNumber.setOnClickListener(this);
        tvMessage.setOnClickListener(this);
        btnStartService.setOnClickListener(this);
        Button btnDefaultMessage = findViewById(R.id.btnDefaultMessage);
        btnDefaultMessage.setOnClickListener(this);

        final String firstNumber = AppPreference.SimPreference.getFirstNumber(this);
        final String secondNumber = AppPreference.SimPreference.getSecondNumber(this);
        final String defaultMessage = AppPreference.SimPreference.getSimMessage(this);
        if (firstNumber != null) tvFirstNumber.setText(firstNumber);
        if (secondNumber != null) tvSecondNumber.setText(secondNumber);
        if (defaultMessage != null) tvMessage.setText(defaultMessage);

        if (simInfoS == null) {
            tvSimInfo.setText("There are no any Sims detected");
        } else {
            if (getSimInfoStr() != null) {
                tvSimInfo.setText("Sim info\n");
                tvSimInfo.append(getSimInfoStr());
            }
        }

    }


    private void registerBoundService() {
        try {
            Intent intent = new Intent(this, SimDetectorService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.d(TAG, "onStart: error : " + e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (simDetectorService != null) {
            if (simDetectorService.isServiceStart()) {
                clickOnBtnStart(true);
            } else {
                clickOnBtnStart(false);
            }
        } else {
            Log.d(TAG, "onStart: bindService null");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        SingleValueDialog singleValueDialog;
        if (id == R.id.btnDefaultMessage) {
            try {
                AppPreference.SimPreference.setSimMessage(FindMobileActivity.this, getDefaultMessage());
                tvMessage.setText(getDefaultMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onClick: error message : " + e.getMessage());
            }

        } else if (id == R.id.tvFirstNumber) {
            singleValueDialog = new SingleValueDialog(this, 1, dialogCallback);
            singleValueDialog.setTitle("First number");
            singleValueDialog.setHint("Insert first number");
            String tempNum1 = tvFirstNumber.getText().toString();
            if (tempNum1.length() != 0) {
                singleValueDialog.setTextValue(tempNum1);
            }
            singleValueDialog.show();
        } else if (id == R.id.tvSecondNumber) {
            singleValueDialog = new SingleValueDialog(this, 2, dialogCallback);
            singleValueDialog.setTitle("Second number");
            singleValueDialog.setHint("Insert second number");
            String tempNum2 = tvSecondNumber.getText().toString();
            if (tempNum2.length() != 0) {
                singleValueDialog.setTextValue(tempNum2);
            }
            singleValueDialog.show();
        } else if (id == R.id.tvMessage) {
            singleValueDialog = new SingleValueDialog(this,
                    SingleValueDialog.DialogOption.MULTIPLE_LINE, 3, dialogCallback);
            singleValueDialog.setTitle("Message");
            singleValueDialog.setHint("Add message");
            String tempMsg = tvMessage.getText().toString();
            if (tempMsg.length() != 0) {
                singleValueDialog.setTextValue(tempMsg);
            }

            singleValueDialog.show();
        } else if (id == R.id.btnStartService) {
            Log.d(TAG, "onClick: isServiceStart : " + simDetectorService.isServiceStart());
            if (!simDetectorService.isServiceStart()) {
                Intent serviceIntent = new Intent(this, SimDetectorService.class);
                serviceIntent.putExtra(SimDetectorService.TASK_KEY, SimDetectorService.INIT_TASK_VALUE);
                ContextCompat.startForegroundService(this, serviceIntent);
                clickOnBtnStart(true);
            } else {
                simDetectorService.stopSimDetectorService();
                if (mBind) {
                    unbindService(serviceConnection);
                    mBind = false;
                }
                clickOnBtnStart(false);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mBind) {
                unbindService(serviceConnection);
                mBind = false;
            }
        } catch (Exception e) {
            Log.d(TAG, "onStop: error : " + e.getMessage());
        }
    }

    private final SingleValueDialog.SingleValueDialogCallback dialogCallback = new SingleValueDialog.SingleValueDialogCallback() {
        @Override
        public void onGetValue(@NonNull final SingleValueDialog dialog, int requestCode, @NonNull String value) {
            switch (requestCode) {
                case 1:
                    tvFirstNumber.setText(value);
                    AppPreference.SimPreference.setFirstNumber(FindMobileActivity.this, value);
                    break;
                case 2:
                    tvSecondNumber.setText(value);
                    AppPreference.SimPreference.setSecondNumber(FindMobileActivity.this, value);
                    break;
                case 3:
                    tvMessage.setText(value);
                    AppPreference.SimPreference.setSimMessage(FindMobileActivity.this, value);
            }
            dialog.dismiss();
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private String getDefaultMessage() {
        final String[] charNumbers = {"First", "Second", "Third", "Fourth", "fifth"};
        if (simInfoS == null) {
            Toast.makeText(this, "No sim detected", Toast.LENGTH_SHORT).show();
            return null;
        }
        final String[] mobileNumbers = new String[simInfoS.size()];
        for (int i = 0; i < simInfoS.size(); i++) {
            mobileNumbers[i] = simInfoS.get(i).getMobileNumber();
        }

        StringBuilder defaultMessageBuilder = new StringBuilder();
        defaultMessageBuilder.append("Mr. %s, Your phone may be lost. you can track your phone at given information..\n");

        for (int i = 0; i < simInfoS.size(); i++) {
            defaultMessageBuilder.append(charNumbers[i]).append(" : ")
                    .append(mobileNumbers[i]).append('\n');
        }
        return defaultMessageBuilder.toString();
    }


    private String getSimInfoStr() {
        if (simInfoS != null) {
            StringBuilder sb = new StringBuilder();
            for (SimCardInfoModel cardInfo : simInfoS) {
                sb.append("Mobile number : ").append(cardInfo.getMobileNumber()).append('\n');
                sb.append("Display Name : ").append(cardInfo.getDisplayName()).append('\n');
                sb.append("Carrier Name : ").append(cardInfo.getCarrierName()).append('\n');
                sb.append("Icc Code : ").append(cardInfo.getIccId()).append('\n');
                sb.append(".............").append('\n');
            }
            return sb.toString();
        }
        return null;
    }


    private void clickOnBtnStart(boolean isRunning) {
        if (isRunning) {
            btnStartService.setText("Service already running, Stop");
            btnStartService.setTextColor(getResources().getColor(R.color.colorGreen, null));
        } else {
            btnStartService.setText("Start Service");
            btnStartService.setTextColor(getResources().getColor(R.color.white, null));
        }
    }


}