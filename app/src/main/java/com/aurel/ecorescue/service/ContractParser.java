package com.aurel.ecorescue.service;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.aurel.ecorescue.interfaces.OnContractLoadedListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.List;

/**
 * Created by daniel on 6/21/17.
 */

public class ContractParser {

    private OnContractLoadedListener listener;

    public ContractParser(OnContractLoadedListener listener) {
        this.listener = listener;
    }


    public void LoadBasicAgreement(Context ctx, final boolean preload) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        final String countryCode = tm.getSimCountryIso();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ContractBasic");
        query.whereGreaterThanOrEqualTo("validUntil", Calendar.getInstance().getTime());
        query.whereLessThanOrEqualTo("validFrom", Calendar.getInstance().getTime());
        if (preload) {
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        } else {
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        }

        query.findInBackground((list, e) -> {
            if (e == null) {
                if (list != null && list.size() > 0) {//Success
                    if (preload) {
                        listener.BasicAgreementPreloaded(true);
                    } else {
                        //Load localized version if available
                        for (ParseObject p : list) {
                            if (p.getString("state").equals(countryCode)) {
                                listener.AgreementLoaded(true, p.getString("url"), p.getString("title"));
                                return;
                            }
                        }
                        //Fallback, load german version
                        for (ParseObject p : list) {
                            if (p.getString("state").equals("de")) {
                                listener.AgreementLoaded(true, p.getString("url"), p.getString("title"));
                                return;
                            }
                        }
                    }
                } else {
                    if (preload) {
                        listener.BasicAgreementPreloaded(false);
                    } else {
                        listener.AgreementLoaded(false, null, null);
                    }
                }
            } else {
                Log.d("EcoRescue", "Error loading basic agreement. " + e.getLocalizedMessage());
                if (preload) {
                    listener.BasicAgreementPreloaded(false);
                } else {
                    listener.AgreementLoaded(false, null, null);
                }
            }
        });
    }
}
