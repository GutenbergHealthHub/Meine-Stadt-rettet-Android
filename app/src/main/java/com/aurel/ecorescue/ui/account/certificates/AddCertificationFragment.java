package com.aurel.ecorescue.ui.account.certificates;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.interfaces.Callback;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;


public class AddCertificationFragment extends Fragment {

    public AddCertificationFragment() {}

    private NavController navController;
    private CertificatesRepository mRepository;
    private String objectId = "";
    private View view;
    private ImageView mCertificate;
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certification_add, container, false);
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

        view.findViewById(R.id.ll_capture_image).setOnClickListener(v -> takeImage());
        view.findViewById(R.id.ll_upload_certificate).setOnClickListener(v -> uploadImage());
        view.findViewById(R.id.btn_delete_certificate).setOnClickListener(v -> deleteImage());
        mCertificate = view.findViewById(R.id.iv_current_certificate);

        setVisibleCertificate(false);
    }


    private void uploadImage(){
        if (PermissionUtils.checkPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyImage.openChooserWithGallery(this, "Select from gallery", 0);
        } else {
            PermissionUtils.requestPermission(getActivity(), PermissionUtils.REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void takeImage(){
        if (PermissionUtils.checkPermission(view.getContext(), Manifest.permission.CAMERA)) {
            EasyImage.openCamera(this, 0);
        } else {
            Timber.d("Requires camera permission!");
            PermissionUtils.requestPermission(getActivity(), PermissionUtils.REQUEST_CAMERA);
        }
    }

    private void deleteImage(){
        mRepository.deleteCertificate(objectId);
        setVisibleCertificate(false);
    }

    private void setVisibleCertificate(boolean visible) {
        if (visible) {
            view.findViewById(R.id.rl_current_certificate_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.fl_upload_certificate_container).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.rl_current_certificate_container).setVisibility(View.GONE);
            view.findViewById(R.id.fl_upload_certificate_container).setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {

                Glide.with(view.getContext())
                        .load(file)
                        .centerCrop()
                        .placeholder(StyleUtils.getCircularProgressDrawable(view.getContext()))
                        .into(mCertificate);
                setVisibleCertificate(true);
                mRepository.addCertificate(file, new Callback() {
                    @Override
                    public void onSuccess(String id, String message) {
                        objectId = message;
                        Toast.makeText(view.getContext(), "Additional certificate added", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String id, String message) {
                        Toast.makeText(view.getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                        setVisibleCertificate(false);
                    }
                });
            }
        });
    }



}
