package com.softgyan.findcallers.widgets.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.utils.CallUtils;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class BusinessDetailsActivity extends AppCompatActivity {
    private static final String TAG = "BusinessDetailsActivity";
    public static final String DATA_KEY = "dataKey";
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Business Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.d(TAG, "onCreate: error : " + e.getMessage());
        }
        linearLayout = findViewById(R.id.llMain);
        TextView tvTitle = findViewById(R.id.tvTitle);
        try {
            if (getIntent() != null) {
                HashMap<String, Object> businessRecord = (HashMap<String, Object>) getIntent().getSerializableExtra(DATA_KEY);
                Log.d(TAG, "onCreate: business Record : " + businessRecord);
                String title = (String) businessRecord.get(FirebaseVar.Business.DB_TYPE_KEY);
                tvTitle.setText(title);
                addLayout(businessRecord);
            }
        } catch (Exception e) {
            Log.d(TAG, "onCreate: error : " + e.getMessage());
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addLayout(HashMap<String, Object> businessRecord) {
        final Set<String> keys = businessRecord.keySet();
        for (String key : keys) {
            addNewLayout(key, String.valueOf(businessRecord.get(key)));
        }
    }


    private void addNewLayout(final String key, final String value) {
        if (FirebaseVar.Business.DB_TYPE_KEY.equals(key)) {
            return;
        }
        final View newView = LayoutInflater.from(this).inflate(R.layout.layout_key_value, null, false);
        TextView tvKey = newView.findViewById(R.id.tvKey);
        TextView tvValue = newView.findViewById(R.id.tvValue);
        tvKey.setText(key);
        tvValue.setText(value);
        if(FirebaseVar.Business.CONTACT.equals(key)){
            newView.setOnClickListener(v->{
                CallHardware.doCall(this, value);
            });
        }
        linearLayout.addView(newView);
    }


}