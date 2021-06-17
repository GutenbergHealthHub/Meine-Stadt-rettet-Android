package com.aurel.ecorescue.parse;

import android.content.SharedPreferences;

import com.aurel.ecorescue.service.BackgroundLocationService;
import com.aurel.ecorescue.service.BackgroundLocationServiceKt;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

public class UserDao {

    public static void updateUserLocation(ParseGeoPoint geoPoint) {
        ParseUser user = ParseUser.getCurrentUser();
        if(user != null && user.isAuthenticated() && !ParseAnonymousUtils.isLinked(user)) {
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("location", geoPoint);
            installation.saveInBackground(e -> {
                if (e != null) {
                    Timber.d(e);
                } else {
                    Timber.d("LocationUpdated");
                }
            });
//            user.put("location", geoPoint);
//            user.saveInBackground(e -> {
//                if (e != null) {
//                    Timber.d(e);
//                } else {
//                    Timber.d("LocationUpdated");
//                }
//            });
        }
    }
}
