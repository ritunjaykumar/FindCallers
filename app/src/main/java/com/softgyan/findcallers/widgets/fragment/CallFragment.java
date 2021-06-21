package com.softgyan.findcallers.widgets.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.database.contacts.system.SystemContacts;
import com.softgyan.findcallers.database.query.CallQuery;
import com.softgyan.findcallers.database.query.SpamQuery;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.firebase.FirebaseDB;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.BlockNumberModel;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.models.ContactNumberModel;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.activity.AllCallHistoryActivity;
import com.softgyan.findcallers.widgets.activity.ScanNumberActivity;
import com.softgyan.findcallers.widgets.adapter.CallLogAdapter;
import com.softgyan.findcallers.widgets.dialog.DialingPadBehavior;
import com.softgyan.findcallers.widgets.dialog.InputDialog;

import java.util.HashMap;
import java.util.List;


public class CallFragment extends Fragment {
    private static final String TAG = "CallFragment";

    private CallLogAdapter callAdapter;
    private final List<CallModel> callList = CommVar.callList;
    private FloatingActionButton fabHideShow;
    private BottomSheetBehavior<View> sheetBehavior;
    private RecyclerView recyclerView;


    public CallFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        callAdapter = new CallLogAdapter(getContext(), callList, false, callLogCallback);
        return inflater.inflate(R.layout.fragment_call, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(callAdapter);
        CardView cardView = view.findViewById(R.id.dialing_layout);
        fabHideShow = view.findViewById(R.id.fab_hide_show_key_pad);
        setupBottomSheetBehavior(cardView);
        fabHideShow.setOnClickListener(v -> {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            fabHideShow.hide();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        callAdapter.notifyDataSetChanged();
    }

    final CallLogAdapter.CallLogCallback callLogCallback = new CallLogAdapter.CallLogCallback() {
        @Override
        public void onClickMoreOption(CallModel callModel, int position, int options) {
            switch (options) {
                case (CallLogAdapter.CallLogCallback.ADD_TO_BLOCK): {
                    insertSpamOrBlock(callModel, false);
                    break;
                }
                case (CallLogAdapter.CallLogCallback.ADD_TO_SPAM): {
                    insertSpamOrBlock(callModel, true);
                    break;
                }
                case (CallLogAdapter.CallLogCallback.ALL_HISTORY): {
                    startActivity(new Intent(getContext(), AllCallHistoryActivity.class)
                            .putExtra(AllCallHistoryActivity.CALL_MODEL_KEY, callModel));
                    break;
                }
                case (CallLogAdapter.CallLogCallback.DELETE): {
                    deleteCAllHistory(callModel);
                    break;
                }
                case (CallLogAdapter.CallLogCallback.SAVE_NUMBER): {
                    final String[] name = new String[1];
                    if (callModel.getCacheName() == null) {
                        InputDialog inputDialog = new InputDialog(getContext(), (dialog, text) -> {
                            name[0] = text;
                            dialog.dismiss();
                        });
                        inputDialog.setTitle("Enter name");
                        inputDialog.show();
                    } else {
                        name[0] = callModel.getCacheName();
                    }

                    ContactModel contactModel = new ContactModel(name[0]);
                    contactModel.setContactNumbers(new ContactNumberModel(callModel.getCallNumberList().get(0).getNumber()));
                    SystemContacts.saveContactToSystem(getContext(), contactModel);
                }

            }
        }

        @Override
        public void onClickItemView() {
            hideBehavior();
        }
    };

    private void deleteCAllHistory(CallModel callModel) {
        final int i = CallQuery.deleteAllCallLog(getContext(), callModel.getNameId());
        if (i > 0) {
            callList.removeIf(cModel -> cModel.getNameId() == callModel.getNameId());
            callAdapter.notifyDataSetChanged();
        }
    }


    private void insertSpamOrBlock(CallModel callModel, boolean isSpam) {

        BlockNumberModel blockModel = new BlockNumberModel();
        blockModel.setNumber(callModel.getCallNumberList().get(0).getNumber());
        if (isSpam) {
            blockModel.setType(SpamContract.SPAM_TYPE);
            uploadSpamOnServer(callModel);
        } else {
            blockModel.setType(SpamContract.BLOCK_TYPE);
        }
        blockModel.setName(callModel.getCacheName());
        final int i = SpamQuery.insertBlockList(getContext(), blockModel);
        if (i != 0) {
            Toast.makeText(getContext(), "save successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "already exits ", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadSpamOnServer(CallModel callModel) {

        if (!Utils.isInternetConnectionAvailable(requireContext())) {
            return;
        }

        HashMap<String, Object> spamMap = new HashMap<>();
        spamMap.put(FirebaseVar.SpamDB.MOBILE_NUMBER, callModel.getCallNumberList().get(0).getNumber());
        spamMap.put(FirebaseVar.SpamDB.SPAM_TYPE_KEY, "fraud");
        spamMap.put(FirebaseVar.SpamDB.NAME , callModel.getCacheName());
        spamMap.put(FirebaseVar.SpamDB.TOTAL_SPAM_VOTE, 1);
        spamMap.put(FirebaseVar.SpamDB.TOTAL_NAME, 1);

        FirebaseDB.SpamDB.uploadSpamNumber(spamMap, null);

    }

    private void setupBottomSheetBehavior(CardView cardView) {
        DialingPadBehavior dialingPadBehavior = new DialingPadBehavior(requireContext(), cardView, dialing);
        sheetBehavior = dialingPadBehavior.getBottomSheetBehavior();
    }

    private final DialingPadBehavior.DailingPadBehaviorListener dialing = new DialingPadBehavior.DailingPadBehaviorListener() {
        @Override
        public void onStateChange(BottomSheetBehavior<View> bottomSheetBehavior, int state) {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                fabHideShow.hide();
            } else if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_HIDDEN) {
                fabHideShow.show();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        @Override
        public void onClickOnKey(String key) {
            callAdapter.getFilter().filter(key);
            Log.d(TAG, "onClickOnKey: size of list : " + callList.size());
            if (callList.size() > 0) {
                recyclerView.scrollToPosition(0);
            }
        }

        @Override
        public void onClickOnCallButton(final String number) {
            CallHardware.makeCall(requireContext(), number);
        }

        @Override
        public void onClickCamera() {
            Intent intent = new Intent(getContext(), ScanNumberActivity.class);
            startActivity(intent);
        }
    };

    public void hideBehavior() {
        if (sheetBehavior != null)
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void showBehavior() {
        if (sheetBehavior != null)
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


}