package com.softgyan.findcallers.temp;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.softgyan.findcallers.R;

public class TempCallActivity extends AppCompatActivity {
    private  AlertDialog.Builder builder;
    private AlertDialog aDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_caller_info);
    }


}