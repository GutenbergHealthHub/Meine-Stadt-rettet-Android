package com.aurel.ecorescue.ui.account.certificates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.model.Certificate;
import com.aurel.ecorescue.utils.StyleUtils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;


public class OtherCertificationFragment extends Fragment {

    public OtherCertificationFragment() {}

    private NavController navController;
    private CertificatesRepository mRepository;
    private String objectId = "";
    private View view;
    private ImageView mCertificate;

    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certification_other, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        mRepository = CertificatesRepository.getInstance();
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        view.findViewById(R.id.btn_delete_certificate).setOnClickListener(v -> deleteImage());
        mCertificate = view.findViewById(R.id.iv_current_certificate);

        Bundle bundle = getArguments();
        if (bundle != null) {
            objectId = bundle.getString("id", "");
            loadCurrentCertificate();
        }

    }


    private void deleteImage(){
        mRepository.deleteCertificate(objectId);
        navController.navigateUp();
    }


    private void loadCurrentCertificate(){

        Certificate certificate = mRepository.getCertificate(objectId);
        Glide.with(view.getContext())
                .load(certificate.getUrl())
                .centerCrop()
                .placeholder(StyleUtils.getCircularProgressDrawable(view.getContext()))
                .into(mCertificate);
    }



}
