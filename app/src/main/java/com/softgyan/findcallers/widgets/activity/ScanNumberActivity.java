package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.softgyan.findcallers.BuildConfig;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.NumberListAdapter;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ScanNumberActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 102;
    private static final int OPEN_CAMERA_REQUEST_CODE = 103;
    private static final int SELECT_IMAGE_REQUEST_CODE = 104;

    private Uri imageUri = null;
    private Uri tempImageUri = null;

    private RecyclerView r;
    private TextView tvShowText;
    private ImageView ivShowImage;

    private static final String TAG = "ScanNumberActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_number);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle("Scan number");
    }

    private void initComponent() {
        tvShowText = findViewById(R.id.tv_scan_text);
        ivShowImage = findViewById(R.id.imageView);
        Button btnCapture = findViewById(R.id.btn_capture);
        Button btnScan = findViewById(R.id.btn_scan_number);
        Button gallery = findViewById(R.id.btn_from_gallery);

        r = findViewById(R.id.rvNumberList);


        btnCapture.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        gallery.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_capture) {
            setText("");
            checkCameraPermission();
        } else if (id == R.id.btn_scan_number) {
            detectText();
        } else if (id == R.id.btn_from_gallery) {
            checkGalleryPermission();
        }
    }

    private void detectText() {
        setText("");
        if (imageUri == null) {
            Toast.makeText(this, "before, take image", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.show();
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        final String text = visionText.getText();
                        final ArrayList<String> strings = filterNumber(text);
                        if (strings.size() == 0) {
                            setText("No numbers are detected");
                            Utils.hideViews(r);
                            Utils.showViews( tvShowText);
                        } else {
                            setRecyclerView(strings);
                            Utils.showViews(r);
                            Utils.hideViews(tvShowText);
                        }
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ScanNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        } catch (IOException e) {
            dialog.dismiss();
            e.printStackTrace();
            setText(e.getMessage());
        }


    }


    private void setText(String strText) {
        tvShowText.setText(strText);
    }


    private void checkCameraPermission() {
        try {
            String[] permissionString = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            final boolean isPermitted = Utils.hasPermissions(this, permissionString);
            if (!isPermitted) {
                ActivityCompat.requestPermissions(this, permissionString, CAMERA_REQUEST_CODE);
            }

            if (Utils.checkPermission(this, permissionString)) {
                openCamera();
                Log.d(TAG, "checkCameraPermission: camera open");
            } else {
                Log.d(TAG, "checkCameraPermission: not camera open");
            }

        } catch (Exception e) {
            Log.d(TAG, "checkCameraPermission: " + e.getMessage());
        }
    }

    private void openCamera() {
        Log.d(TAG, "openCamera: ");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String uniqueId = UUID.randomUUID().toString();
        String fileName = uniqueId + ".jpg";
        try {
            tempImageUri = imageUri; // for keep previous image data to delete before reading new image
            File file = File.createTempFile(uniqueId, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("listPhotoName", fileName);
            startActivityForResult(intent, OPEN_CAMERA_REQUEST_CODE);
        } catch (IOException e) {
            Log.d(TAG, "openCamera: " + e.getMessage());
        }

    }

    private void checkGalleryPermission() {

        String[] permissionString = {Manifest.permission.READ_EXTERNAL_STORAGE};
        final boolean hasPermissions = Utils.hasPermissions(this, permissionString);
        if (!hasPermissions) {
            ActivityCompat.requestPermissions(this, permissionString, GALLERY_PERMISSION_REQUEST_CODE);
        }
        if (Utils.checkPermission(this, permissionString)) {
            openGallery();
        }
    }




    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "select image"), SELECT_IMAGE_REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            ivShowImage.setImageURI(imageUri);
            deleteFile(tempImageUri);
        } else if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                ivShowImage.setImageURI(imageUri);
            }
        }
    }


    private void setRecyclerView(ArrayList<String> numberList) {
        NumberListAdapter numberAdapter = new NumberListAdapter(numberList, this);
        r.setAdapter(numberAdapter);
        numberAdapter.notifyDataSetChanged();
    }


    private ArrayList<String> filterNumber(String text) {
        ArrayList<String> scanMobileList = new ArrayList<>();
        String[] strArr = text.split("\\s+");
        for (String strText : strArr) {
            Log.d(TAG, "filterNumber: string : " + strText);
            if (strText.length() < 10 || strText.length() > 15) {
                continue;
            }
            char[] charArr = strText.toCharArray();
            boolean flag = false;
            for (char ch : charArr) {
                if (ch < 48 || ch > 57) {
                    Log.d(TAG, "filterNumber: failed because : " + ch);
                    flag = true; //if any alphabet is present so it is not part of mobile number
                    break;
                }
            }
            if (!flag) {
                scanMobileList.add(strText);
            }
        }
        return scanMobileList;
    }


    private void deleteFile(Uri uri) {
        if (uri != null) {
            final int delete = getContentResolver().delete(uri, null, null);
            if (delete == 1) {
                Log.d(TAG, "deleteFile: file deleted");
                Toast.makeText(this, "file deleted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "deleteFile: file not deleted");
            }
        }
    }

}