package com.softgyan.findcallers.widgets.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.callback.OnResultCallback;
import com.softgyan.findcallers.callback.OnUploadCallback;
import com.softgyan.findcallers.database.query.SpamQuery;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.models.BlockNumberModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SearchNumberActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SearchNumberActivity";
    public static final String NOT_AVAILABLE = "not available";
    private ShapeableImageView imageView;
    private EditText etSearch;
    private TextView tvName, tvAddress, tvEmail, tvMobile;
    private Button btnSaveToContact, btnAddToSpam, btnAddToBlock, btnSearch;
    private ImageButton ibClear;
    private CardView cardView;
    private boolean isSpam = false;
    private boolean isBlock = false;
    private boolean isNumberSave = false;
    private String name;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_number);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Search Number");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponent();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibClear) {
            etSearch.setText(null);
        } else if (id == R.id.btnSearch) {
            searchOperation();
        } else if (id == R.id.btnAddToSpam) {
            insertSpamNumber();
        } else if (id == R.id.btnAddToBlock) {
            String mobileNumber = getMobileNumber();
            if (mobileNumber == null) {
                Toast.makeText(this, "invalid mobile number.", Toast.LENGTH_SHORT).show();
                return;
            }
            insertSpamOrBlock(mobileNumber, "block");
        }
    }

    private void searchOperation() {

        String tempNumber = getMobileNumber();
        if (tempNumber == null) return;
        //checking block list or spam -> start
        pDialog.setTitle("searching number...");
        pDialog.show();
        final List<BlockNumberModel> blockListArray = SpamQuery.getBlockListArray(this, tempNumber);
        if (blockListArray != null && blockListArray.size() >= 1) {
            final BlockNumberModel blockNumberModel = blockListArray.get(0);
            if (blockNumberModel.getType() == SpamContract.BLOCK_TYPE) {
                isBlock = true;
            } else {
                isSpam = true;
            }
        }
        //checking block list or spam -> end

        final ContactModel contactModel = Utils.advanceSearch(this, tempNumber);

        if (contactModel == null) {
            if (!Utils.isInternetConnectionAvailable(this)) {
                Toast.makeText(this, "internet connection is not available", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
                return;
            }
            FirebaseDB.MobileNumberInfo.getMobileNumber(tempNumber, new OnResultCallback<ContactModel>() {
                @Override
                public void onSuccess(@NonNull ContactModel contactModel) {
                    name = contactModel.getName();
                    setValueToView(contactModel);
                    pDialog.dismiss();
                }

                @Override
                public void onFailed(String failedMessage) {
                    Toast.makeText(SearchNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                }

            });
        } else {
            isNumberSave = true;
            name = contactModel.getName();
            setValueToView(contactModel);
            pDialog.dismiss();
        }
    }

    private void insertSpamNumber() {
        final String mobileNumber = getMobileNumber();
        if (mobileNumber == null) {
            Toast.makeText(this, "invalid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> spamMap = new HashMap<>();
        spamMap.put(FirebaseVar.SpamDB.MOBILE_NUMBER, mobileNumber);
        spamMap.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY + 1, "fraud");
        spamMap.put(FirebaseVar.SpamDB.NAME + 1, name);
        spamMap.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, 1);
        spamMap.put(FirebaseVar.SpamDB.TOTAL_NAME, 1);

        FirebaseDB.SpamDB.uploadSpamNumber(spamMap, new OnUploadCallback() {
            @Override
            public void onUploadSuccess(String message) {

            }

            @Override
            public void onUploadFailed(String failedMessage) {

            }
        });

        insertSpamOrBlock(mobileNumber, "spam");
    }

    private void insertSpamOrBlock(String mobileNumber, String message) {
        if (!isSpam && !isBlock) {
            BlockNumberModel blockModel = new BlockNumberModel();
            blockModel.setNumber(mobileNumber);
            blockModel.setName(name);
            blockModel.setType(SpamContract.SPAM_TYPE);
            final int i = SpamQuery.insertBlockList(this, blockModel);
            if (i == 1) {
                Toast.makeText(this, "inserted into " + message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "not inserted into " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "already exits.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initComponent() {
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvEmail = findViewById(R.id.tvEmailId);
        tvMobile = findViewById(R.id.tvMobile);
        etSearch = findViewById(R.id.etSearchView);
        btnAddToBlock = findViewById(R.id.btnAddToBlock);
        btnAddToSpam = findViewById(R.id.btnAddToSpam);
        btnSaveToContact = findViewById(R.id.btnSaveToContact);
        btnSearch = findViewById(R.id.btnSearch);
        imageView = findViewById(R.id.sivProfile);
        ibClear = findViewById(R.id.ibClear);
        cardView = findViewById(R.id.cardView);

        btnSearch.setOnClickListener(this);
        btnAddToBlock.setOnClickListener(this);
        btnAddToSpam.setOnClickListener(this);
        btnSaveToContact.setOnClickListener(this);
        ibClear.setOnClickListener(this);

        pDialog = new ProgressDialog(this);
    }

    private String getMobileNumber() {
        String tempNumber = etSearch.getText().toString();
        if ((tempNumber != null) && (tempNumber.length() > 9 && tempNumber.length() < 14)) {
            return Utils.trimNumber(tempNumber);
        } else {
            Toast.makeText(this, "invalid mobile number", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setValueToView(final ContactModel contactModel) {
        Utils.showViews(cardView);

        if (contactModel.getName() != null) {
            tvName.setText(contactModel.getName());
        } else {
            tvName.setText(NOT_AVAILABLE);
        }
        if (contactModel.getEmailId() != null) {
            tvEmail.setText(contactModel.getEmailId());
        } else {
            tvEmail.setText(NOT_AVAILABLE);
        }
        tvMobile.setText(contactModel.getContactNumbers().get(0).getMobileNumber());
        if (contactModel.getImage() != null) {
            Glide.with(this).load(contactModel.getImage()).into(imageView);
        }
        if (contactModel.getAddress() != null) {
            tvAddress.setText((contactModel.getAddress()));
        } else {
            tvAddress.setText(NOT_AVAILABLE);
        }

        if (isSpam || isBlock) {
            Utils.hideViews(btnAddToBlock);
        } else {
            Utils.showViews(btnAddToBlock);
        }
        if (isNumberSave) {
            Utils.hideViews(btnSaveToContact);
        } else {
            Utils.showViews(btnSaveToContact);
        }
    }
}