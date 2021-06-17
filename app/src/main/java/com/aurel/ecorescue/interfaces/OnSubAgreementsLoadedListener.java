package com.aurel.ecorescue.interfaces;

import com.aurel.ecorescue.model.SubAgreement;
import com.aurel.ecorescue.service.SubAgreementsParser;

import java.util.ArrayList;

/**
 * Created by daniel on 7/3/17.
 */

public interface OnSubAgreementsLoadedListener {
    void SubAgreementLoadedForCurrentUser(ArrayList<SubAgreement> list);
}
