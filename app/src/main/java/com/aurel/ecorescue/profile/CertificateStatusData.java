package com.aurel.ecorescue.profile;

import com.aurel.ecorescue.model.CertificateStatus;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;


public class CertificateStatusData implements Serializable {

    private CertificateStatus certificateStatus;

    public CertificateStatusData createFromCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        setCertificateStatus(currentUser);

        return this;
    }


    private void setCertificateStatus(ParseUser parseUser) {
        int state = 0;
        ParseObject certificate = parseUser.getParseObject("certificateFR");
        if(certificate != null){
            try {
                certificate.fetch();
                state = certificate.getInt("state");
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            certificateStatus = CertificateStatus.NOT_SUBMITTED;
            return;
        }

        switch (state) {
            case 1:
                certificateStatus = CertificateStatus.IN_REVIEW;
//                certificateStatus.postValue(CertificateStatus.IN_REVIEW);
                break;
            case 2:
                certificateStatus = CertificateStatus.REVIEWED;
//                certificateStatus.postValue(CertificateStatus.REVIEWED);
                break;
            default:
                certificateStatus = CertificateStatus.NOT_SUBMITTED;
//                certificateStatus.postValue(CertificateStatus.NOT_SUBMITTED);
        }
    }

    public CertificateStatus getCertificateStatus() {
        return certificateStatus;
    }

}
