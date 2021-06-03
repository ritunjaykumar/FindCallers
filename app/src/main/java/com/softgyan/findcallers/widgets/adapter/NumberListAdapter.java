package com.softgyan.findcallers.widgets.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.hardware.CallHardware;
import com.softgyan.findcallers.utils.CopyPasteUtils;

import java.util.ArrayList;

public class NumberListAdapter extends RecyclerView.Adapter<NumberListAdapter.ViewHolder> {
    private final ArrayList<String> numbers;
    private final Context context;
    private boolean isGravityStart;

    public NumberListAdapter(ArrayList<String> numbers, Context context) {
        this.numbers = numbers;
        this.context = context;
        this.isGravityStart = false;
    }

    public NumberListAdapter(ArrayList<String> numbers, Context context, boolean isGravityStart) {
        this(numbers, context);
        this.isGravityStart = isGravityStart;
    }

    @NonNull
    @Override
    public NumberListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_custom_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberListAdapter.ViewHolder holder, int position) {
        holder.tvNumber.setText(numbers.get(position));
        if (isGravityStart) {
            holder.tvNumber.setGravity(Gravity.START);
            holder.tvNumber.setPadding(4, 4, 4, 4);
            holder.tvNumber.setTextSize(20);
        }
        holder.itemView.setOnClickListener(v -> {
            CallHardware.makeCall(context, numbers.get(position));
        });

        holder.itemView.setOnLongClickListener(v -> {
            CopyPasteUtils.copyToClipboard(context, numbers.get(position));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvCustomView);
        }
    }
}
