package com.softgyan.findcallers.widgets.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.firebase.FirebaseVar;
import com.softgyan.findcallers.utils.Utils;
import com.softgyan.findcallers.widgets.activity.BusinessDetailsActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BusinessRecordAdapter extends RecyclerView.Adapter<BusinessRecordAdapter.ViewHolder> {
    private final Context mContext;
    private final List<Map<String, Object>> businessRecords;
    private static final String TAG = "BusinessRecordAdapter";

    public BusinessRecordAdapter(Context context, List<Map<String, Object>> businessRecord) {
        this.businessRecords = businessRecord;
        Log.d(TAG, "BusinessRecordAdapter: size : " + businessRecord.size());
        this.mContext = context;
    }

    @NonNull
    @Override
    public BusinessRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_business_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusinessRecordAdapter.ViewHolder holder, int position) {
        final HashMap<String, Object> map = (HashMap<String, Object>) businessRecords.get(position);
        String type = (String) map.get(FirebaseVar.Business.DB_TYPE_KEY);
        if (type != null && type.equals(FirebaseVar.Business.DB_POLICE_STATION)) {
            String inspectorName = (String) map.get(FirebaseVar.Business.PoliceInfo.INSPECTOR_NAME);
            if (inspectorName != null) {
                holder.tvName.setText(inspectorName);
            }

            String countryName = (String) map.get(FirebaseVar.Business.PoliceInfo.COUNTRY);
            if (countryName != null) {
                holder.tvArea.setText(countryName);
            }
            String contactNumber = (String) map.get(FirebaseVar.Business.PoliceInfo.CONTACT);
            if (contactNumber != null) {
                holder.tvDoctorType.setText(contactNumber);
            }

            String policeStation = (String) map.get(FirebaseVar.Business.PoliceInfo.POLICE_STATION_NAME);
            if (policeStation != null) {
                holder.tvDistrict.setText(policeStation);
            }

            Double distance = (Double) map.get(FirebaseVar.Business.DISTANCE_KEY);
            if (distance != null) {
                holder.tvDistance.setText(String.format(Locale.getDefault(),"%.2f km",distance));
            }
        } 
        else {
            if (type != null && type.equals(FirebaseVar.Business.DB_DOCTOR)) {
                String doctorType = (String) map.get(FirebaseVar.Business.DOCTOR_TYPE);
                if (doctorType != null) {
                    holder.tvDoctorType.setText(doctorType);
                } else {
                    Utils.hideViews(holder.tvDoctorType);
                }
            } else {
                Utils.hideViews(holder.tvDoctorType);
            }

            String name = (String) map.get(FirebaseVar.Business.NAME);
            if (name != null) {
                holder.tvName.setText(name);
            }

            String area = (String) map.get(FirebaseVar.Business.AREA);
            if (area != null) {
                holder.tvArea.setText(area);
            }

            String district = (String) map.get(FirebaseVar.Business.DISTRICT);
            if (district != null) {
                holder.tvDistrict.setText(district);
            }
            Double distance = (Double) map.get(FirebaseVar.Business.DISTANCE_KEY);
            if (distance != null) {
                holder.tvDistance.setText(String.format(Locale.getDefault(),"%.2f km",distance));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: hello");
            Intent intent = new Intent(mContext, BusinessDetailsActivity.class);
            intent.putExtra(BusinessDetailsActivity.DATA_KEY, map);
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return businessRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDoctorType, tvArea, tvDistrict, tvDistance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDoctorType = itemView.findViewById(R.id.tvDoctorType);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}
