package com.softgyan.findcallers.widgets.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.firebase.FirebaseVar;

public class FilterActivity extends AppCompatActivity {
    public static final String FILTER_KEY = "filterKey";
    public static final String DISTANCE_KEY = "distanceKey";
    private static final String TAG = "FilterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        initViewComponent();
    }

    private void initViewComponent() {
        Spinner spinner = findViewById(R.id.spinner);
        String[] optionList = getResources().getStringArray(R.array.businessName);
        ArrayAdapter<String> aAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, optionList);
        spinner.setAdapter(aAdapter);

        String[] distanceList = getResources().getStringArray(R.array.distance);
        Spinner sDistance = findViewById(R.id.sDistance);
        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, distanceList);
        sDistance.setAdapter(bAdapter);

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String filterKey = (String) spinner.getSelectedItem();
            String strDistance = (String) sDistance.getSelectedItem();
            final String[] s = strDistance.split(" ");
            try {
                final int i = Integer.parseInt(s[0]);
                returnSearchActivity(filterKey, i);
            } catch (Exception e) {
                Log.d(TAG, "initViewComponent: error " + e.getMessage());
            }
        });


        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(FILTER_KEY) != null) {
                spinner.setPrompt(intent.getStringExtra(FILTER_KEY));
            }
            if (intent.getIntExtra(DISTANCE_KEY, -1) != -1) {
                spinner.setPrompt(intent.getIntExtra(DISTANCE_KEY, -1) + " km");
            }
        }
    }


    private void returnSearchActivity(@NonNull String filterKey, int distance) {
        Intent intent = new Intent();
        switch (filterKey) {
            case (FirebaseVar.Business.DOCTOR):
            case (FirebaseVar.Business.ELECTRICIAN): {
                intent.putExtra(FILTER_KEY, filterKey);
                intent.putExtra(DISTANCE_KEY, distance);
                break;
            }
            default: {
                Log.d(TAG, "returnSearchActivity: working on this feature");
            }
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}