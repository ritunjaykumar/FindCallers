package com.softgyan.findcallers.widgets.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.widgets.adapter.CallLogAdapter;

import java.util.ArrayList;
import java.util.List;


public class CallFragment extends Fragment {

    private CallLogAdapter callAdapter;
    private final List<CallModel> callList = new ArrayList<>(CommVar.callList);

    private RecyclerView recyclerView;

    public CallFragment() {
        // Required empty public constructor
    }


    public static CallFragment getInstance() {
        return new CallFragment();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        callAdapter.notifyDataSetChanged();
    }

    private final CallLogAdapter.CallLogCallback callLogCallback = new CallLogAdapter.CallLogCallback() {
        @Override
        public void onClickMoreOption(CallModel callModel, int position, int options) {

        }

        @Override
        public void onClickItemView() {

        }
    };
}