package com.softgyan.findcallers.widgets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.call.system.SystemCalls;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.preferences.AppPreference;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        initComponent();
        tempCall();
    }

    private void tempCall() {
        Intent intent = new Intent(this, AccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void initComponent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivity();
            }
        }, 1000);
    }

    private void openActivity() {
        Intent intent;
        if (!AppPreference.isWelcomeActivitySet(this)) {
            intent = new Intent(this, WelcomeActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}