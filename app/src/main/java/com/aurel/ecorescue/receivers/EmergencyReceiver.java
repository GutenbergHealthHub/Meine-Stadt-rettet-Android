package com.aurel.ecorescue.receivers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.adapter.Notification;
import com.aurel.ecorescue.parse.UserDao;
import com.aurel.ecorescue.service.notificationservice.NotificationService;
import com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl;
import com.aurel.ecorescue.view.x_emergency.IncomingEmergencyActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.x_emergency.x_LocationTrackingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

import static com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl.DELETE_ACTION;

/**
 * Created by aurel on 26-Sep-16.
 */

public class EmergencyReceiver extends ParsePushBroadcastReceiver {

    private NotificationService notificationService;
    private String sound;
    private String emergencyId;
    private ParseGeoPoint userGeoPoint;
    private ParseGeoPoint emergencyGeoPoint;
    private Context context;
    private ParseObject emergency, emergencyState;
    private List alertingRegions;
    private JSONObject config;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Message: %s", intent);
        super.onReceive(context, intent);
        notificationService = new NotificationServiceImpl(context);

        if (intent.getAction() != null && (isAction(intent, DELETE_ACTION) || isAction(intent, ACTION_PUSH_OPEN))) {
            NotificationServiceImpl.stopCurrentAlert(context);
//            cancelExpiredNotification();
        }
    }

    public boolean isAction(Intent intent, String action){
        return intent.getAction().equals(action);
    }

    @Override
    public void onPushReceive(final Context context, Intent intent) {
        Timber.d("onPushReceive");
        this.context = context;
        Bundle extras = intent.getExtras();
        String jsonData = extras.getString("com.parse.Data");
        JSONObject jsonObject;

        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(context);
        Timber.d("onPushReceive: %s", jsonData);
        try {
            jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("cancel") && jsonObject.getBoolean("cancel")) {
                cancelEmergency(jsonObject);
            } else if(jsonObject.has("fetchLocation")) {
                saveCurrentLocation();
            } else if (jsonObject.has("emergencyId") && jsonObject.has("emergencyStateId")) {
                Log.d("EcoRescue", "This is a new emergency");
                String newEmergencyId = jsonObject.getString("emergencyId");
                if (!pref.getString(x_EcoPreferences.receivedEmergencyId, "").equals(newEmergencyId)) {
                    if (!canAcceptEmergency()) {
                        Log.d("EcoRescue", "Other emergency pending... aborting processing of new emergency");
                        return;
                    }

                    SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
                    editor.putString(x_EcoPreferences.receivedEmergencyId, newEmergencyId);
                    editor.putLong("onReceiveDate", (new Date()).getTime());
                    editor.putBoolean("notificationAlreadyAccepted", false);
                    editor.apply();

                    emergencyId = newEmergencyId;
                    String emergencyStateId = jsonObject.getString("emergencyStateId");
                    this.config = jsonObject.getJSONObject("config");
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    this.sound = currentUser.get("sound").toString();
                    if (sound==null || sound.isEmpty()) sound = "horn";
                    if (!emergencyId.isEmpty() && !emergencyStateId.isEmpty()) {
                        ParseQuery query = ParseQuery.getQuery("EmergencyState");
                        query.whereEqualTo("objectId", emergencyStateId);
                        query.getFirstInBackground((GetCallback<ParseObject>) (object, e) -> {
                            if(e == null) {
                                emergencyState = object;
                                emergencyState.put("state", 1);
                                emergencyState.saveEventually();
                                getAlertingRegionsAsync(() -> getUserLocationAsync(() -> {
                                    if(userGeoPoint != null) {
                                        getEmergencyDataAsync(emergencyId, this::showEmergencyIfUserWithinDistance);
                                    }
                                }));
                            } else {
                                Log.d("EcoRescue", "Error in loading emergencyState. " + e.getLocalizedMessage());
                            }
                        });
                    }
                } else {
                    Log.d("EcoRescue", "emergency already received");
                }
            } else {
                //PSC: let all other notifications be handled by the standard parse-push-receiver
                super.onPushReceive(context, intent);
            }
        } catch (JSONException e) {
            Log.e("EcoRescue", "Error in onPushReceive. " + e.getLocalizedMessage());
        }
    }

    private void showEmergencyIfUserWithinDistance() {
        int maxDistanceInMeters = AlertingRegionUtils.determineMaxDistance(config, alertingRegions, userGeoPoint);
        boolean isUserWithinMaxDistance = AlertingRegionUtils.isUserWithinMaxDistance(userGeoPoint, emergencyGeoPoint, maxDistanceInMeters);
        if (isUserWithinMaxDistance) {
            Log.d("EcoRescue", "User in range. showing notification.");
            showEmergencyNotification();
        } else {
            Log.d("EcoRescue", "User NOT in range for emergency. not displaying a notification.");
        }
    }

    private void saveCurrentLocation() {
        //this.getUserLocation();
        if(userGeoPoint != null) {
            UserDao.updateUserLocation(userGeoPoint);
        }
    }


    private void showEmergencyNotification() {
        String activeDescription = context.getString(R.string.notification_emergency_info);
        final Intent mainActivityIntent = new Intent(context, MainActivity.class);
        showNotificationMessage(activeDescription, mainActivityIntent, userGeoPoint, emergency);
    }

    private boolean canAcceptEmergency() {
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(this.context);
        Date lastRecievedEmergencyActiveTime = new Date(pref.getLong(x_EcoPreferences.EmergencyActiveTime, 0));

        boolean acceptedEmergencyExists = pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
        boolean pendingEmergencyExists = lastRecievedEmergencyActiveTime.after(new Date());
        String sessiontoken = ParseUser.getCurrentUser().getSessionToken();
        boolean isLoggedIn = sessiontoken != null;
        return !acceptedEmergencyExists && !pendingEmergencyExists && isLoggedIn;
    }

    private void getEmergencyDataAsync(final String emergencyId, Runnable callback) {
        ParseQuery query = ParseQuery.getQuery("Emergency");
        query.whereEqualTo("objectId", emergencyId);
        query.getFirstInBackground((GetCallback<ParseObject>) (object, e) -> {
            emergency = object;
            emergencyGeoPoint = object.getParseGeoPoint("locationPoint");
            runCallback(callback);
        });
    }

    private void getAlertingRegionsAsync(Runnable callback) {
        ParseQuery query = ParseQuery.getQuery("AlertingRegion");
        query.whereEqualTo("activated", false); // change to true;
        query.findInBackground((FindCallback<ParseObject>) (objects, e) -> {
            if (e == null) {
                alertingRegions = objects;
                runCallback(callback);
            }
        });
    }

    private void showNotificationMessage(final String message,
                                         final Intent intent,
                                         final ParseGeoPoint location,
                                         final ParseObject emergency) {
        Log.d("EcoRescue", "showNotificationMessage");
        try {
            ParseInstallation.getCurrentInstallation().save();
            ParseObject controlCenter = emergency.getParseObject("controlCenterRelation");
            try {
                controlCenter.fetch();
                Notification notification = new Notification();
                notification.setObjectId(emergencyState.getObjectId());
                notification.setStartDate(emergencyState.getCreatedAt());
                notification.setLatitude(location.getLatitude());
                notification.setLongitude(location.getLongitude());
                notification.setContext(message);
                notification.setState(1);
                notification.setControlCenterId(controlCenter.getObjectId());
                notification.setZip(emergency.has("zip") ? emergency.getString("zip") : "");
                notification.setKeyword(emergency.has("keyword") ? emergency.getString("keyword") : "");
                notification.setObjectName(emergency.has("objectName") ? emergency.getString("objectName") : "");
                notification.setCity(emergency.has("city") ? emergency.getString("city") : "");
                notification.setStreetNumber(emergency.has("streetNumber") ? emergency.getString("streetNumber") : "");
                notification.setStreetName(emergency.getString("streetName"));
                notification.setPatientName(emergency.has("patientName") ? emergency.getString("patientName") : "");
                notification.setCountry(emergency.has("country") ? emergency.getString("country") : "");
                notification.setEmergencyNumber(emergency.getInt("emergencyNumberDC"));
                notification.setIndicatorName(emergency.has("indicatorName") ? emergency.getString("indicatorName") : "");
                intent.setAction(IncomingEmergencyActivity.EMERGENCY);
                intent.putExtra(IncomingEmergencyActivity.EMERGENCY, notification);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                String title = context.getResources().getString(R.string.new_emergency);
                Log.d("EcoRescue", "Received emergency state. object id: " + emergencyState.getObjectId());
                SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
                editor.putLong(x_EcoPreferences.EmergencyActiveTime, emergency.getCreatedAt().getTime() + x_EcoPreferences.EmergencyActiveDurationInMillis);
                editor.putString(x_EcoPreferences.ActiveEmergencyId, emergency.getObjectId());
                editor.putString(x_EcoPreferences.ActiveEmergencyStateId, emergencyState.getObjectId());
                editor.putString("emergencyNumberDC", emergency.getString("emergencyNumberDC"));
//                editor.commit();
                editor.apply();
                if (sound==null || sound.isEmpty()) sound = "horn";
                notificationService.showEmergencyNotification(title, message, intent, sound);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void cancelEmergency(JSONObject emergencyJson) throws JSONException {
        Log.d("EcoRescue", "cancelling emergency");
        final String emergencyId = emergencyJson.getString("emergencyId");
        final String emergencyStateId = emergencyJson.getString("emergencyStateId");

        SharedPreferences localStorage = x_EcoPreferences.GetSharedPreferences(context);
        if (noActiveEmergency(localStorage) ||
                differentEmergencyActive(emergencyId, localStorage, x_EcoPreferences.ActiveEmergencyId)) {
            Log.d("EcoRescue", "Cancelled emergency is not active. Nothing to do.");
        } else {
            ParseQuery<ParseObject> emergencyStateQuery = queryEmergencyState(emergencyStateId);
            Log.d("EcoRescue", "loadEmergencyState for emergency " + emergencyId);
            emergencyStateQuery.getFirstInBackground((parseEmergencyState, e) -> {
                if (e == null) {
                    int state = parseEmergencyState.getInt("state");
                    if (state == 3 || state == 4) { // TODO: rename condition with understandable wording
                        parseEmergencyState.put("state", 7);
                        parseEmergencyState.put("cancelledAt", new Date());
                        parseEmergencyState.saveInBackground(e1 -> {
                            if (e1 == null) {
                                notificationService.showEmergencyCancelledNotification(emergencyId);
                            } else {
                                Log.d("EcoRescue", "Error saving cancelled emergencyState.");
                            }
                        });
                        //Stop location tracking
                        Intent startIntent = new Intent(context, x_LocationTrackingService.class);
                        context.stopService(startIntent);
                        //Cleanup
                        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(context);
                        editor.putBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
                        editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, "");
                        editor.putString(x_EcoPreferences.ActiveEmergencyId, "");
                        editor.putLong(x_EcoPreferences.EmergencyActiveTime, 0);
//                        editor.commit();
                        editor.apply();
                    } else {
                        Log.d("EcoRescue", "EmergencyState indicates that the emergency is not yet accepted or already finished. aborting cancel.");
                    }
                } else {
                    Log.d("EcoRescue", "error getting emergencyState " + e.getLocalizedMessage());
                }
            });
        }
    }

    private ParseQuery<ParseObject> queryEmergencyState(final String emergencyStateId) {
        ParseQuery<ParseObject> emergencyStateQuery = new ParseQuery<>("EmergencyState");
        emergencyStateQuery.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        emergencyStateQuery.whereEqualTo("objectId", emergencyStateId);
        emergencyStateQuery.whereEqualTo("userRelation", ParseUser.getCurrentUser());
        emergencyStateQuery.whereEqualTo("installationRelation", ParseInstallation.getCurrentInstallation());
        return emergencyStateQuery;
    }

    private boolean differentEmergencyActive(final String emergencyId, final SharedPreferences localStorage, final String activeEmergencyId) {
        return !localStorage.getString(activeEmergencyId, "").equals(emergencyId);
    }

    private boolean noActiveEmergency(final SharedPreferences pref) {
        return !pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        Location from = new Location("FROM");
        from.setLatitude(lat1);
        from.setLongitude(lon1);

        Location to = new Location("TO");
        to.setLatitude(lat2);
        to.setLongitude(lon2);

        double dist = from.distanceTo(to);
        return dist;
    }

    @Nullable
    public void getUserLocationAsync(Runnable callback) {
        final FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLocationAvailability().addOnSuccessListener(locationAvailability -> {
                if (locationAvailability.isLocationAvailable()) {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            Log.d("EcoRescue", "got location: " + location.getLatitude() + "  " + location.getLongitude());
                                userGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                            runCallback(callback);
                        });
                    }
            });
        }else {
            notificationService.showGpsDisabledNotification();
        }
    }

    private void runCallback(Runnable runnable) {
        if(runnable != null) {
            runnable.run();
        }
    }
}