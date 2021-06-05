package com.softgyan.findcallers.widgets.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.models.BusinessRecord;
import com.softgyan.findcallers.models.DoctorModel;
import com.softgyan.findcallers.models.ElectricianModel;
import com.softgyan.findcallers.utils.Utils;

import java.util.List;

public class BusinessRecordAdapter extends RecyclerView.Adapter<BusinessRecordAdapter.ViewHolder> {
    private final Context mContext;
    private final BusinessRecord businessRecord;

    public BusinessRecordAdapter(Context context, BusinessRecord businessRecord) {
        this.businessRecord = businessRecord;
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
        int id = businessRecord.getType();
        if (id == BusinessRecord.DOCTOR_TYPE) {
            final List<DoctorModel> doctorList = businessRecord.getDoctorList();
            DoctorModel doctorModel = doctorList.get(position);
            holder.tvName.setText(doctorModel.getName());
            holder.tvDoctorType.setText(doctorModel.getDoctorType());
            holder.tvArea.setText(doctorModel.getArea());
            holder.tvDistrict.setText(doctorModel.getDistrict());
            holder.tvCountry.setText(doctorModel.getState());
        } else if (id == BusinessRecord.ELECTRICIAN_TYPE) {
            final List<ElectricianModel> electricianList = businessRecord.getElectricianList();
            ElectricianModel electricianModel = electricianList.get(position);
            holder.tvName.setText(electricianModel.getName());
            holder.tvArea.setText(electricianModel.getArea());
            holder.tvDistrict.setText(electricianModel.getDistrict());
            holder.tvCountry.setText(electricianModel.getState());
            Utils.hideViews(holder.tvDoctorType);
        }
    }

    @Override
    public int getItemCount() {
        if (businessRecord.getType() == BusinessRecord.DOCTOR_TYPE) {
            return businessRecord.getDoctorList().size();
        } else if (businessRecord.getType() == BusinessRecord.ELECTRICIAN_TYPE) {
            return businessRecord.getElectricianList().size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDoctorType, tvArea, tvDistrict, tvCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDoctorType = itemView.findViewById(R.id.tvDoctorType);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvCountry = itemView.findViewById(R.id.tvCountry);
        }
    }
}
