package com.softgyan.findcallers.widgets.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.softgyan.findcallers.R;
import com.softgyan.findcallers.models.ContactModel;
import com.softgyan.findcallers.widgets.activity.ContactDetailActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements Filterable {
    public static final String POSITION = "position";
    private final List<ContactModel> contactList;
    private final List<ContactModel> contactListAll;
    private final Context mContext;
    private static final String TAG = "my_tag";

    public ContactsAdapter(Context mContext, List<ContactModel> contactList) {
        this.contactList = contactList;
        this.contactListAll = new ArrayList<>(contactList);
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_contact_adapter, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contactModel = contactList.get(position);
        if (contactModel.getName() == null) {
            String name = "unknown_" + contactModel.getContactNumbers().get(0).getMobileNumber();
            holder.tvName.setText(name);
        } else {
            holder.tvName.setText(contactModel.getName());
        }
        if (contactModel.getImage() != null) {
            Glide.with(mContext).load(contactModel.getImage()).into(holder.sivProfile);
        } else {
            holder.tvProfile.setText(String.valueOf(contactModel.getName().charAt(0)));
        }


        holder.itemView.setOnClickListener(v -> {
            try {
                Intent contactDetailsIntent = new Intent(mContext, ContactDetailActivity.class);
                contactDetailsIntent.putExtra(ContactDetailActivity.CONTACT_MODEL_KEY, contactModel);
                contactDetailsIntent.putExtra(POSITION, position);
                mContext.startActivity(contactDetailsIntent);

            } catch (Exception e) {
                Log.d(TAG, "onBindViewHolder: " + e.getMessage());
            }

        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            final List<ContactModel> userInfoFiltered = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                userInfoFiltered.addAll(contactListAll);
            } else {
                for (ContactModel contactModel : contactListAll) {
                    if (contactModel.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        userInfoFiltered.add(contactModel);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = userInfoFiltered;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            contactList.addAll((Collection<? extends ContactModel>) results.values);
            notifyDataSetChanged();
        }
    };


    //    view holder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvProfile;
        private final ShapeableImageView sivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvProfile = itemView.findViewById(R.id.tvProfile);
            sivProfile = itemView.findViewById(R.id.sivProfile);
        }
    }


}
