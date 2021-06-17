package com.aurel.ecorescue.ui.account.certificates;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.StyleUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;


public class AdditionalCertificationFragment extends Fragment {

    public AdditionalCertificationFragment() {}

    private NavController navController;
    private View view;
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certification_additional, container, false);
    }

    public void onViewCreated(@NotNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        if (getContext() != null) {
            toolbar.setNavigationIcon(StyleUtils.getHomeAsUpIndicator(getContext()));
        }

        view.findViewById(R.id.ll_capture_image).setOnClickListener(v -> takeImage());
        view.findViewById(R.id.ll_upload_certificate).setOnClickListener(v -> uploadImage());

    }

    private void uploadImage(){
        EasyImage.openChooserWithGallery(this, getString(R.string.select), 0);
    }

    private void takeImage(){
        EasyImage.openCamera(this, 0);
    }




    private void saveParseFile(File file) {

        ParseUser u = ParseUser.getCurrentUser();
        List<ParseObject> certificates = u.getList("certificates");
        Timber.d("Relation: %s", certificates);
        if (certificates==null) certificates = new ArrayList<>();

        ParseObject certificate = new ParseObject("Certificate");
        certificate.put("file", new ParseFile(file));
        certificate.put("state", 1);
        certificate.put("title", "Additional certificate");
        certificate.put("type", 2);
        certificates.add(certificate);

        u.put("certificates", certificates);

        u.saveInBackground(e -> {
            if (e != null) {
                Timber.d("Error uploading image");
                Toast.makeText(view.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            } else {
                Timber.d("Success");
                Toast.makeText(view.getContext(), R.string.additional_certificate_added, Toast.LENGTH_SHORT).show();
            }
            navController.navigateUp();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int i) {
                saveParseFile(file);
            }
        });
    }



}
