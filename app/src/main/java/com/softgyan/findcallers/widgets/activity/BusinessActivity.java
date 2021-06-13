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
import com.softgyan.findcallers.models.BusinessRecord;
import com.softgyan.findcallers.models.DoctorModel;
import com.softgyan.findcallers.models.ElectricianModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusinessActivity extends AppCompatActivity {
    private static final String TAG = "BusinessActivity";
    private Spinner spinner;
    private EditText etName, etContact, etArea, etPin, etDistrict, etState;
    private RadioGroup rgGender, rgDoctorType;
    private boolean isDoctorSelected = false;

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
        Button btnSave = findViewById(R.id.btnSaveData);
        btnSave.setOnClickListener(v -> {
            saveRecord();
        });
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

        final BusinessRecord data = getData();
        Log.d(TAG, "saveRecord: data : " + data);
        if (data == null) {
            return;
        }
        if (data.getType() == BusinessRecord.DOCTOR_TYPE) {
            FirebaseDB.Business.uploadDoctorRecord(data.getDoctorList().get(0), new OnUploadCallback() {
                @Override
                public void onUploadSuccess(String message) {
                    toastMessage("saved data");
                }

                @Override
                public void onUploadFailed(String failedMessage) {
                    toastMessage(failedMessage);
                }
            });
        } else if (data.getType() == BusinessRecord.ELECTRICIAN_TYPE) {
            FirebaseDB.Business.uploadElectricianRecord(data.getElectricianList().get(0), new OnUploadCallback() {
                @Override
                public void onUploadSuccess(String message) {
                    toastMessage("saved data");
                }

                @Override
                public void onUploadFailed(String failedMessage) {
                    toastMessage(failedMessage);
                }
            });
        }
    }

    private void initSpinner() {
        LinearLayout llDoctorType = findViewById(R.id.llDoctorType);
        spinner = findViewById(R.id.spinner);
        String[] filters = getResources().getStringArray(R.array.businessName);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getSelectedItem().toString().equals(FirebaseVar.Business.POLICE_STATION)) {
                    toastMessage("you can't add this feature");
                    return;
                }

                if (parent.getSelectedItem().toString().equals(FirebaseVar.Business.DOCTOR)) {
                    Utils.showViews(llDoctorType);
                    isDoctorSelected = true;
                } else {
                    Utils.hideViews(llDoctorType);
                    isDoctorSelected = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private BusinessRecord getData() {
        String strName = etName.getText().toString();
        String strContactNumber = etContact.getText().toString();
        String strVillage = etArea.getText().toString();
        String strPin = etPin.getText().toString();
        String strDistrict = etDistrict.getText().toString();
        String strState = etState.getText().toString();
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
        }
        String strDoctorType = rbTemp.getText().toString();

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

        BusinessRecord businessRecord;
        GeoPoint point = getCurrentLocation();
        if (isDoctorSelected) {
            List<DoctorModel> doctorModelList = new ArrayList<>();
            DoctorModel doctorModel = new DoctorModel(strVillage, strPin, strState, strDistrict,
                    point, null, strName, strGender, strContactNumber, strDoctorType);
            doctorModelList.add(doctorModel);
            businessRecord = new BusinessRecord(BusinessRecord.DOCTOR_TYPE, doctorModelList);

        } else {
            List<ElectricianModel> electricianModels = new ArrayList<>();
            ElectricianModel electricianModel = new ElectricianModel(strVillage, strPin, strState,
                    strDistrict, point, null, strName, strGender, strContactNumber);
            electricianModels.add(electricianModel);
            businessRecord = new BusinessRecord(electricianModels, BusinessRecord.ELECTRICIAN_TYPE);
        }

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