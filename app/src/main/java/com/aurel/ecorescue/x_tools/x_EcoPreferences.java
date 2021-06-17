package com.aurel.ecorescue.x_tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by daniel on 7/13/17.
 */

public class x_EcoPreferences {
    public static int LocationTrackingDuration = 60 * 60 * 1000;
    public static long EmergencyActiveDurationInMillis = 3 * 60 * 1000;
    public static String EmergencyActiveTime = "emergencyActiveTime";
    public static String ActiveEmergencyAccepted = "activeEmergencyAccepted";
    public static String ActiveEmergencyId = "activeEmergency";
    public static String ActiveEmergencyStateId = "activeEmergencyState";
    public static String receivedEmergencyId = "receivedEmergency";
    public static String AppComesFromBackground = "AppComesFromBackground";
    public static String ActiveEmergencyReadyId = "ActiveEmergencyReadyId";
    public static String EmergencyLatitude = "";
    public static String EmergencyLongitude = "";
    public static String LastEmergencyAcceptedTime = "lastEmergencyAcceptedTime";

    public static SharedPreferences.Editor GetEditor(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.edit();
    }

    public static SharedPreferences GetSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
