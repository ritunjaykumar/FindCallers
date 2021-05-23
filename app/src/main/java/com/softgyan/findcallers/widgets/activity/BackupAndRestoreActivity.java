package com.softgyan.findcallers.widgets.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BackupAndRestoreActivity extends AppCompatActivity {
    private static final String TAG = BackupAndRestoreActivity.class.getName();
    private CheckBox cbCallLog, cbContact;
    private Button btnBackup, btnSelectContact, btnSelectCallLog, btnRestoreContact, btnRestoreCallLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_and_restore);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Backup And Restore");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final List<HashMap<String, Object>> allContactForBackup = Utils.getAllContactForBackup(this);
        for (HashMap<String, Object> mapList : allContactForBackup) {
            Log.d(TAG, "onCreate: map value : " + mapList);
        }
    }
}