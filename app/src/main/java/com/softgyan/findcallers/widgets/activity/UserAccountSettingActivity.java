package com.softgyan.findcallers.widgets.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.softgyan.findcallers.R;

public class UserAccountSettingActivity extends AppCompatActivity {

    public static final String DELETE_ACCOUNT_SHOW = "deleteAccountShow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account_setting);
    }
}