package com.softgyan.findcallers.widgets.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallNumberModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.AllCallNumberAdapter;

import java.util.ArrayList;
import java.util.List;

public class AllCallHistoryActivity extends AppCompatActivity {
    private static final String TAG = "AllCallHistoryActivity";
    public static final String CALL_MODEL_KEY = "callModelKey";

    private AllCallNumberAdapter callLogAdapter;

    private CallModel mCallModel;


    private List<CallNumberModel> callNumberModels;
    private TextView tvAlertMessage;
    private RecyclerView rvCallHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_call_history);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            mCallModel = (CallModel) getIntent().getSerializableExtra(CALL_MODEL_KEY);
            callNumberModels = new ArrayList<>(mCallModel.getCallNumberList());
        }
        if (mCallModel.getCacheName() == null) {
            getSupportActionBar().setTitle("All Call History");
        } else {
            getSupportActionBar().setTitle(mCallModel.getCacheName());
        }

        RadioGroup radioGroup = findViewById(R.id.rgCallOption);
        tvAlertMessage = findViewById(R.id.tvEmpty);
        rvCallHistory = findViewById(R.id.rvCallHistory);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int id = group.getCheckedRadioButtonId();
            onChangeRadio(id);
        });

        setRecyclerView();

        Log.d(TAG, "onCreate: callModel : " + mCallModel);
    }

    private void onChangeRadio(int id) {
        callNumberModels.clear();
        if (id == R.id.rbAll) {
            callNumberModels.addAll(mCallModel.getCallNumberList());
        } else if (id == R.id.rbMissed) {
            for (CallNumberModel model : mCallModel.getCallNumberList()) {
                if (model.getType() == CommVar.MISSED_TYPE) {
                    callNumberModels.add(model);
                }
            }
        } else if (id == R.id.rbIncoming) {
            for (CallNumberModel model : mCallModel.getCallNumberList()) {
                if (model.getType() == CommVar.INCOMING_TYPE || model.getType() == CommVar.REJECTED_TYPE) {
                    callNumberModels.add(model);
                }
            }
        } else if (id == R.id.rbOutgoing) {
            for (CallNumberModel model : mCallModel.getCallNumberList()) {
                if (model.getType() == CommVar.OUTGOING_TYPE) {
                    callNumberModels.add(model);
                }
            }
        }
        refreshLayout();
        setLayout();
        invalidateOptionsMenu();
    }

    private void setRecyclerView() {

        callLogAdapter = new AllCallNumberAdapter(callNumberModels, this, mCallModel.getCacheName(), (callModel, position) -> {
            final int i = CallQuery.deleteSingleCallLog(AllCallHistoryActivity.this, callModel.getCallModelId(), callModel.getNameRefId());
            if (i != 0) {
                final boolean remove = callNumberModels.remove(callModel);
                Log.d(TAG, "onDeleteCall: is removed from callNumberModel : " + remove);
                final boolean remove1 = mCallModel.getCallNumberList().remove(callModel);
                Log.d(TAG, "onDeleteCall: is removed from callModel :" + remove1);

                for (CallModel callModel1 : CommVar.callList) {
                    if (callModel1.getNameId() == mCallModel.getNameId()) {
                        final List<CallNumberModel> callNumberList = callModel1.getCallNumberList();
                        final boolean b = callNumberList.removeIf(numberModel -> numberModel.getCallModelId() == callModel.getCallModelId());
                        Log.d(TAG, "setRecyclerView: is deleted : "+b);
                        break;
                    }
                }

                invalidateOptionsMenu();
                refreshLayout();
            }
        });
        rvCallHistory.setAdapter(callLogAdapter);
    }


    private void setLayout() {
        if (callNumberModels.size() == 0) {
            Utils.showViews(tvAlertMessage);
            Utils.hideViews(rvCallHistory);
        } else {
            Utils.showViews(rvCallHistory);
            Utils.hideViews(tvAlertMessage);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshLayout() {
        callLogAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        if (callNumberModels != null) {
            final MenuItem item = menu.findItem(R.id.menu_delete);
            item.setTitle(String.valueOf(callNumberModels.size()));
            item.setVisible(true);
        } else {
            menu.findItem(R.id.menu_delete).setVisible(false);
        }
        Log.d(TAG, "onCreateOptionsMenu: called");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}