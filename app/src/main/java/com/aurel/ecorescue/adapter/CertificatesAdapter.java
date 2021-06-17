package com.aurel.ecorescue.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Certificate;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class CertificatesAdapter extends RecyclerView.Adapter {


    private List<Certificate> items;
    private NavController navController;
    private Certificate addAdditionalCertificate;

    public CertificatesAdapter(NavController navController, Context context) {
        this.navController = navController;

//        Certificate certificate = new Certificate();
//        certificate.setObjectId("-1");
//        certificate.setUrl("");
//        certificate.setTitle(context.getResources().getString(R.string.add_additional_certificates));
//        certificate.setState(1);
//        this.addAdditionalCertificate = certificate;
//        this.items = setUpItemsList(new ArrayList<>());
        this.items = new ArrayList<>();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new CertificatesViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final CertificatesViewHolder viewHolder = (CertificatesViewHolder)holder;
        Certificate item = items.get(position);

//        viewHolder.title.setText(ParseUtils.getString(item.getTitle()));

//        if (item.getObjectId().equals("-1")) {
//            viewHolder.title.setText(ParseUtils.getString(item.getTitle()));
//            viewHolder.container.setOnClickListener(v -> {
//                navController.navigate(R.id.action_certificationFragment_to_addCertificationFragment);
//            });
//        } else {
//
//
//        }
        viewHolder.title.setText(viewHolder.title.getContext().getResources().getString(R.string.other_document));
        viewHolder.container.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("id", item.getObjectId());
            navController.navigate(R.id.action_certificationFragment_to_otherCertificationFragment, bundle);
        });

        viewHolder.container.setOnClickListener(v -> {
            if (item.getObjectId().equals("-1")) {
                navController.navigate(R.id.action_certificationFragment_to_addCertificationFragment);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("id", item.getObjectId());
                navController.navigate(R.id.action_certificationFragment_to_otherCertificationFragment, bundle);
            }
        });
    }


    @Override
    public int getItemCount() {
        if (items == null) return 0;
        return items.size();
    }

    class CertificatesViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout container;
        private TextView title;

        CertificatesViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false));
            container = itemView.findViewById(R.id.rl_container);
            title = itemView.findViewById(R.id.tv_title);
        }
    }

    public void setItems(List<Certificate> list) {
//        items = setUpItemsList(list);
        items = list;
        notifyDataSetChanged();
    }

//    private List<Certificate> setUpItemsList(List<Certificate> list){
//        if (!list.contains(addAdditionalCertificate)) {
//            list.add(addAdditionalCertificate);
//        }
//        return list;
//    }

}
