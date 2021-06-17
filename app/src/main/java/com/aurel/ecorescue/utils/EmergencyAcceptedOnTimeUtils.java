package com.aurel.ecorescue.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.collection.ArraySet;

import com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class EmergencyAcceptedOnTimeUtils {

    private static final long ACCEPTED_EMERGENCY_RESET_TIME = 60 * 60 * 1000; // 60 Minutes in Millis
    private static final String ACCEPTED_EMERGENCY_LIST = "accepted_emergency_list";

    public static boolean isAcceptedOnTime(Context context){
        SharedPreferences prefs = x_EcoPreferences.GetSharedPreferences(context);
        long acceptedTime = prefs.getLong(x_EcoPreferences.LastEmergencyAcceptedTime, 0);
        long currentTime = (new Date()).getTime();
        // SharedPref lost its value and default returned is 0,
        // just ignore, do not show, expired emergency notification by returning true
        Timber.d("currentTime: %s", currentTime);
        Timber.d("SharedPref leak check: %s", acceptedTime);
        if (acceptedTime==0) return true;

        // If currentTime is older by 60 mins return false, as it is reset, and now emergency was accepted within 60 mins
        // If expired emergency notification is trying to be shown within 60 mins,
        // return true as there is accepted emergency in past 60 mins
        return !(currentTime - acceptedTime > ACCEPTED_EMERGENCY_RESET_TIME);
    }

    public static void addAcceptedEmergency(String id, Context context){
        if (id==null || id.isEmpty()) {
            Timber.d("Id is empty");
            return;
        }

        NotificationServiceImpl.clearNotifications(context);

        SharedPreferences prefs = x_EcoPreferences.GetSharedPreferences(context);
        Set<String> values = prefs.getStringSet(ACCEPTED_EMERGENCY_LIST, new ArraySet<>());
        Timber.d("Vals: %s", values);

        if (values==null) values = new ArraySet<>();
        values.add(id);

        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
        editor.putStringSet(ACCEPTED_EMERGENCY_LIST, values);
        editor.apply();
    }

    public static boolean isEmergencyAccepted(String id, Context context){
        if (id==null || id.isEmpty()) {
            Timber.d("Id is empty");
            // SharedPref leaked and returned empty id, just return to ignore expired emergency notification
            return true;
        }
        SharedPreferences prefs = x_EcoPreferences.GetSharedPreferences(context);
        Set<String> values = prefs.getStringSet(ACCEPTED_EMERGENCY_LIST, new ArraySet<>());
        Timber.d("Got Vals: %s", values);
        if (values==null) {
            return false;
        } else {
            return values.contains(id);
        }
    }

}
