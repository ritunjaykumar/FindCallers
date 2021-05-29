package com.softgyan.findcallers.widgets.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.findcallers.R;
import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.models.BlockNumberModel;


import java.util.List;

public class BlockListAdapter extends RecyclerView.Adapter<BlockListAdapter.ViewHolder> {

    private final List<BlockNumberModel> blockNumberModelList;
    private final Context context;
    private final BlockListCallback callback;

    public BlockListAdapter(@NonNull Context context, List<BlockNumberModel> blockNumberModelList, BlockListCallback callback) {
        this.blockNumberModelList = blockNumberModelList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.block_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BlockNumberModel blockNumberModel = blockNumberModelList.get(position);
        holder.tvNumber.setText(blockNumberModel.getNumber());
        holder.tvName.setText(blockNumberModel.getName());
        final int type = blockNumberModel.getType();
        if(type == SpamContract.BLOCK_TYPE){
            holder.tvType.setText("Block");
        }else {
            holder.tvType.setText("Spam");
        }
        holder.ibDelete.setOnClickListener(v->{
            callback.onBlockedNumberDeleted(position, blockNumberModel);
        });
    }

    @Override
    public int getItemCount() {
        return blockNumberModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvNumber, tvType;
        private final ImageButton ibDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvType = itemView.findViewById(R.id.tvType);
            ibDelete = itemView.findViewById(R.id.ibRemove);

        }
    }

    public interface BlockListCallback{
        void onBlockedNumberDeleted(final int position, final BlockNumberModel blockModel);
    }
}
