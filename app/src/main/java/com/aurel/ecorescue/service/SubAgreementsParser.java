package com.aurel.ecorescue.service;

import android.util.Log;

import com.aurel.ecorescue.interfaces.OnSubAgreementsLoadedListener;
import com.aurel.ecorescue.model.SubAgreement;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by daniel on 7/3/17.
 */

public class SubAgreementsParser {


    private OnSubAgreementsLoadedListener listener;

    private ArrayList<SubAgreement> allSubAgreements;
    private ArrayList<SubAgreement> userSubAgreements;

    boolean allLoaded, userSpecificLoaded;

    public SubAgreementsParser(OnSubAgreementsLoadedListener listener) {
        this.listener = listener;
    }

    public void LoadSubagreements() {
        allLoaded = false;
        userSpecificLoaded = false;

        loadAllSubagreements();
        loadSubagreementsForUser();
    }

    private void loadAllSubagreements() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ContractSub");
//        query.whereGreaterThanOrEqualTo("validUntil", Calendar.getInstance().getTime());
        query.whereLessThanOrEqualTo("validFrom", Calendar.getInstance().getTime());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                allSubAgreements = new ArrayList<SubAgreement>();
                if (e == null) {
                    for (ParseObject p : list) {
                        SubAgreement s = new SubAgreement();
                        s.Id = p.getObjectId();
                        s.Url = p.getString("url");
                        s.ValidUntil = p.getDate("validUntil");
                        s.Title = p.getString("title");
                        s.SubTitle = p.getString("subtitle");
                        allSubAgreements.add(s);
                    }
                    allLoaded = true;
                    finishedLoading(false);
                } else {
                    allLoaded = true;
                    finishedLoading(true);
                }
            }
        });


    }

    private void loadSubagreementsForUser() {
        ParseUser user = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserContract");
//        query.whereGreaterThanOrEqualTo("validUntil", Calendar.getInstance().getTime());
//        query.whereLessThanOrEqualTo("validFrom", Calendar.getInstance().getTime());

        query.whereEqualTo("user", user);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                userSubAgreements = new ArrayList<SubAgreement>();
                if (e == null) {
                    for (ParseObject p : list) {
                        SubAgreement s = new SubAgreement();
                        s.Id = p.getParseObject("contract").getObjectId();
                        s.SignedByUser = true;
                        s.ValidUntil = p.getDate("validUntil");
                        s.Title = p.getString("title");
                        s.SubTitle = p.getString("subtitle");
                        s.SignedOn = p.getDate("signedAt");
                        userSubAgreements.add(s);
                    }
                    userSpecificLoaded = true;
                    finishedLoading(false);
                } else {
                    userSpecificLoaded = true;
                    finishedLoading(true);
                }
            }
        });
    }


    private void finishedLoading(boolean hasError) {
        Log.d("EcoRescue", "finishedLoading hasError: " + hasError);
        if (allLoaded && userSpecificLoaded) {
            //both lists are now loaded.
            for (SubAgreement userSub : userSubAgreements) {
                for (SubAgreement sub : allSubAgreements) {
                    if (userSub.Id.equals(sub.Id)) {
                        sub.SignedByUser = true;
                        sub.SignedOn = userSub.SignedOn;
                    }
                }
            }
            listener.SubAgreementLoadedForCurrentUser(allSubAgreements);
        }
    }

}
