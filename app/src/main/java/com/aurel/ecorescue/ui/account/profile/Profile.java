package com.aurel.ecorescue.ui.account.profile;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.aurel.ecorescue.R;
import com.bumptech.glide.Glide;

public class Profile {

    private String fullName;
    private String email;
    private String status;
    private String imageUrl;

    private boolean statusAgreementSigned;
    private boolean statusPersonalDataComplete;
    private int statusCertificate;  /* 0 - Not submitted, 1 - In Review, 2 - Reviewed */
    private boolean statusAccessSetUp;
    private boolean statusNotificationsOn;
    private boolean statusLocationAuthorized;

    Profile(){}

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isStatusAgreementSigned() {
        return statusAgreementSigned;
    }

    public void setStatusAgreementSigned(boolean statusAgreementSigned) {
        this.statusAgreementSigned = statusAgreementSigned;
    }

    public boolean isStatusPersonalDataComplete() {
        return statusPersonalDataComplete;
    }

    public void setStatusPersonalDataComplete(boolean statusPersonalDataComplete) {
        this.statusPersonalDataComplete = statusPersonalDataComplete;
    }

    public int getStatusCertificate() {
        return statusCertificate;
    }

    public void setStatusCertificate(int statusCertificate) {
        this.statusCertificate = statusCertificate;
    }

    public boolean isStatusAccessSetUp() {
        return statusAccessSetUp;
    }

    public void setStatusAccessSetUp(boolean statusAccessSetUp) {
        this.statusAccessSetUp = statusAccessSetUp;
    }

    public boolean isStatusNotificationsOn() {
        return statusNotificationsOn;
    }

    public void setStatusNotificationsOn(boolean statusNotificationsOn) {
        this.statusNotificationsOn = statusNotificationsOn;
    }

    public boolean isStatusLocationAuthorized() {
        return statusLocationAuthorized;
    }

    public void setStatusLocationAuthorized(boolean statusLocationAuthorized) {
        this.statusLocationAuthorized = statusLocationAuthorized;
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        Glide.with(view.getContext())
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.logo_v2)
                .into(view);
    }
}
