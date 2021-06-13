package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.models.BusinessRecord;
import com.softgyan.findcallers.models.DoctorModel;
import com.softgyan.findcallers.models.ElectricianModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.BusinessRecordAdapter;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.List;
import java.util.Objects;

public class SearchImportantNumberActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SearchImportantNumberActivity";
    public static final int FILTER_ACTIVITY_REQUEST_CODE = 100;
    private String filterValue = null;
    private int distance = -1;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_important_number);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        dialog = new ProgressDialog(this);
        initViewComponent();
    }

    private void initViewComponent() {
        Button btnSearch = findViewById(R.id.btnSearch);
        TextView tvBusinessAccount = findViewById(R.id.tvBusinessAccount);
        btnSearch.setOnClickListener(this);
        tvBusinessAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSearch) {
            if (!checkPermission()) {
                toastMessage("make sure you gave Location Permission");
                return;
            }

            if (!Utils.isInternetConnectionAvailable(this)) {
                toastMessage("check internet connection");
                return;
            }
            if (!isLocationEnabled(this)) {
                toastMessage("check gps is enable");
                return;
            }

            Intent intent = new Intent(this, FilterActivity.class);
            if (filterValue != null) {
                intent.putExtra("filterValue", filterValue);
            }
            if (distance != -1) {
                intent.putExtra("distance", distance);
            }
            ActivityCompat.startActivityForResult(this, intent, FILTER_ACTIVITY_REQUEST_CODE, null);
        } else if (id == R.id.tvBusinessAccount) {
            Intent intent = new Intent(this, BusinessActivity.class);
            startActivity(intent);
        }
    }

    private void setupRecyclerView(BusinessRecord businessRecord) {
        Log.d(TAG, "setupRecyclerView: business Record : " + businessRecord);
        RecyclerView rvDetails = findViewById(R.id.rvDetails);
        BusinessRecordAdapter businessRecordAdapter = new BusinessRecordAdapter(this, businessRecord);
        rvDetails.setAdapter(businessRecordAdapter);
        businessRecordAdapter.notifyDataSetChanged();

    }


    public void getCurrentGeoPoint(String filterValue, int distance) {
        Log.d("Find Location", "in find_location");
        if (!checkPermission()) {
            return;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d(TAG, "find_Location: latitude : " + latitude + " longitude : " + longitude);
                getDataFromServer(latitude, longitude, filterValue, distance);
                break;
            } else {
                Log.d(TAG, "getCurrentGeoPoint: get location value is null");
            }
        }
    }

    private void getDataFromServer(double latitude, double longitude, String filterValue, int range) {
        switch (filterValue) {
            case (FirebaseVar.Business.DOCTOR): {
                Log.d(TAG, "getDataFromServer: getting Doctor record");
                getDoctorRecord(range, latitude, longitude);
                break;
            }
            case (FirebaseVar.Business.ELECTRICIAN): {
                Log.d(TAG, "getDataFromServer: getting Business Record");
                getElectricianRecord(range, latitude, longitude);
                break;
            }
        }
    }


    private synchronized void getDoctorRecord(int range, double lat, double lan) {
        Log.d(TAG, "getDoctorRecord: searching..");
        dialog.setProgressTitle("Getting doctor records");
        dialog.show();
        FirebaseDB.Business.getDoctorRecord(range, lat, lan, new OnResultCallback<List<DoctorModel>>() {
            @Override
            public void onSuccess(@NonNull List<DoctorModel> doctorModels) {
                dialog.dismiss();
                Log.d(TAG, "onSuccess: doctor model " + doctorModels);
                setupRecyclerView(new BusinessRecord(BusinessRecord.DOCTOR_TYPE, doctorModels));
            }

            @Override
            public void onFailed(String failedMessage) {
                dialog.dismiss();
                Toast.makeText(SearchImportantNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private synchronized void getElectricianRecord(int range, double lat, double lan) {
        FirebaseDB.Business.getElectricianRecord(range, lat, lan, new OnResultCallback<List<ElectricianModel>>() {
            @Override
            public void onSuccess(@NonNull List<ElectricianModel> electricianModels) {
                Log.d(TAG, "onSuccess: electrician model : " + electricianModels);
                setupRecyclerView(new BusinessRecord(electricianModels, BusinessRecord.ELECTRICIAN_TYPE));
            }

            @Override
            public void onFailed(String failedMessage) {
                Toast.makeText(SearchImportantNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String filterValue = data.getStringExtra(FilterActivity.FILTER_KEY);
                this.filterValue = filterValue;
                int distance = data.getIntExtra(FilterActivity.DISTANCE_KEY, -1);
                this.distance = distance;
                Log.d(TAG, "onActivityResult: distance : " + distance + " filter value : " + filterValue);
                if (filterValue != null && distance != -1) {
                    getCurrentGeoPoint(filterValue, distance);
                }

            }
        }


    }


    private boolean checkPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!Utils.hasPermissions(this, permission)) {
            ActivityCompat.requestPermissions(this, permission, 100);
        }
        return Utils.checkPermission(this, permission);
    }

    private Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}