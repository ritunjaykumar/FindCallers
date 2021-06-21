package com.softgyan.findcallers.widgets.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BackupAndRestoreActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = BackupAndRestoreActivity.class.getName();
    private Button btnBackup, btnSelectContact, btnRestoreContact;
    private TextView tvContactBackupLocation1, tvContactBackupLocation2;

    private static final Uri CONTACT_BASE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final String DISPLAY_NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    private static final String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String CONTACT_NAME_KEY = "name";
    private static final String CONTACT_NUMBER_KEY = "number";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_and_restore);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Backup And Restore");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checkContactPermission();

        btnRestoreContact = findViewById(R.id.btnRestoreContact);
        tvContactBackupLocation1 = findViewById(R.id.tvContactLocation_1);
        tvContactBackupLocation2 = findViewById(R.id.tvContactLocation_2);
        btnBackup = findViewById(R.id.btnBackup);
        btnRestoreContact.setOnClickListener(this);
        btnBackup.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBackup) {
            if (!isExternalStorageAvailableForRW()) {
                toastMessage("external storage is not available");
                return;
            }
            if (!checkContactPermission()) {
                toastMessage("you don't have permission to back up contact");
                return;
            }
            SaveContact saveContact = new SaveContact();
            saveContact.execute();

        } else if (id == R.id.btnRestoreContact) {
            if (!checkContactPermission()) {
                toastMessage("you don't have permission to write contact");
                return;
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            ActivityCompat.startActivityForResult(this, Intent.createChooser(intent, "select file"),
                    100, null);


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                final Uri uri = data.getData();
                tvContactBackupLocation2.setText(uri.getPath());
                readText(uri);
            }
        }
    }

    private void readText(Uri uri) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                final String[] strings = line.split(",");
                saveContactProgrammatically(strings[0], strings[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkContactPermission() {
        String[] permissions = {Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final boolean b = Utils.hasPermissions(this, permissions);
        if (!b) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
        return Utils.checkPermission(this, permissions);
    }


    private synchronized List<HashMap<String, String>> readContact() {
        List<HashMap<String, String>> contactsList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(CONTACT_BASE_URI, null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            toastMessage("No contact found!");
            return contactsList;
        }
        while (cursor.moveToNext()) {
            HashMap<String, String> contactMap = new HashMap<>();
            contactMap.put(CONTACT_NAME_KEY, cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
            contactMap.put(CONTACT_NUMBER_KEY, cursor.getString(cursor.getColumnIndex(NUMBER)));
            contactsList.add(contactMap);
        }
        cursor.close();
        Log.d(TAG, "readContact: contactMap : " + contactsList);
        return contactsList;
    }

    private String parseInCsv(List<HashMap<String, String>> contactsList) {
        StringBuilder sb = new StringBuilder();
        for (HashMap<String, String> contact : contactsList) {
            sb.append(contact.get(CONTACT_NAME_KEY)).append(',').append(contact.get(CONTACT_NUMBER_KEY)).append('\n');
        }
        return sb.toString();
    }

    private void saveFile(String fileContent) {
        try {
            final String uniqueId = Utils.getUniqueId();
            String fileName = uniqueId.substring(10) + "_ContactBackup.txt";
            File myExternalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName);
            final String path = myExternalFile.getPath();
            tvContactBackupLocation1.setText(path);
            FileOutputStream fos = new FileOutputStream(myExternalFile);
            fos.write(fileContent.getBytes());
            fos.flush();
            fos.close();
            toastMessage("File saved");
        } catch (IOException e) {
            e.printStackTrace();
            toastMessage("File not saved");
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private boolean isExternalStorageAvailableForRW() {
        String extraStorageState = Environment.getExternalStorageState();
        return extraStorageState.equals(Environment.MEDIA_MOUNTED);
    }


    private void saveContactProgrammatically(String displayName, String mobileNumber) {
        ArrayList<ContentProviderOperation> contentProviderOperationArrayList = new ArrayList<ContentProviderOperation>();

        contentProviderOperationArrayList.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //Name
        if (displayName != null) {
            contentProviderOperationArrayList.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            displayName).build());
        }

        //Mobile Number
        if (mobileNumber != null) {
            contentProviderOperationArrayList.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        // Creating new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentProviderOperationArrayList);
            Log.d(TAG, "saveContactProgrammatically: saved");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private class SaveContact extends AsyncTask<Void, Void, String> {

        private final ProgressDialog progressDialog = new ProgressDialog(BackupAndRestoreActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setProgressTitle("Backup Contacts..");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            final List<HashMap<String, String>> hashMaps = readContact();
            return parseInCsv(hashMaps);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            saveFile(s);
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}