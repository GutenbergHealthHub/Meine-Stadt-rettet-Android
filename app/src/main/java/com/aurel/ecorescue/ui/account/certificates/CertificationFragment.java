package com.aurel.ecorescue.ui.account.certificates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.CertificatesAdapter;
import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.profile.CertificateStatusData;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.utils.StyleUtils;

import org.jetbrains.annotations.NotNull;


public class CertificationFragment extends Fragment {

    public CertificationFragment() {}

    private NavController navController;
    private CertificatesAdapter mAdapter;
    private CertificatesRepository mRepository;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certification, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }
        mRepository = CertificatesRepository.getInstance();
        view.findViewById(R.id.rl_fr_certification).setOnClickListener(v -> {
            navController.navigate(R.id.action_certificationFragment_to_frCertificationFragment);
        });

        TextView tvStatus = view.findViewById(R.id.tv_fr_certificate_status);

        AppExecutors appExecutors = new AppExecutors();
        appExecutors.networkIO().execute(() -> {
            CertificateStatusData certificateStatusData = new CertificateStatusData().createFromCurrentUser();
            appExecutors.mainThread().execute(() -> {
                switch (certificateStatusData.getCertificateStatus()) {
                    case REVIEWED:
                        tvStatus.setText(R.string.first_responder_certificate_state_reviewed);
                        tvStatus.setTextColor(view.getContext().getResources().getColor(R.color.md_green_800));
                        break;
                    case IN_REVIEW:
                        tvStatus.setText(R.string.first_responder_certificate_state_inreview);
                        tvStatus.setTextColor(view.getContext().getResources().getColor(R.color.md_yellow_800));
                        break;
                    case NOT_SUBMITTED:
                        tvStatus.setText(R.string.first_responder_certificate_state_notsubmitted);
                        tvStatus.setTextColor(view.getContext().getResources().getColor(R.color.secondary_text));
                        break;
                }
            });

        });

        view.findViewById(R.id.rl_add_additional_certificate).setOnClickListener(v -> {
            navController.navigate(R.id.action_certificationFragment_to_addCertificationFragment);
        });

        RecyclerView recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new CertificatesAdapter(navController, view.getContext());
        recyclerView.setAdapter(mAdapter);

        mRepository.loadCertificates();
        mRepository.getCertificates().observe(getViewLifecycleOwner(), certificates -> {
            if (certificates!=null) {
                mAdapter.setItems(certificates);
            }
        });
    }


}
