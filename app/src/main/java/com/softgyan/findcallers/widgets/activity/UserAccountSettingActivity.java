package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.softgyan.findcallers.BuildConfig;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.firebase.FirebaseBasic;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.models.UploadContactModel;
import com.softgyan.findcallers.models.UserInfoModel;
import com.softgyan.findcallers.preferences.AppPreference;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ImageSelectorDialog;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;
import com.softgyan.findcallers.widgets.dialog.SingleValueDialog;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class UserAccountSettingActivity extends AppCompatActivity implements View.OnClickListener,
        ImageSelectorDialog.ImageSelectorCallback {

    public static final String IS_SAVE_DATA = "isSaveData";
    private static final int SELECT_IMAGE_REQUEST_CODE = 102;
    private static final int OPEN_CAMERA_REQUEST_CODE = 103;
    private static final String TAG = "UserAccountSettingActivity";
    private TextView tvName, tvAddress, tvEmail, tvNumber;
    private ShapeableImageView sivProfile;
    private Uri outputUri;
    private boolean isSaveData = false;
    private ProgressDialog pDialog;
    private UserInfoModel mUserInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account_setting);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Info");
        if (getIntent() != null) {
            isSaveData = getIntent().getBooleanExtra(IS_SAVE_DATA, false);
        }
        Log.d(TAG, "onCreate: is saved data : " + isSaveData);
        initViewComponent();
        getUserInfo();
    }

    private void initViewComponent() {
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvEmail = findViewById(R.id.tvEmail);
        tvNumber = findViewById(R.id.tvNumber);
        sivProfile = findViewById(R.id.sivProfile);
        Button btnDeleteAccount = findViewById(R.id.btnAccountDelete);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnSave = findViewById(R.id.btnSave);
        if (isSaveData) {
            Utils.showViews(btnSave);
            Utils.hideViews(btnDeleteAccount, btnLogout);
        } else {
            Utils.hideViews(btnSave);
            Utils.showViews(btnDeleteAccount, btnLogout);
        }
        ImageButton ibAddImage = findViewById(R.id.ibAddImage);
        ibAddImage.setOnClickListener(this);
        tvName.setOnClickListener(this);
        tvAddress.setOnClickListener(this);
        tvEmail.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnDeleteAccount.setOnClickListener(this);

        pDialog = new ProgressDialog(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        SingleValueDialog valueDialog;
        if (id == R.id.btnAccountDelete) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra(AccountActivity.DELETE_KEY, true);
            ActivityCompat.startActivityForResult(this, intent, 500, null);
        } else if (id == R.id.btnLogout) {
            logout();
        } else if (id == R.id.btnSave) {
            uploadUserInfo();
        } else if (id == R.id.ibAddImage) {
            View sheetView = findViewById(R.id.linearLayout);
            ImageSelectorDialog imageSelectorDialog = new ImageSelectorDialog(sheetView, this);
            imageSelectorDialog.getBottomSheetBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (id == R.id.tvName) {
            valueDialog = new SingleValueDialog(UserAccountSettingActivity.this, 1, valueDialogCallback);
            valueDialog.setTitle("User Name");
            valueDialog.setHint("User name");
            String tempName = tvName.getText().toString();
            if (tempName.length() != 0) {
                valueDialog.setTextValue(tempName);
            }
            valueDialog.show();
        } else if (id == R.id.tvEmail) {
            valueDialog = new SingleValueDialog(UserAccountSettingActivity.this, 2, valueDialogCallback);
            valueDialog.setTitle("Email id");
            valueDialog.setHint("email id");
            String tempEmail = tvEmail.getText().toString();
            if (tempEmail.length() != 0) {
                valueDialog.setTextValue(tempEmail);
            }
            valueDialog.show();
        } else if (id == R.id.tvAddress) {
            valueDialog = new SingleValueDialog(UserAccountSettingActivity.this,
                    SingleValueDialog.DialogOption.MULTIPLE_LINE, 3, valueDialogCallback);
            valueDialog.setTitle("Address");
            valueDialog.setHint("address");
            String tempAddress = tvAddress.getText().toString();
            if (tempAddress.length() != 0) {
                valueDialog.setTextValue(tempAddress);
            }
            valueDialog.show();
        }
    }


    @Override
    public void onImageSelectResult(final BottomSheetBehavior<View> sheetBehavior, final int operationCode) {
        switch (operationCode) {
            case ImageSelectorDialog.ImageSelectorCallback.CAMERA_CODE: {
                camera();
                break;
            }
            case (ImageSelectorDialog.ImageSelectorCallback.GALLERY_CODE): {
                openGallery();
                break;
            }
            case (ImageSelectorDialog.ImageSelectorCallback.REMOVE_CODE): {
                removeImageFromFirebase(true);
                break;
            }
        }
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void openGallery() {

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final boolean isPermitted = Utils.hasPermissions(this, permissions);
        if (!isPermitted) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
        if (Utils.checkPermission(this, permissions)) {
            gallery();
        }
    }

    private void gallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ActivityCompat.startActivityForResult(this, Intent.createChooser(intent, "select image"),
                SELECT_IMAGE_REQUEST_CODE, null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            final Uri imageUri = data.getData();
            uploadImage(imageUri);
        } else if (requestCode == OPEN_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (outputUri != null)
                uploadImage(outputUri);
        } else if (requestCode == 500) {
            if (resultCode == Activity.RESULT_OK) {
                deleteAccount();
            }
        }
    }

    private void camera() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        final boolean isPermitted = Utils.hasPermissions(this, permissions);
        if (!isPermitted) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        if (Utils.checkPermission(this, permissions)) {
            openCamera();
        }

    }

    private void openCamera() {
        Log.d(TAG, "openCamera: ");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String uniqueId = UUID.randomUUID().toString();
        String fileName = uniqueId + ".jpg";
        try {
            File file = File.createTempFile(uniqueId, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            outputUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra("listPhotoName", fileName);
            startActivityForResult(intent, OPEN_CAMERA_REQUEST_CODE);
        } catch (IOException e) {
            Log.d(TAG, "openCamera: " + e.getMessage());
        }

    }


    private void setDataToView() {
        if (mUserInfo.getUserName() != null) {
            tvName.setText(mUserInfo.getUserName());
        }
        if (mUserInfo.getUserEmail() != null) {
            tvEmail.setText(mUserInfo.getUserEmail());
        }
        if (mUserInfo.getUserAddress() != null) {
            tvAddress.setText(mUserInfo.getUserAddress());
        }
        tvNumber.setText(mUserInfo.getAccountMobileNumber());

        Glide.with(this).load(mUserInfo.getUserProfile())
                .placeholder(R.drawable.ic_image).into(sivProfile);

    }

    private void getUserInfo() {
        pDialog.setProgressTitle("Getting data from server...");
        pDialog.show();
        FirebaseDB.UserInfo.getUserInfo(new OnResultCallback<UserInfoModel>() {
            @Override
            public void onSuccess(@NonNull UserInfoModel userInfoModel) {
                mUserInfo = userInfoModel;
                setDataToView();
                pDialog.dismiss();
            }

            @Override
            public void onFailed(String failedMessage) {
                Toast.makeText(UserAccountSettingActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                mUserInfo = UserInfoModel.getInstance(null, null, null,
                        null, false, null, false);
                pDialog.dismiss();
            }

        });
    }

    private final SingleValueDialog.SingleValueDialogCallback valueDialogCallback
            = new SingleValueDialog.SingleValueDialogCallback() {
        @Override
        public void onGetValue(@NonNull SingleValueDialog dialog, int requestCode, @NonNull String value) {
            switch (requestCode) {
                case 1: {
                    if (isSaveData) {
                        mUserInfo.setUserName(value);
                        tvName.setText(value);
                    } else {
                        updateData(FirebaseVar.User.USER_NAME, value, requestCode);
                    }
                    break;
                }
                case 2: {
                    if (isSaveData) {
                        mUserInfo.setUserEmail(value);
                        mUserInfo.setEmailVerify(false);
                        tvEmail.setText(value);
                    } else {
                        FirebaseDB.UserInfo.updateUser(new String[]{FirebaseVar.User.USER_EMAIL, FirebaseVar.User.EMAIL_VERIFY},
                                new Object[]{value, false}, new OnUploadCallback() {
                                    @Override
                                    public void onUploadSuccess(String message) {
                                        mUserInfo.setUserEmail(value);
                                        mUserInfo.setEmailVerify(false);
                                        tvEmail.setText(value);
                                    }

                                    @Override
                                    public void onUploadFailed(String failedMessage) {
                                        Toast.makeText(UserAccountSettingActivity.this, failedMessage,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    break;
                }
                case 3: {
                    if (isSaveData) {
                        mUserInfo.setUserAddress(value);
                        tvAddress.setText(value);
                    } else {
                        updateData(FirebaseVar.User.USER_ADDRESS, value, requestCode);
                    }
                }


            }
            dialog.dismiss();
        }
    };

    private void updateData(String key, String value, int requestCode) {
        FirebaseDB.UserInfo.updateUser(new String[]{key}, new Object[]{value}, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                switch (requestCode) {
                    case 1: {
                        tvName.setText(value);
                        mUserInfo.setUserName(value);
                        break;
                    }
                    case 3: {
                        tvAddress.setText(value);
                        mUserInfo.setUserAddress(value);
                        break;
                    }
                    case 4: {
                        Glide.with(UserAccountSettingActivity.this).load(value)
                                .placeholder(R.drawable.ic_image).into(sivProfile);
                        mUserInfo.setUserProfile(value);
                    }
                }
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                switch (requestCode) {
                    case 1: {
                        Toast.makeText(UserAccountSettingActivity.this,
                                "some thing wrong during update name", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 3: {
                        Toast.makeText(UserAccountSettingActivity.this,
                                "some thing wrong during update name", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void uploadImage(final Uri imageUri) {
        pDialog.show();
        pDialog.setProgressTitle("");
        removeImageFromFirebase(false);
        FirebaseBasic.uploadImage(imageUri, this, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String profileUrl) {
                Log.d(TAG, "onUploadSuccess: profile url :" + profileUrl);

                mUserInfo.setUserProfile(profileUrl);
                if (isSaveData) {
                    Glide.with(UserAccountSettingActivity.this).load(profileUrl)
                            .placeholder(R.drawable.ic_image).into(sivProfile);
                } else {
                    updateData(FirebaseVar.User.USER_PROFILE, profileUrl, 4);
                }
                pDialog.dismiss();
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                pDialog.dismiss();
                Toast.makeText(UserAccountSettingActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeImageFromFirebase(boolean isShowMessage) {

        if (mUserInfo != null && mUserInfo.getUserProfile() != null && !mUserInfo.getUserProfile().isEmpty()) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressTitle("deleting image");
            progressDialog.show();
            FirebaseBasic.deleteImage(mUserInfo.getUserProfile(), new OnUploadCallback() {
                @Override
                public void onUploadSuccess(String message) {
                    if (isShowMessage) {
                        sivProfile.setImageResource(R.mipmap.test_image);
                        Toast.makeText(UserAccountSettingActivity.this, "profile image deleted", Toast.LENGTH_SHORT).show();
                        FirebaseDB.UserInfo.updateUser(new String[]{FirebaseVar.User.USER_PROFILE},
                                new String[]{null}, null);
                        mUserInfo.setUserProfile(null);
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onUploadFailed(String failedMessage) {
                    progressDialog.dismiss();
                    if (isShowMessage) {
                        Toast.makeText(UserAccountSettingActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void uploadUserInfo() {
        pDialog.setProgressTitle("saving User Info");
        pDialog.show();
        uploadContactModel();
        FirebaseDB.UserInfo.uploadUserInfo(mUserInfo, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                pDialog.dismiss();
                AppPreference.setAccountActivitySet(UserAccountSettingActivity.this, true);
                Intent intent = new Intent(UserAccountSettingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                pDialog.dismiss();
                Toast.makeText(UserAccountSettingActivity.this, "some thing wrong, try again!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        deleteAllDatabase();
    }

    private void deleteAccount() {
        pDialog.setTitle("wait for seconds");
        pDialog.show();
        String documentId = FirebaseAuth.getInstance().getUid();
        FirebaseDB.UserInfo.deleteUserRecord(this, documentId, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {
                FirebaseDB.UserInfo.deleteAccount(UserAccountSettingActivity.this, new OnUploadCallback() {
                    @Override
                    public void onUploadSuccess(String message) {
                        pDialog.dismiss();
                        deleteAllDatabase();
                    }

                    @Override
                    public void onUploadFailed(String failedMessage) {
                        pDialog.dismiss();
                        Toast.makeText(UserAccountSettingActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onUploadFailed: error : " + failedMessage);
                    }

                });
            }

            @Override
            public void onUploadFailed(String failedMessage) {
                pDialog.dismiss();
                Toast.makeText(UserAccountSettingActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onUploadFailed: error : " + failedMessage);
            }
        });
    }

    private void deleteAllDatabase() {
       /* deleteDatabase(CallHelper.DATABASE_NAME);
        deleteDatabase(ContactHelper.DATABASE_NAME);
        deleteDatabase(SpamDbHelper.SPAM_DB);*/
        CommVar.callList.clear();
        CommVar.contactsList.clear();
        AppPreference.clearPreference(this);
        Intent intent = new Intent(this, AccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadContactModel() {
        UploadContactModel model = new UploadContactModel();
        model.setAddress(mUserInfo.getUserAddress());
        model.setUserEmail(mUserInfo.getUserEmail());
        model.setMobileNumber(mUserInfo.getAccountMobileNumber());
        model.setUserName(mUserInfo.getUserName());
        model.setUserSetName(true);
        model.setTotalName(0);
        model.setProfileUrl(model.getProfileUrl());
        FirebaseDB.MobileNumberInfo.uploadContacts(model);
    }

}