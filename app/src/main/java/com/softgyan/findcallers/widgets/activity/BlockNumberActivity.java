package com.softgyan.findcallers.widgets.activity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.query.SpamQuery;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.models.BlockNumberModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.adapter.BlockListAdapter;
import com.softgyan.findcallers.widgets.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class BlockNumberActivity extends AppCompatActivity implements BlockListAdapter.BlockListCallback {
    private RadioGroup radioGroup;
    private List<BlockNumberModel> blockNumberModelList;
    private List<BlockNumberModel> blockNumberModelListTemp;
    private RecyclerView recyclerView;
    private BlockListAdapter adapter;
    private TextView alertMessage;
    private static final String TAG = "BlockNumberActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_number);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Blocked List");
        }
        initComponent();
    }

    private void initComponent() {
        radioGroup = findViewById(R.id.spamOptionGroup);
        recyclerView = findViewById(R.id.rbBlockList);
        alertMessage = findViewById(R.id.tvEmpty);
        checkButton();
        if (blockNumberModelList == null) {
            Loader loader = new Loader();
            loader.execute();
        } else {
            refreshLayout();
        }
    }

    private void setRecyclerView(List<BlockNumberModel> blockNumberModels) {

        blockNumberModelList = blockNumberModels;
        blockNumberModelListTemp = new ArrayList<>(blockNumberModels);
        setLayout();
        adapter = new BlockListAdapter(this, blockNumberModelListTemp, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onChangeGroupButton(final int id) {
        blockNumberModelListTemp.clear();
        if (id == R.id.rbAll) {
            blockNumberModelListTemp.addAll(blockNumberModelList);

        } else if (id == R.id.rbBlock) {
            for (BlockNumberModel model : blockNumberModelList) {
                if (model.getType() == SpamContract.BLOCK_TYPE) {
                    blockNumberModelListTemp.add(model);
                }
            }

        } else if (id == R.id.rbSpam) {
            for (BlockNumberModel model : blockNumberModelList) {
                if (model.getType() == SpamContract.SPAM_TYPE) {
                    blockNumberModelListTemp.add(model);
                }
            }
        }
        setLayout();
        refreshLayout();
    }

    private void checkButton() {

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int radioId = radioGroup.getCheckedRadioButtonId();
            onChangeGroupButton(radioId);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshLayout() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setLayout() {
        if (blockNumberModelListTemp.size() == 0) {
            Utils.showViews(alertMessage);
            Utils.hideViews(recyclerView);
        } else {
            Utils.showViews(recyclerView);
            Utils.hideViews(alertMessage);
        }
    }

    @Override
    public void onBlockedNumberDeleted(int position, BlockNumberModel blockModel) {
        final int delete = SpamQuery.deleteSingleBlockList(this, blockModel.getId());
        if (delete == 1) {
            try {
                // blocked number is deleted
                blockNumberModelListTemp.remove(blockModel);
                blockNumberModelList.remove(blockModel);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, blockModel.getNumber() + " deleted", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.d(TAG, "onBlockedNumberDeleted: error:  " + e.getMessage());
            }
        }
    }

    public final class Loader extends AsyncTask<Void, Void, List<BlockNumberModel>> {

        ProgressDialog progressDialog = new ProgressDialog(BlockNumberActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected List<BlockNumberModel> doInBackground(Void... voids) {
            final List<BlockNumberModel> blockList = SpamQuery.getBlockListArray(BlockNumberActivity.this, null);
            if (blockList != null) {
                Log.d(TAG, "doInBackground: blockList value : not null");
                return blockList;

            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<BlockNumberModel> blockNumberModels) {
            super.onPostExecute(blockNumberModels);
            setRecyclerView(blockNumberModels);
            refreshLayout();
            progressDialog.dismiss();
        }
    }
}