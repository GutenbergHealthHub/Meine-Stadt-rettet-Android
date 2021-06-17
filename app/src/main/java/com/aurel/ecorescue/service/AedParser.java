package com.aurel.ecorescue.service;

import android.location.Location;
import android.util.Log;

import com.aurel.ecorescue.interfaces.OnDefibrilatorLoadedListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by daniel on 7/6/17.
 */

public class AedParser {

    OnDefibrilatorLoadedListener listener;

    public AedParser(OnDefibrilatorLoadedListener listener) {
        this.listener = listener;
    }

    public void loadAeds(Location location, int kmRadius) {
        if (location == null) {
            return;
        }
        Log.d("EcoRescue", "loading Defibrilators");
        ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Defibrillator");
        query.whereWithinKilometers("location", point, kmRadius);
        query.whereEqualTo("activated", true);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    listener.defibrilatorLoaded(objects);
                } else {
                    Log.d("EcoRescue", "Error downloading defibrilators: " + e.getLocalizedMessage());
                }
            }
        });


    }

}
