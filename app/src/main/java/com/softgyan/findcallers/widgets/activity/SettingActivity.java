package com.softgyan.findcallers.widgets.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.Utils;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnCallNotification;
    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViewComponent();
    }

    private void initViewComponent() {
        btnCallNotification = findViewById(R.id.btnCallNotification);
        btnCallNotification.setOnClickListener(this);
        SwitchCompat switchCompat = findViewById(R.id.switch1);
        boolean isSet = AppPreference.getCallNotification(this);
        if (isSet) {
            switchCompat.setChecked(true);
            Utils.showViews(btnCallNotification);
        } else {
            Utils.hideViews(btnCallNotification);
        }
        switchCompat.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            Log.d(TAG, "initViewComponent: isChecked : " + isChecked);


            if (isChecked) {
                Utils.showViews(btnCallNotification);
            } else {
                if (!Utils.isInternetConnectionAvailable(this)) {
                    Toast.makeText(SettingActivity.this, "check internet connection", Toast.LENGTH_SHORT).show();
                    switchCompat.setChecked(true);
                    return;
                } else {
                    delete();
                }
                Utils.hideViews(btnCallNotification);
            }

            AppPreference.setCallNotification(SettingActivity.this, isChecked);

        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnCallNotification) {
            startActivityCompat();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void startActivityCompat() {
        Intent intent = new Intent(this, CallNotificationActivity.class);
        startActivity(intent);
    }

    private synchronized void delete() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "you are not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String mobileNumber = currentUser.getPhoneNumber();
        FirebaseDB.CallNotification.deleteCallNotification(mobileNumber, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {

            }

            @Override
            public void onUploadFailed(String failedMessage) {
                Log.d(TAG, "onUploadFailed: failed Message : " + failedMessage);
            }
        });
    }
}