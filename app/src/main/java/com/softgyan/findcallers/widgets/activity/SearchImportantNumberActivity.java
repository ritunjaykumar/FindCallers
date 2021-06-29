package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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

import com.google.firebase.firestore.GeoPoint;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.BusinessRecordAdapter;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchImportantNumberActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SearchImportantNumberActivity";
    public static final int FILTER_ACTIVITY_REQUEST_CODE = 100;
    private ProgressDialog dialog;
    private volatile boolean isCancelByUser = false;

    private GeoPoint geoPoint;

    private RecyclerView rvDetails;
    private TextView tvMessage;

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
        tvMessage = findViewById(R.id.tvMessage);
        TextView tvBusinessAccount = findViewById(R.id.tvBusinessAccount);
        btnSearch.setOnClickListener(this);
        tvBusinessAccount.setOnClickListener(this);
        rvDetails = findViewById(R.id.rvDetails);
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

            isCancelByUser = false;
            LocationLoader locationLoader = new LocationLoader(true);
            locationLoader.execute();

        } else if (id == R.id.tvBusinessAccount) {

            LocationLoader locationLoader = new LocationLoader(false);
            locationLoader.execute();

        }
    }

    private void setupRecyclerView(List<Map<String, Object>> businessRecord) {
        Log.d(TAG, "setupRecyclerView: business Record : " + businessRecord);
        BusinessRecordAdapter businessRecordAdapter = new BusinessRecordAdapter(this, businessRecord);
        rvDetails.setAdapter(businessRecordAdapter);
        businessRecordAdapter.notifyDataSetChanged();

    }


    private void getDataFromServer(String filterValue, int range) {
        FirebaseDB.Business.getBusinessRecord(this, filterValue, range, geoPoint,
                new OnResultCallback<List<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(@NonNull List<Map<String, Object>> maps) {
                        Log.d(TAG, "onSuccess: map value : " + maps);
                        setupRecyclerView(maps);
                    }

                    @Override
                    public void onFailed(String failedMessage) {
                        toastMessage(failedMessage);
                        setupRecyclerView(new ArrayList<>());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String filterValue = data.getStringExtra(FilterActivity.FILTER_KEY);
                int distance = data.getIntExtra(FilterActivity.DISTANCE_KEY, -1);
                Log.d(TAG, "onActivityResult: distance : " + distance + " filter value : " + filterValue);
                if (filterValue != null && distance != -1) {
                    getDataFromServer(filterValue, distance);
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

    private final ProgressDialog.OnCancelListener cancelListener = dialog -> {
        isCancelByUser = true;
        dialog.dismiss();
    };


    private final class LocationLoader extends AsyncTask<Void, Void, GeoPoint> {
        private final boolean isSearch;

        public LocationLoader(boolean isSearch) {
            this.isSearch = isSearch;
        }

        ProgressDialog progressDialog = new ProgressDialog(SearchImportantNumberActivity.this,
                cancelListener);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressTitle("Getting current location..");
            progressDialog.show();
        }

        @Override
        protected GeoPoint doInBackground(Void... voids) {
            GeoPoint geoPoint;
            while (true) {
                geoPoint = getGeoPoint();
                if (geoPoint != null || isCancelByUser) {
                    break;
                }
            }
            return geoPoint;
        }

        @Override
        protected void onPostExecute(GeoPoint geoPoint) {
            super.onPostExecute(geoPoint);
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: geo point : " + geoPoint);
            if (geoPoint == null) {
                finish();
                toastMessage("you can't get current location");
            } else {
                SearchImportantNumberActivity.this.geoPoint = geoPoint;
                startActivity(isSearch);
            }
        }
    }

    private synchronized GeoPoint getGeoPoint() {
        GeoPoint geoPoint = null;
        Log.d("Find Location", "in find_location");
        if (!checkPermission()) {
            return null;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                geoPoint = new GeoPoint(latitude, longitude);
                Log.d(TAG, "find_Location: latitude : " + latitude + " longitude : " + longitude);
                break;
            }
        }
        return geoPoint;
    }

    private void startActivity(boolean isSearch) {
        if (isSearch) {
            Intent intent = new Intent(this, FilterActivity.class);
            ActivityCompat.startActivityForResult(this, intent, FILTER_ACTIVITY_REQUEST_CODE, null);
        } else {
            Intent intent = new Intent(this, BusinessActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}