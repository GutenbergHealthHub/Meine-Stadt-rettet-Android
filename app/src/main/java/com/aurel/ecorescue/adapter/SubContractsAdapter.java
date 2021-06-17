package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.SubContract;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class SubContractsAdapter extends RecyclerView.Adapter {


    private Context context;
    private List<SubContract> items;
    private NavController navController;


    public SubContractsAdapter(Context context, NavController navController) {
        this.context = context;
        this.items = new ArrayList<>();
        this.navController = navController;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new AdapterViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final AdapterViewHolder viewHolder = (AdapterViewHolder)holder;
        SubContract item = items.get(position);

        viewHolder.title.setText(item.getTitle());
        viewHolder.subtitle.setText(item.getSubtitle());
        viewHolder.container.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("agreement", "additional");
            bundle.putString("url", item.getUrl());
            bundle.putString("cc", item.getObjectId());
            bundle.putBoolean("isSigned", item.getState()==1);
            navController.navigate(R.id.agreementSignFragment, bundle);
        });
        if (item.getState()==0) {
            viewHolder.agreementStatus.setImageResource(R.drawable.ic_cancel_white_24dp);
            viewHolder.agreementStatus.setColorFilter(viewHolder.agreementStatus.getContext().getResources().getColor(R.color.md_red_700));

        } else {
            viewHolder.agreementStatus.setImageResource(R.drawable.ic_check_circle_white_24dp);
            viewHolder.agreementStatus.setColorFilter(viewHolder.agreementStatus.getContext().getResources().getColor(R.color.md_green_700));
        }

    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder {
        private ImageView agreementStatus;
        private TextView title, subtitle;
        private RelativeLayout container;

        AdapterViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subcontract, parent, false));
            container = itemView.findViewById(R.id.rl_container);
            agreementStatus = itemView.findViewById(R.id.iv_agreement_status);
            title = itemView.findViewById(R.id.tv_title);
            subtitle = itemView.findViewById(R.id.tv_subtitle);
        }
    }

    public void setItems(List<SubContract> list) {
        items = list;
        notifyDataSetChanged();
    }

}
