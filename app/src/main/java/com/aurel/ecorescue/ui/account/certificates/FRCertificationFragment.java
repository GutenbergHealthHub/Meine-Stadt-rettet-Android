package com.aurel.ecorescue.ui.account.certificates;

import android.Manifest;
import android.content.Intent;
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
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.utils.PermissionUtils;
import com.aurel.ecorescue.utils.StyleUtils;
import com.aurel.ecorescue.view.MainActivity;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;


public class FRCertificationFragment extends Fragment {

    public FRCertificationFragment() {}

    private NavController navController;
    private View view;
    private ImageView mCertificate;
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certification_fr, container, false);
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
        view.findViewById(R.id.btn_delete_certificate).setOnClickListener(v -> deleteImage());
        mCertificate = view.findViewById(R.id.iv_current_certificate);

        setUpUI();
    }

    private void setUpUI(){
        // If certificate exists then show it
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseObject certificate = parseUser.getParseObject("certificateFR");
        if (certificate==null) {
            setVisibleCertificate(false);
        } else {
            setVisibleCertificate(true);
            loadCurrentCertificate();
        }


    }

    private void uploadImage(){
        if (PermissionUtils.checkPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyImage.openChooserWithGallery(this, "Select from gallery", 0);
        } else {
            Timber.d("Requires camera permission!");
            PermissionUtils.requestPermission(getActivity(), PermissionUtils.REQUEST_READ_EXTERNAL_STORAGE);
        }

    }

    private void takeImage(){
        if (PermissionUtils.checkPermission(view.getContext(), Manifest.permission.CAMERA)) {
            EasyImage.openCamera(this, 0);
        } else {
            Timber.d("Requires camera permission!");
            PermissionUtils.requestPermission(getActivity(), PermissionUtils.REQUEST_CAMERA, Manifest.permission.CAMERA);
        }
    }

    private void deleteImage(){
        setVisibleCertificate(false);
        ParseUser u = ParseUser.getCurrentUser();
        // TODO: Delete in server
        u.put("certificateFR", JSONObject.NULL);
        u.saveInBackground();
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

    private void loadCurrentCertificate(){
        ParseUser u = ParseUser.getCurrentUser();
        ParseObject certificate = u.getParseObject("certificateFR");
        Timber.d("Relation: %s", certificate);
        if (certificate!=null) {
            File file;
            try {
                file = certificate.getParseFile("file").getFile();
                Glide.with(view.getContext())
                        .load(file)
                        .centerCrop()
                        .placeholder(StyleUtils.getCircularProgressDrawable(view.getContext()))
                        .into(mCertificate);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IllegalStateException ie) {
                ie.printStackTrace();
            }
        }
    }

    private void saveParseFile(File file) {
        ParseObject certificate = new ParseObject("Certificate");
        certificate.put("file", new ParseFile(file));
        certificate.put("state", 1);
        certificate.put("title", "First responder certificate");
        certificate.put("type", 1);

        ParseUser u = ParseUser.getCurrentUser();
        u.put("certificateFR", certificate);

        u.saveInBackground(e -> {
            if (e == null) {
                Timber.d("Error uploading image");
            } else {
                Timber.d("Success");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode: %s, resultCode: %s", requestCode, resultCode);
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
                saveParseFile(file);
            }
        });
    }



}
