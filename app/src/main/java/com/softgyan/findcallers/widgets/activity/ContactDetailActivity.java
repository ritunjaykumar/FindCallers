package com.softgyan.findcallers.widgets.activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.contacts.ContactContracts;
import com.softgyan.findcallers.database.query.ContactsQuery;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.NumberListAdapter;
import com.softgyan.findcallers.widgets.dialog.SingleValueDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactDetailActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String CONTACT_MODEL_KEY = "ContactModelKey";
    private ShapeableImageView sivProfile;
    private TextView tvName, tvEmail, tvAddress, tvProfile;
    private RecyclerView rvMobileNumbers;
    private ContactModel contactModel;
    private static final String TAG = "ContactDetailActivity";
    private boolean isUpdatable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Contact Details");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initViewComponent();

        if (getIntent() != null) {
            contactModel = (ContactModel) getIntent().getSerializableExtra(CONTACT_MODEL_KEY);
            if (contactModel == null) {
                Toast.makeText(this, "unable to load data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "unable to load data", Toast.LENGTH_SHORT).show();
            finish();
        }

        setValueToViews();
    }


    private void initViewComponent() {
        tvProfile = findViewById(R.id.tvProfile);
        sivProfile = findViewById(R.id.sivProfile);
        tvName = findViewById(R.id.tvName);
        rvMobileNumbers = findViewById(R.id.rvContactNumber);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.tvAddress);
        FloatingActionButton fabCall = findViewById(R.id.fabCall);
        FloatingActionButton fabMessage = findViewById(R.id.fabMessage);

        tvProfile.setOnClickListener(this);
        tvName.setOnClickListener(this);
        tvEmail.setOnClickListener(this);
        tvAddress.setOnClickListener(this);
        fabCall.setOnClickListener(this);
        fabMessage.setOnClickListener(this);

    }

    private void setRecyclerView(ArrayList<String> numberList) {
        NumberListAdapter numberAdapter = new NumberListAdapter(numberList, this, true);
        rvMobileNumbers.setAdapter(numberAdapter);
        numberAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);

        final MenuItem item = menu.findItem(R.id.menu_delete);
        item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_delete));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.menu_delete) {
            deleteContact();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        SingleValueDialog singleValueDialog;
        if (id == R.id.fabCall) {
            String number = contactModel.getContactNumbers().get(0).getMobileNumber();
            CallHardware.makeCall(this, number);
        } else if (id == R.id.fabMessage) {
            // TODO: 02-06-2021 send message
        } else if (id == R.id.tvName) {
            singleValueDialog = new SingleValueDialog(this, 1, callBack);
            singleValueDialog.setHint("User name");
            singleValueDialog.setTitle("Enter User Name");
            String text = tvName.getText().toString();
            if (text.length() != 0) {
                singleValueDialog.setTextValue(text);
            }
            singleValueDialog.show();
        } else if (id == R.id.tvEmail) {
            singleValueDialog = new SingleValueDialog(this, 2, callBack);
            singleValueDialog.setHint("Email id");
            singleValueDialog.setTitle("Enter Email id");
            String text = tvEmail.getText().toString();
            if (text.length() != 0) {
                singleValueDialog.setTextValue(text);
            }
            singleValueDialog.show();
        } else if (id == R.id.tvAddress) {
            singleValueDialog = new SingleValueDialog(this, 3, callBack);
            singleValueDialog.setHint("Address");
            singleValueDialog.setTitle("Enter Address");
            String text = tvAddress.getText().toString();
            if (text.length() != 0) {
                singleValueDialog.setTextValue(text);
            }
            singleValueDialog.show();
        }
    }

    private void setValueToViews() {
        if (contactModel == null) {
            Log.d(TAG, "setValueToViews: userInfoModel is null");
            return;
        }
        if (contactModel.getImage() != null) {
            Glide.with(this).load(contactModel.getImage()).placeholder(R.mipmap.test_image).into(sivProfile);
            Utils.showViews(sivProfile);
            Utils.hideViews(tvProfile);
        } else {
            if (contactModel.getName() != null) {
                tvProfile.setText(contactModel.getName().substring(0, 1));
            } else {
                tvProfile.setText("C");
            }
            Utils.hideViews(sivProfile);
            Utils.showViews(tvProfile);
        }


        if (contactModel.getName() != null) {
            tvName.setText(contactModel.getName());
        } else {
            tvName.setText("Contact_" + contactModel.getContactNumbers().get(0).getMobileNumber());
        }


        if (contactModel.getAddress() != null && !contactModel.getAddress().equals("")) {
            tvAddress.setText(contactModel.getAddress());
        } else {
            tvAddress.setHint("Address is not set ");
        }
        if (contactModel.getEmailId() != null && !contactModel.getEmailId().equals("")) {
            tvEmail.setText(contactModel.getEmailId());
        } else {
            tvEmail.setHint("Email is not set ");
        }

        ArrayList<String> numbers = new ArrayList<>();
        final List<ContactNumberModel> contactNumbers = contactModel.getContactNumbers();
        for (ContactNumberModel cNumber : contactNumbers) {
            numbers.add(cNumber.getMobileNumber());
        }

        setRecyclerView(numbers);
    }


    private final SingleValueDialog.SingleValueDialogCallback callBack =
            new SingleValueDialog.SingleValueDialogCallback() {
                @Override
                public void onGetValue(@NonNull SingleValueDialog dialog, int requestCode, @NonNull String value) {
                    if (requestCode == 1) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ContactContracts.ContactsDetails.COLUMN_USER_NAME, value);
                        final boolean b = updateContentValues(contentValues);
                        if (b) {
                            tvName.setText(value);
                            contactModel.setName(value);
                        }

                    } else if (requestCode == 2) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ContactContracts.ContactsDetails.COLUMN_USER_EMAIL_ID, value);
                        final boolean b = updateContentValues(contentValues);
                        if (b) {
                            tvEmail.setText(value);
                            contactModel.setEmailId(value);
                        }
                    } else if (requestCode == 3) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ContactContracts.ContactsDetails.COLUMN_USER_ADDRESS, value);
                        final boolean b = updateContentValues(contentValues);
                        if (b) {
                            tvAddress.setText(value);
                            contactModel.setAddress(value);
                        }
                    }
                    isUpdatable = true;
                    dialog.dismiss();
                }
            };


    private boolean updateContentValues(final ContentValues contentValues) {
        final int i = ContactsQuery.updateContactValue(this, contentValues, contactModel.getId());
        if (i > 0) {
            Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "some thing is wrong", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (isUpdatable)
            Utils.setContactToList(contactModel);
        super.onBackPressed();
    }

    private void deleteContact() {
        final int i = ContactsQuery.deleteSingleContact(this, contactModel.getId());
        if (i > 0) {
            final List<ContactModel> contactsList = CommVar.contactsList;
            contactsList.removeIf(cModel -> cModel.getId() == contactModel.getId());
            Log.d(TAG, "deleteContact: contact deleted");
            finish();
        }
    }
}