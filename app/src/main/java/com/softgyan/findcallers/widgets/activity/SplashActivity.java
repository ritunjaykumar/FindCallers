package com.softgyan.findcallers.widgets.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.models.SimCardInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getName();
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "onCreate: subscriptions : "+ SimCardInfoModel.getSimInfoS(this));
        firebaseAuth = FirebaseAuth.getInstance();
        initComponent();



    }


    private void initComponent() {
        new Handler().postDelayed(this::openActivity, 1000);
    }

    private void openActivity() {
        Intent intent;
        if (firebaseAuth.getCurrentUser() == null) {
            intent = new Intent(this, AccountActivity.class);
        } else
        if (!AppPreference.isWelcomeActivitySet(this)) {
            intent = new Intent(this, WelcomeActivity.class);
        } else if (!AppPreference.isAccountActivitySet(this)) {
            intent = new Intent(this, UserAccountSettingActivity.class);
            intent.putExtra(UserAccountSettingActivity.IS_SAVE_DATA, true);
        } else {
            intent = new Intent(this, MainActivity.class);

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}