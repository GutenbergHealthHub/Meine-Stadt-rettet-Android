package com.aurel.ecorescue.model;

public enum CertificateStatus {
    NOT_SUBMITTED(0),
    IN_REVIEW(1),
    REVIEWED(2);

    int status;

    CertificateStatus(int status) {
        this.status = status;
    }

    public int value(){
        return status;
    }
}
