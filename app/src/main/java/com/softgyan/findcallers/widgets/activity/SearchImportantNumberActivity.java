package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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
        dialog = new ProgressDialog(this);
        initViewComponent();
    }

    private void initViewComponent() {
        Button btnSearch = findViewById(R.id.btnSearch);
        TextView tvBusinessAccount = findViewById(R.id.tvBusinessAccount);
        btnSearch.setOnClickListener(this);
        tvBusinessAccount.setOnClickListener(this);
    }

    private void setupRecyclerView(BusinessRecord businessRecord) {
        RecyclerView rvDetails = findViewById(R.id.rvDetails);
        BusinessRecordAdapter businessRecordAdapter = new BusinessRecordAdapter(this, businessRecord);
        rvDetails.setAdapter(businessRecordAdapter);
        businessRecordAdapter.notifyDataSetChanged();

    }


    public void getCurrentLocation(String filterValue, int distance) {

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
            }
        }
    }

    private void getDataFromServer(double latitude, double longitude, String filterValue, int range) {
        switch (filterValue) {
            case (FirebaseVar.Business.DOCTOR): {
                getDoctorRecord(range, latitude, longitude);
                break;
            }
            case (FirebaseVar.Business.ELECTRICIAN): {
                getElectricianRecord(range, latitude, longitude);
                break;
            }
        }
    }


    private synchronized void getDoctorRecord(int range, double lat, double lan) {
        FirebaseDB.Business.getDoctorRecord(range, lat, lan, new OnResultCallback<List<DoctorModel>>() {
            @Override
            public void onSuccess(@NonNull List<DoctorModel> doctorModels) {
                setupRecyclerView(new BusinessRecord(BusinessRecord.DOCTOR_TYPE, doctorModels));
            }

            @Override
            public void onFailed(String failedMessage) {
                Toast.makeText(SearchImportantNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private synchronized void getElectricianRecord(int range, double lat, double lan) {
        FirebaseDB.Business.getElectricianRecord(range, lat, lan, new OnResultCallback<List<ElectricianModel>>() {
            @Override
            public void onSuccess(@NonNull List<ElectricianModel> electricianModels) {
                setupRecyclerView(new BusinessRecord(electricianModels, BusinessRecord.ELECTRICIAN_TYPE));
            }

            @Override
            public void onFailed(String failedMessage) {
                Toast.makeText(SearchImportantNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSearch) {
            Intent intent = new Intent(this, FilterActivity.class);
            ActivityCompat.startActivityForResult(this, intent, FILTER_ACTIVITY_REQUEST_CODE, null);
        }else if(id == R.id.tvBusinessAccount){
            Intent intent = new Intent(this, BusinessActivity.class);
            startActivity(intent);
        }
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

                if (filterValue != null && distance != -1) {
                    getCurrentLocation(filterValue, distance);
                }


            }
        }


    }


    /*private void getCurrentLocation(String filterValue) {
        Log.d(TAG, "getCurrentLocation: called");
        if (!checkPermission()) return;
        dialog.setProgressTitle("getting current location");
        dialog.show();
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            Log.d(TAG, "onComplete: location : " + location);
            if (location != null) {
                Geocoder geocoder = new Geocoder(SearchImportantNumberActivity.this, Locale.getDefault());
                Log.d(TAG, "onComplete: geocoder : " + geocoder);
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );
                    Log.d(TAG, "onComplete: list of Address : " + addresses);
                    final double latitude = addresses.get(0).getLatitude();
                    final double longitude = addresses.get(0).getLongitude();
                    Log.d(TAG, "onComplete: latitude : " + latitude + " longitude : " + longitude);

                    switch (filterValue) {
                        case (FirebaseVar.Business.DOCTOR):
                            getDoctorRecord(distance, latitude, longitude);
                            break;
                        case (FirebaseVar.Business.ELECTRICIAN): {
                            getElectricianRecord(distance, latitude, longitude);
                        }
                    }


                } catch (IOException e) {
                    Log.d(TAG, "onComplete: error  : " + e.getMessage());
                    dialog.dismiss();
                }
                dialog.dismiss();

            } else {
                Log.d(TAG, "onSuccess: location is null");
            }
            dialog.dismiss();
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: error : " + e.getMessage());
                dialog.dismiss();
            }
        });

    }*/

    private boolean checkPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!Utils.hasPermissions(this, permission)) {
            ActivityCompat.requestPermissions(this, permission, 100);
        }
        return Utils.checkPermission(this, permission);
    }
}