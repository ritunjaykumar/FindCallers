package com.softgyan.findcallers.widgets.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.widgets.adapter.ContactsAdapter;

import java.util.ArrayList;
import java.util.List;


public class ContactFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ContactFragment.class.getName();
    private final List<ContactModel> contactList =CommVar.contactsList;
    private RecyclerView recyclerView;
    private EditText etSearchView;
    private ContactsAdapter contactsAdapter;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        etSearchView = view.findViewById(R.id.etSearchView);

        contactsAdapter = new ContactsAdapter(getContext(), contactList);
        recyclerView.setAdapter(contactsAdapter);
        recyclerView.setAdapter(contactsAdapter);

        ImageButton ibClear = view.findViewById(R.id.ibClear);
        ibClear.setOnClickListener(this);


        etSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //do nothing
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        contactsAdapter.notifyDataSetChanged();
        Log.d(TAG, "onStart: start contact fragment");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibClear) {
            etSearchView.setText(null);
        }
    }
}


