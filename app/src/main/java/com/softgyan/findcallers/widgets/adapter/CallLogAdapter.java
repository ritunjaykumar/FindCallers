package com.softgyan.findcallers.widgets.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.softgyan.findcallers.models.CallModel;
import com.softgyan.findcallers.models.CallNumberModel;
import com.softgyan.findcallers.utils.CallUtils;
import com.softgyan.findcallers.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "CallLogAdapter";
    private final List<CallModel> callModelList;
    private final List<CallModel> callModelListBackup;
    private final Context mContext;
    private final CallLogCallback logCallback;

    public CallLogAdapter(Context context, final List<CallModel> callModelList, final CallLogCallback logCallback) {
        this.callModelList = callModelList;
        this.callModelListBackup = new ArrayList<>(callModelList);
        this.mContext = context;
        this.logCallback = logCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_call_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CallModel callModel = callModelList.get(position);
        if (callModel.getCacheName() == null || callModel.getCacheName().length() == 0) {
            holder.tvName.setText(String.format("unknown_%s", callModel.getFirstCall().getNumber()));
        } else {
            holder.tvName.setText(callModel.getCacheName());
        }
        final CallNumberModel callNumber = callModel.getFirstCall();
        if (callNumber == null) return;


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
//        Log.d(TAG, "onBindViewHolder: subscription id : "+subscriptionId);

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


        holder.ibMoreInfo.setOnClickListener(v -> onItemClicked(holder.itemView, callModel, position));

        holder.itemView.setOnClickListener(v -> {
            CallHardware.makeCall(mContext, callModel.getCallNumberList().get(0).getNumber());

        });

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


    @Override
    public int getItemCount() {
        return callModelList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            final List<CallModel> userInfoModelListFiltered = new ArrayList<>();
            if (constraint == null || constraint.toString().isEmpty()) {
                userInfoModelListFiltered.addAll(callModelListBackup);
            } else {
                for (CallModel model : callModelListBackup) {
                    if (model.getCallNumberList().get(0).getNumber().contains(constraint.toString())) {
                        userInfoModelListFiltered.add(model);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = userInfoModelListFiltered;

            return filterResults;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            callModelList.clear();
            callModelList.addAll((Collection<? extends CallModel>) results.values);
            notifyDataSetChanged();
        }
    };


    private void onItemClicked(final View itemView, final CallModel callModel, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, itemView);
        popupMenu.inflate(R.menu.call_log_menu);
        popupMenu.setGravity(Gravity.END);

        Log.d(TAG, "onItemClicked: callModel : " + callModel);

        if (callModel.getCacheName() != null) {
            Log.d(TAG, "onItemClicked: cachedName : " + callModel.getCacheName());
            popupMenu.getMenu().findItem(R.id.search_number).setVisible(false);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            final int id = item.getItemId();
            if (id == R.id.call) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.CALL);
            } else if (id == R.id.search_number) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.SEARCH_NUMBER);
            } else if (id == R.id.save_number) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.SAVE_NUMBER);
            } else if (id == R.id.add_to_block) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.ADD_TO_BLOCK);
            } else if (id == R.id.add_to_spam) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.ADD_TO_SPAM);
            } else if (id == R.id.all_history) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.ALL_HISTORY);
            } else if (id == R.id.delete) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.DELETE);
            } else if (id == R.id.send_sms) {
                logCallback.onClickMoreOption(callModel, position, CallLogCallback.SEND_SMS);
            } else if (id == R.id.exit) {
                popupMenu.dismiss();
            } else {
                return false;
            }
            return true;
        });
        popupMenu.show();
        logCallback.onClickItemView();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        int SAVE_NUMBER = 0;
        int ADD_TO_BLOCK = 1;
        int ADD_TO_SPAM = 2;
        int ALL_HISTORY = 3;
        int CALL = 4;
        int DELETE = 5;
        int SEND_SMS = 6;
        int SEARCH_NUMBER = 7;

        void onClickMoreOption(final CallModel callModel, final int position, final int options);

        void onClickItemView();
    }


}
