package com.aurel.ecorescue.interfaces;

/**
 * Created by daniel on 6/21/17.
 */

public interface OnContractLoadedListener {
    void BasicAgreementPreloaded(boolean success);
    void AgreementLoaded(boolean success, String url, String title);
}
