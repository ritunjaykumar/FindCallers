package com.softgyan.findcallers.widgets.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private TextView tvName, tvAddress, tvEmail, tvMobile, tvSpamType, tvSpamVote;
    private Button btnSaveToContact;
    private Button btnAddToSpam;
    private Button btnAddToBlock;
    private CardView cardView, spamCardView;
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
            Utils.hideViews(cardView,spamCardView, btnAddToSpam, btnAddToBlock, btnSaveToContact);
        } else if (id == R.id.btnSearch) {
            Utils.hideViews(cardView,spamCardView, btnAddToSpam, btnAddToBlock, btnSaveToContact);
            searchOperation();
        } else if (id == R.id.btnAddToSpam) {
            insertSpamNumber();
        } else if (id == R.id.btnAddToBlock) {
            String mobileNumber = getMobileNumber();
            if (mobileNumber == null) {
                Toast.makeText(this, "invalid mobile number.", Toast.LENGTH_SHORT).show();
                return;
            }
            insertSpamOrBlock(mobileNumber, "block", true);
        } else if (id == R.id.btnSaveToContact) {

        }
    }

    private void searchOperation() {
        HashMap<String, Object> searchedNumber = new HashMap<>();
        String tempNumber = getMobileNumber();
        Log.d(TAG, "searchOperation: tempNumber : " + tempNumber);
        if (tempNumber == null) return;
        //checking block list or spam -> start
        pDialog.setTitle("searching number...");
        pDialog.show();
        final List<BlockNumberModel> blockListArray = SpamQuery.getBlockListArray(this, tempNumber);
        if (blockListArray != null && blockListArray.size() >= 1) {
            final BlockNumberModel blockNumberModel = blockListArray.get(0);
            if (blockNumberModel.getType() == SpamContract.BLOCK_TYPE) {
                searchedNumber.put("blockKey", true);
            } else {
                searchedNumber.put("spamKey", true);
            }
        }
        //checking block list or spam -> end
        final ContactModel contactModel = Utils.advanceSearch(this, tempNumber);
        Log.d(TAG, "searchOperation: contact model : " + contactModel);

        if (contactModel == null) {
            if (!Utils.isInternetConnectionAvailable(this)) {
                Toast.makeText(this, "check internet connectivity..", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
                return;
            }
            FirebaseDB.MobileNumberInfo.getMobileNumber(tempNumber, new OnResultCallback<ContactModel>() {
                @Override
                public void onSuccess(@NonNull ContactModel contactModel) {
                    name = contactModel.getName();
                    searchedNumber.put("contactModel", contactModel);
                    pDialog.dismiss();
                    setValueToView(searchedNumber);
                }

                @Override
                public void onFailed(String failedMessage) {
                    Toast.makeText(SearchNumberActivity.this, failedMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailed: failedMessage : " + failedMessage);
                    pDialog.dismiss();
                }

            });

        } else {
            searchedNumber.put("isNumberSave", true);
            name = contactModel.getName();
            searchedNumber.put("contactModel", contactModel);
            setValueToView(searchedNumber);
            //            setValueToView(contactModel);
            pDialog.dismiss();
        }

        FirebaseDB.SpamDB.getSpamNumber(tempNumber, new OnResultCallback<HashMap<String, Object>>() {
            @Override
            public void onSuccess(@NonNull HashMap<String, Object> spamMap) {
                searchedNumber.put("spamMap", spamMap);
                Log.d(TAG, "onSuccess: spam number : " + spamMap);
                setValueToView(searchedNumber);
            }

            @Override
            public void onFailed(String failedMessage) {
                //do nothing...
                searchedNumber.remove("spamMap");
                setValueToView(searchedNumber);
                Log.d(TAG, "onFailed: spam failedMessage : " + failedMessage);
            }
        });
    }

    private void insertSpamNumber() {
        final String mobileNumber = getMobileNumber();
        if (mobileNumber == null) {
            Toast.makeText(this, "invalid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> spamMap = new HashMap<>();
        spamMap.put(FirebaseVar.SpamDB.MOBILE_NUMBER, mobileNumber);
        spamMap.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY, "fraud");
        spamMap.put(FirebaseVar.SpamDB.NAME, name);
        spamMap.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, 1);
        spamMap.put(FirebaseVar.SpamDB.TOTAL_NAME, 1);

        FirebaseDB.SpamDB.uploadSpamNumber(spamMap, null);

        insertSpamOrBlock(mobileNumber, "spam", false);
    }

    private void insertSpamOrBlock(String mobileNumber, String message, boolean isBlock) {

        BlockNumberModel blockModel = new BlockNumberModel();
        blockModel.setNumber(mobileNumber);
        blockModel.setName(name);
        if (isBlock) {
            blockModel.setType(SpamContract.BLOCK_TYPE);
        } else {
            blockModel.setType(SpamContract.SPAM_TYPE);
        }
        final int i = SpamQuery.insertBlockList(this, blockModel);
        if (i != 1) {
            Toast.makeText(this, "inserted into " + message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "not inserted into " + message, Toast.LENGTH_SHORT).show();
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
        Button btnSearch = findViewById(R.id.btnSearch);
        imageView = findViewById(R.id.sivProfile);
        tvSpamType = findViewById(R.id.tvSpamType);
        tvSpamVote = findViewById(R.id.tvSpamVote);
        ImageButton ibClear = findViewById(R.id.ibClear);
        cardView = findViewById(R.id.cardView);
        spamCardView = findViewById(R.id.spamCardView);
        btnSearch.setOnClickListener(this);
        btnAddToBlock.setOnClickListener(this);
        btnAddToSpam.setOnClickListener(this);
        btnSaveToContact.setOnClickListener(this);
        ibClear.setOnClickListener(this);

        pDialog = new ProgressDialog(this);


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                final String number = invalidateNumber(sharedText);
                if (number != null) {
                    etSearch.setText(number);
                    Utils.hideViews(cardView);
                    searchOperation();
                } else {
                    Toast.makeText(this, "invalid mobile number", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private String getMobileNumber() {
        String tempNumber = etSearch.getText().toString();
        if (tempNumber.length() > 9 && tempNumber.length() < 14) {
            return Utils.trimNumber(tempNumber);
        } else {
            Toast.makeText(this, "invalid mobile number", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setValueToView(final HashMap<String, Object> searchedNumber) {
        try {
            ContactModel contactModel = (ContactModel) searchedNumber.get("contactModel");
            if (contactModel != null) {
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

            } else {
                Utils.hideViews(cardView);
            }

            Boolean isSpam = (Boolean) searchedNumber.get("spamKey");
            Boolean isBlock = (Boolean) searchedNumber.get("blockKey");
            if ((isSpam != null && isSpam) || (isBlock != null && isBlock)) {
                Utils.hideViews(btnAddToSpam, btnAddToBlock);
            } else {
                Utils.showViews(btnAddToSpam, btnAddToBlock);
            }

            Boolean isNumberSave = (Boolean) searchedNumber.get("isNumberSave");
            if (isNumberSave != null && isNumberSave) {
                Utils.hideViews(btnSaveToContact);
            } else {
                Utils.showViews(btnSaveToContact);
            }

            try {
                HashMap<String, Object> spamData = (HashMap<String, Object>) searchedNumber.get("spamMap");
                Log.d(TAG, "setValueToView: spamData : " + spamData);
                if (spamData != null) {
                    Utils.showViews(spamCardView);
                    Long spamVote = (Long) spamData.get(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE);
                    String spamType = (String) spamData.get(FirebaseVar.SpamDB.SPAM_TYPE_KEY);
                    if (spamVote != null) {
                        tvSpamVote.setText(String.valueOf(spamVote));
                    } else {
                        Log.d(TAG, "setValueToView: tvSpamProblem " + spamVote);
                    }
                    if (spamType != null && spamType.length() > 0) {
                        tvSpamType.setText(spamType);
                    } else {
                        Log.d(TAG, "setValueToView: tvSpamTypeProblem " + spamType);
                    }
                } else {
                    Utils.hideViews(spamCardView);
                }
            } catch (Exception e) {
                Utils.hideViews(spamCardView);
                Log.d(TAG, "setValueToView: error message : " + e.getMessage());
            }


        } catch (Exception e) {
            Log.d(TAG, "setValueToView: error : " + e.getMessage());
            Utils.hideViews(btnAddToBlock, btnAddToSpam, btnSaveToContact, cardView, spamCardView);
        }
    }

    private String invalidateNumber(String number) {
        Log.d(TAG, "invalidateNumber: number_1 : " + number);
        if (number == null || number.length() < 10 || number.length() > 15) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) >= '0' && number.charAt(i) <= '9') {
                sb.append(number.charAt(i));
            }
        }
        Log.d(TAG, "invalidateNumber: number_2 : " + sb.toString());
        if (sb.length() >= 10 && sb.length() <= 15) {
            return sb.toString();
        }
        return null;

    }
}