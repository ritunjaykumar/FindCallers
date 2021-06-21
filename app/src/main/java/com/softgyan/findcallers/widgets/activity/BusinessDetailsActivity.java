package com.softgyan.findcallers.widgets.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.models.BusinessRecord;
import com.softgyan.findcallers.models.DoctorModel;
import com.softgyan.findcallers.models.ElectricianModel;
import com.softgyan.findcallers.utils.Common;
import com.softgyan.findcallers.utils.Utils;

import java.util.Objects;

public class BusinessDetailsActivity extends AppCompatActivity {
    private TextView tvName, tvContactNumber, tvVillage, tvPincode, tvCity, tvState, tvGender, tvDoctorType, tvDistance;
    private static final String TAG = "BusinessDetailsActivity";

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
        }

        DoctorModel doctorModel = Common.doctorModel;
        ElectricianModel electricianModel = Common.electricianModel;
        int type = Common.type;

        String name = null, contactNumber = null, village = null, pincode = null, city = null,
                state = null, gender = null, doctorType = null;
        double distance = 0;

        if (doctorModel == null && electricianModel == null) {
            Toast.makeText(this, "some thing is wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (type == BusinessRecord.DOCTOR_TYPE) {
            name = doctorModel.getName();
            contactNumber = doctorModel.getContact();
            village = doctorModel.getArea();
            pincode = doctorModel.getPinCode();
            city = doctorModel.getDistrict();
            state = doctorModel.getState();
            gender = doctorModel.getGender();
            doctorType = doctorModel.getDoctorType();
            distance = doctorModel.getDistance();
        } else if (type == BusinessRecord.ELECTRICIAN_TYPE) {
            name = electricianModel.getName();
            contactNumber = electricianModel.getContact();
            village = electricianModel.getArea();
            pincode = electricianModel.getPinCode();
            city = electricianModel.getDistrict();
            state = electricianModel.getState();
            gender = electricianModel.getGender();
            distance = electricianModel.getDistance();
        }

        initComponentView();

        if (type == BusinessRecord.DOCTOR_TYPE || type == BusinessRecord.ELECTRICIAN_TYPE) {
            setDataToView(name, contactNumber, village, pincode, city, state, gender, distance, doctorType);
        }
    }

    private void initComponentView() {
        try {
            tvName = findViewById(R.id.tvName);
            tvContactNumber = findViewById(R.id.tvContactNumber);
            tvVillage = findViewById(R.id.tvVillage);
            tvPincode = findViewById(R.id.tvPincode);
            tvCity = findViewById(R.id.tvCity);
            tvState = findViewById(R.id.tvState);
            tvGender = findViewById(R.id.tvGender);
            tvDoctorType = findViewById(R.id.tvDoctorType);
            tvDistance = findViewById(R.id.tvDistance);
        } catch (Exception e) {
            Log.d(TAG, "initComponentView: error " + e.getMessage());
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Common.clearValue();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDataToView(String name, String contactNumber, String village, String pincode,
                               String city, String state, String gender, double distance, String doctorType) {
        try {
            if (doctorType == null) {
                Utils.hideViews(tvDoctorType);
            } else {
                tvDoctorType.setText(doctorType);
            }
            tvName.setText(name);
            tvContactNumber.setText(contactNumber);
            tvVillage.setText(village);
            tvPincode.setText(pincode);
            tvCity.setText(city);
            tvGender.setText(gender);
            tvState.setText(state);
            tvDistance.setText(String.valueOf(distance));


        } catch (Exception e) {
            Log.d(TAG, "setDataToView: error : " + e.getMessage());
        }
    }


}