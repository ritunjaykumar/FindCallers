package com.softgyan.findcallers.widgets.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.CommVar;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.models.CallNumberModel;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.Utils;

import java.util.List;

public class AllCallNumberAdapter extends RecyclerView.Adapter<AllCallNumberAdapter.ViewHolder> {
    private static final String TAG = "AllCallNumberAdapter";
    private final List<CallNumberModel> callNumberList;
    private final Context mContext;
    private final CallLogCallback callback;
    private final String name;

    public AllCallNumberAdapter(List<CallNumberModel> callNumberList, Context context, String name, CallLogCallback callback) {
        this.callNumberList = callNumberList;
        this.mContext = context;
        this.callback = callback;
        this.name = name;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_call_adapter, parent, false);
        return new AllCallNumberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllCallNumberAdapter.ViewHolder holder, int position) {

        final CallNumberModel callNumber = callNumberList.get(position);
        if (callNumber == null) return;
        if (name != null) {
            holder.tvName.setText(name);
        } else {
            holder.tvName.setText("unknown_" + callNumber.getNumber());
        }
        switch (callNumber.getType()) {
            case (CommVar.INCOMING_TYPE): {
                changeColor(R.color.colorGreen, holder.tvName, holder.ivCallType);
                holder.ivCallType.setImageResource(R.drawable.ic_in_coming);
                break;
            }
            case (CommVar.REJECTED_TYPE): {
                holder.ivCallType.setImageResource(R.drawable.ic_in_coming);
                changeColor(R.color.colorGreen, holder.tvName, holder.ivCallType);
                break;
            }
            case (CommVar.OUTGOING_TYPE): {
                holder.ivCallType.setImageResource(R.drawable.ic_out_going);
                changeColor(R.color.accent, holder.tvName, holder.ivCallType);
                break;
            }
            case (CommVar.MISSED_TYPE): {
                holder.ivCallType.setImageResource(R.drawable.ic_missed);
                changeColor(R.color.colorPrimary, holder.tvName, holder.ivCallType);
                break;
            }
            case (CommVar.BLOCKED_TYPE): {
                holder.itemView.setBackground(ContextCompat.getDrawable(mContext, R.color.primary_light));
                holder.ivCallType.setImageResource(R.drawable.ic_missed);
                break;
            }
        }

        final int subscriptionId = CallUtils.getSubscriptionId(mContext, callNumber.getIccId());
        switch (subscriptionId) {
            case CommVar.SIM_ONE: {
                holder.ivSimId.setImageResource(R.drawable.sim_1);
                break;
            }
            case CommVar.SIM_TWO: {
                holder.ivSimId.setImageResource(R.drawable.sim_2);
                break;
            }
            default: {
                Utils.hideViews(holder.ivSimId);
            }

        }

        holder.tvNumber.setText(callNumber.getNumber());

        holder.tvDate.setText(callNumber.getDate().toString().substring(0, 10));


        holder.ibMoreInfo.setOnClickListener(v -> {
            onItemClicked(holder.itemView, callNumber, position);
        });

        holder.itemView.setOnClickListener(v -> {
            CallHardware.makeCall(mContext, callNumber.getNumber());

        });

    }

    @Override
    public int getItemCount() {
        return callNumberList.size();
    }

    private void changeColor(int colorCode, View... views) {
        for (View view : views) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(ContextCompat.getColor(mContext, colorCode));
            } else if (view instanceof ImageView) {
                ((ImageView) view).setImageTintList(ContextCompat.getColorStateList(mContext, colorCode));
            }
        }

    }


    private void onItemClicked(final View itemView, final CallNumberModel callModel, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, itemView);
        popupMenu.inflate(R.menu.delete_menu);
        popupMenu.setGravity(Gravity.END);

        popupMenu.setOnMenuItemClickListener(item -> {
            final int id = item.getItemId();
            if (id == R.id.menu_delete) {
                callback.onDeleteCall(callModel, position);
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCallType, ivSimId;
        private final TextView tvName, tvNumber, tvDate;
        private final ImageButton ibMoreInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCallType = itemView.findViewById(R.id.ivCallType);
            ivSimId = itemView.findViewById(R.id.ivSimId);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvDate = itemView.findViewById(R.id.tvDate);
            ibMoreInfo = itemView.findViewById(R.id.ibMoreInfo);
        }
    }


    public interface CallLogCallback {
        void onDeleteCall(final CallNumberModel callModel, final int position);
    }
}
