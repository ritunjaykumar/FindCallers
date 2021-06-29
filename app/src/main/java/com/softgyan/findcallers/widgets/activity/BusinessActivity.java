package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.GeoPoint;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BusinessActivity extends AppCompatActivity {
    private static final String TAG = "BusinessActivity";
    private EditText etName, etContact, etArea, etPin, etDistrict, etState, etShopName;
    private RadioGroup rgGender, rgDoctorType;
    private LinearLayout llMain;
    private boolean isDoctorSelected = false;
    private String dbName;

    public static Boolean isLocationEnabled(Context context) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register");

        initViewComponent();
    }

    private void initViewComponent() {
        initSpinner();
        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContactNumber);
        etArea = findViewById(R.id.tvVillage);
        etPin = findViewById(R.id.tvPincode);
        etDistrict = findViewById(R.id.etDistrict);
        etState = findViewById(R.id.etState);
        rgDoctorType = findViewById(R.id.rgDoctorType);
        rgGender = findViewById(R.id.rgGender);
        llMain = findViewById(R.id.llMain);
        etShopName = findViewById(R.id.tvshopName);
        Button btnSave = findViewById(R.id.btnSaveData);
        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        if (!Utils.isInternetConnectionAvailable(this)) {
            toastMessage("check your internet connection");
            return;
        }
        if (!isLocationEnabled(this)) {
            toastMessage("gps is not enable");
            return;
        }
        if (dbName == null || dbName.isEmpty()) {
            toastMessage("invalid DB name");
            return;
        }
        final Map<String, Object> businessRecord = getData();

        FirebaseDB.Business.saveBusinessRecord(this, dbName, businessRecord, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                toastMessage(message);
                finish();
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                toastMessage(failedMessage);
            }
        });

    }

    private void initSpinner() {
        LinearLayout llDoctorType = findViewById(R.id.llDoctorType);
        Spinner spinner = findViewById(R.id.spinner);
        String[] filters = getResources().getStringArray(R.array.businessName);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getSelectedItem().toString().equals(FirebaseVar.Business.DB_POLICE_STATION)) {
                    Utils.hideViews(llDoctorType, llMain);
                    dbName = FirebaseVar.Business.DB_POLICE_STATION;
                    toastMessage("you can't add this feature");
                } else if (parent.getSelectedItem().toString().equals(FirebaseVar.Business.DB_DOCTOR)) {
                    Utils.showViews(llDoctorType, llMain);
                    isDoctorSelected = true;
                    dbName = FirebaseVar.Business.DB_DOCTOR;
                } else if (parent.getSelectedItem().toString().equals(FirebaseVar.Business.DB_ELECTRICIAN)) {
                    Utils.hideViews(llDoctorType);
                    Utils.showViews(llMain);
                    isDoctorSelected = false;
                    dbName = FirebaseVar.Business.DB_ELECTRICIAN;
                } else {
                    Utils.hideViews(llDoctorType, llMain);
                    toastMessage("please select valid option");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //blank
            }
        });
    }

    private Map<String, Object> getData() {
        Map<String, Object> businessRecord = new HashMap<>();

        String strName = etName.getText().toString();
        String strContactNumber = etContact.getText().toString();
        String strVillage = etArea.getText().toString();
        String strPin = etPin.getText().toString();
        String strDistrict = etDistrict.getText().toString();
        String strState = etState.getText().toString();
        String strShopName = etShopName.getText().toString();
        final int rbGenderId = rgGender.getCheckedRadioButtonId();
        RadioButton rbTemp;
        if (rbGenderId == R.id.rbFemale) {
            rbTemp = findViewById(R.id.rbFemale);
        } else {
            rbTemp = findViewById(R.id.rbMale);
        }
        String strGender = rbTemp.getText().toString();

        if (isDoctorSelected) {
            final int rbDoctorType = rgDoctorType.getCheckedRadioButtonId();
            if (rbDoctorType == R.id.rbGeneralSurgeon) {
                rbTemp = findViewById(R.id.rbGeneralSurgeon);
            } else if (rbDoctorType == R.id.rbDentist) {
                rbTemp = findViewById(R.id.rbDentist);
            } else if (rbDoctorType == R.id.rbPediatricians) {
                rbTemp = findViewById(R.id.rbPediatricians);
            } else {
                rbTemp = findViewById(R.id.rbPhysicians);
            }
            String strDoctorType = rbTemp.getText().toString();
            businessRecord.put(FirebaseVar.Business.DOCTOR_TYPE, strDoctorType);
        }

        if (TextUtils.isEmpty(strName)) {
            toastMessage("name field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strContactNumber)) {
            toastMessage("Contact Number field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strVillage)) {
            toastMessage("Area/Village field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strPin)) {
            toastMessage("Pin Code field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strDistrict)) {
            toastMessage("City field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strState)) {
            toastMessage("State field is empty");
            return null;
        }
        if (TextUtils.isEmpty(strShopName)) {
            toastMessage("shop name can't be null");
            return null;
        }
        GeoPoint point = getCurrentLocation();
        businessRecord.put(FirebaseVar.Business.NAME, strName);
        businessRecord.put(FirebaseVar.Business.CONTACT, strContactNumber);
        businessRecord.put(FirebaseVar.Business.AREA, strVillage);
        businessRecord.put(FirebaseVar.Business.PIN_CODE, strPin);
        businessRecord.put(FirebaseVar.Business.STATE, strState);
        businessRecord.put(FirebaseVar.Business.DISTRICT, strDistrict);
        businessRecord.put(FirebaseVar.Business.GEO_POINT, point);
        businessRecord.put(FirebaseVar.Business.GENDER, strGender);
        businessRecord.put(FirebaseVar.Business.SHOP_NAME, strShopName);

        return businessRecord;
    }

    private void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public GeoPoint getCurrentLocation() {

        Log.d("Find Location", "in find_location");
        if (!checkPermission()) {
            toastMessage("Don't have permission to access location");
            return null;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d(TAG, "find_Location: latitude : " + latitude + " longitude : " + longitude);

                return new GeoPoint(latitude, longitude);
            }
        }
        return null;
    }

    private boolean checkPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!Utils.hasPermissions(this, permission)) {
            ActivityCompat.requestPermissions(this, permission, 100);
        }
        return Utils.checkPermission(this, permission);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}