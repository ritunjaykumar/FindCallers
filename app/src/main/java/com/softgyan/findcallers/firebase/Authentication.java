package com.softgyan.findcallers.firebase;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Authentication {
    private static final String TAG = "Authentication";
    private final Context mContext;
    private final FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mobileNumber;

    private MobileAuthenticationCallback mobAuthCallback;

    public Authentication(Context mContext) {
        this.mContext = mContext;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void onPhoneVerify(final String mobileNumber, MobileAuthenticationCallback authCallback) {
        Log.d(TAG, "onPhoneVerify: verification start");
        mobAuthCallback = authCallback;
        this.mobileNumber = mobileNumber;
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //self mobile verification
                signInWithPhoneAuthCredential(phoneAuthCredential);
                Log.d(TAG, "onVerificationCompleted: otp is verified automatically");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                mobAuthCallback.onFailed(e.getMessage());
                Log.d(TAG, "onVerificationFailed: failed");
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                mResendToken = forceResendingToken;
                mobAuthCallback.onGetOtp();
                Log.d(TAG, "onCodeSent: code sent to " + mobileNumber);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                mobAuthCallback.onTimeOut();
                Log.d(TAG, "onCodeAutoRetrievalTimeOut: time out");
            }
        };
        startPhoneNumberVerification(mobileNumber);
    }

    public void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
        Log.d(TAG, "verifyOtp: call verifyOtp method");
    }

    public void resendOtp() {
        resendVerificationCode(mobileNumber, mResendToken);
        Log.d(TAG, "resendOtp: call resend Otp method");
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: called");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mobAuthCallback.onSuccessLogin();
                        Log.d(TAG, "signInWithPhoneAuthCredential: verification successfully");
                    } else {
                        mobAuthCallback.onOtpWrong(Objects.requireNonNull(task.getException()).getMessage());
                        Log.d(TAG, "signInWithPhoneAuthCredential: " + task.getException().getMessage());
                    }
                });


    }

    private void startPhoneNumberVerification(String phoneNumber) {
        Log.d(TAG, "startPhoneNumberVerification: " + phoneNumber);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity((Activity) mContext)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity((Activity) mContext)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Log.d(TAG, "resendVerificationCode: " + phoneNumber);
    }

    public interface MobileAuthenticationCallback {
        void onGetOtp();

        void onSuccessLogin();

        void onTimeOut();

        void onFailed(String errorMessage);

        void onOtpWrong(String errorMessage);
    }

}
