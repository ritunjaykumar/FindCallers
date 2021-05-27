package com.softgyan.findcallers.widgets.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.firebase.Authentication;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.Objects;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AccountActivity";
    private EditText etNumber, etOtp;
    private Button btnReset, btnClearNumber, btnSendOtp, btnClearOtp, btnVerifyOtp;
    private Authentication mAuth;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account");
        initView();
        mAuth = new Authentication(this);
        mDialog = new ProgressDialog(this);
        mDialog.setProgressTitle("Sending Otp..");
    }


    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        switch (vId) {
            case R.id.btnSendOtp: {
                final String mobileNumber = getMobileNumber();
                if (mobileNumber == null) return;
                mDialog.setProgressTitle("Sending Otp..");
                mDialog.show();
                //view
                Utils.hideViews(btnClearNumber, btnSendOtp);
                Utils.disableViews(etNumber);

                mAuth.onPhoneVerify(mobileNumber, authCallback);
                break;
            }
            case R.id.btnVerifyOtp: {
                final String otp = getOtp();
                if (otp == null) {
                    Utils.toastMessage(AccountActivity.this, "Please provide correct otp");
                    return;
                }
                mDialog.setTitle("wait...");
                mDialog.show();
                mAuth.verifyOtp(otp);
                Utils.hideViews(btnClearOtp, btnVerifyOtp);
                break;
            }
            case R.id.btnClearNumber:{
                etNumber.setText("");
                break;
            }
            case R.id.btnClearOtp:{
                etOtp.setText("");
                break;
            }
            case R.id.btnReset:{
                mAuth = new Authentication(AccountActivity.this);
                Utils.hideViews(etOtp, btnSendOtp, btnSendOtp, btnReset);
                Utils.showViews(btnClearNumber, btnSendOtp);
                Utils.enableViews(etNumber);
            }
        }

    }

    //-----------------manual function ------------------
    private void initView() {
        etNumber = findViewById(R.id.etNumber);
        btnClearNumber = findViewById(R.id.btnClearNumber);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnReset = findViewById(R.id.btnReset);
        etOtp = findViewById(R.id.etOtp);
        btnClearOtp = findViewById(R.id.btnClearOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnVerifyOtp.setOnClickListener(this);
        btnClearOtp.setOnClickListener(this);
        btnClearNumber.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnSendOtp.setOnClickListener(this);
    }

    private final Authentication.MobileAuthenticationCallback authCallback = new Authentication.MobileAuthenticationCallback() {
        @Override
        public void onGetOtp() {
            mDialog.dismiss();
            etOtp.setText("");
            Utils.showViews(etOtp, btnClearOtp, btnVerifyOtp, btnReset);
            Utils.hideViews(btnClearNumber);
            Utils.toastMessage(AccountActivity.this, "You got otp, check your phone!");
        }

        @Override
        public void onSuccessLogin() {
            mDialog.dismiss();
            Toast.makeText(AccountActivity.this, "login successful", Toast.LENGTH_SHORT).show();
            Utils.openActivity(AccountActivity.this, WelcomeActivity.class, true);
        }

        @Override
        public void onTimeOut() {
            mDialog.dismiss();
        }

        @Override
        public void onFailed(String errorMessage) {
            mDialog.dismiss();
            Log.d(TAG, "onFailed: error : "+errorMessage);
            Utils.toastMessage(AccountActivity.this, "onFailed : "+errorMessage);
            Utils.showViews(btnClearNumber, btnSendOtp);
            Utils.enableViews(etNumber);
        }

        @Override
        public void onOtpWrong(String errorMessage) {
            mDialog.dismiss();
        }
    };

    private String getMobileNumber() {
        if (etNumber != null) {
            String tempNumber = etNumber.getText().toString();
            tempNumber = tempNumber.replace("+91", "");
            /*try {
                final int i = Integer.parseInt(tempNumber);//checking that is string have only number
            } catch (Exception e) {
                Toast.makeText(this, "invalid mobile number, try again", Toast.LENGTH_SHORT).show();
                return null;
            }*/
            return "+91" + tempNumber;
        }
        return null;
    }

    private String getOtp() {
        if (etOtp != null) {
            String otp = etOtp.getText().toString();
            /*try {
                final int i = Integer.parseInt(otp);
            } catch (Exception e) {
                Utils.toastMessage(this, "invalid Otp");
                return null;
            }*/
            return otp;
        }
        return null;
    }

}