package com.aurel.ecorescue.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.aurel.ecorescue.adapter.Notification;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aurel on 28-Sep-16.
 */

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context mContext;

    // Sharedpref protocol fail
    private static final String PROTOCOL= "PROTOCOL";

    // Sharedpref file name
    private static final String PREF_NAME = "com.aurel.ecorescue";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    private static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    private static final String KEY_EMAIL = "email";

    // Constructor
    public SessionManager(Context context) {
        mContext = context;
        pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String name, String email) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // commit changes
        editor.commit();
    }

    /**
     * Clear session details
     */
    public void logoutUser(Activity activity) {
        ActivityCompat.finishAffinity(activity);
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(mContext, MainActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Add new Flag to getInstance new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        mContext.startActivity(i);
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }


    public HashMap<String, Notification> getData() {
        Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
        editor = pref.edit();
        Type type = new TypeToken<ArrayList<Notification>>() {
        }.getType();
        String storedHashMapString = pref.getString("DATA", "ERROR");
        HashMap<String, Notification> listDayItems = new HashMap<>();
        try {
            ArrayList<Notification> data = gson.fromJson(storedHashMapString, type);
            for (Notification n : data) {
                n.setLeftTime(120000L - (Calendar.getInstance().getTimeInMillis() - n.getStartTime()));
                if (n.getLeftTime() > 0) {
                    listDayItems.put(n.getObjectId(), n);
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
        }
        return listDayItems;
    }

    public void saveProtocol(String name, JSONObject report) {
        String data = report.toString();
        editor.putString(PROTOCOL+name, data);
        editor.commit();
    }

    public JSONObject getProtocol(String name) {
        try {
            String value = pref.getString(PROTOCOL + name, "");
            if(value == null || value.isEmpty()) {
                return null;
            } else {
                return new JSONObject(value);
            }
        } catch (JSONException e) {
            return null;
        }
    }
}