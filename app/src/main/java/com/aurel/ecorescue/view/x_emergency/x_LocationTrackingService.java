package com.aurel.ecorescue.view.x_emergency;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Emergency;
import com.aurel.ecorescue.receivers.EmergencyReceiver;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.x_tools.x_Helper;
import com.aurel.ecorescue.view.MainActivity;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl.DEFAULT_NOTIFICATION_CHANNEL_ID;

/**
 * Created by daniel on 7/13/17.
 */

public class x_LocationTrackingService extends Service implements LocationListener {
    private static final int REQUEST_CODE = 18882;
    private static final int NOTIFICATION_ID = 7673;
    LocationManager mLocationManager;
    NotificationCompat.Builder mBuilder;
    private Emergency mEmergency;
    private Date mTrackLocationMaxTill;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("starting locationTracking");
        if (!permissionGranted(ACCESS_FINE_LOCATION) || !permissionGranted(ACCESS_COARSE_LOCATION)) {
            Timber.d("exiting location service as we dont have location permission.");
            stopSelf();
        }
        if (mBuilder == null)
            mBuilder = new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID);
        startForeground(NOTIFICATION_ID, getCompatNotification());
        if (mLocationManager == null) {
            loadData();
        }
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private boolean permissionGranted(String permission) {
        return x_Helper.permissionGranted(this, permission);
    }

    private Notification getCompatNotification() {
        Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), REQUEST_CODE, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        createNotificationChannel();
        return createNotification(pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26)
            return;
        CharSequence channelName = "Notification";
        NotificationChannel notificationChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

    }

    private Notification createNotification(PendingIntent pendingIntent) {
        int icon = selectIcon();
        Notification notification = mBuilder
                .setSmallIcon(icon)
                .setSound(null)
                .setWhen(System.currentTimeMillis())
                .setChannelId(DEFAULT_NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true) // notification only pops up the first time it is invoked, not for future updates
                .setContentTitle(getString(R.string.notification_track_title))
                .setContentText(getString(R.string.notification_track_info))
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }

    private int selectIcon() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return R.drawable.ic_ecorescue_material;
        else
            return R.mipmap.ic_launcher;
    }

    private void loadData() {
        Timber.d("locationTrackingService loadData");

        final SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(getApplicationContext());
        final String emergencyId = pref.getString(x_EcoPreferences.ActiveEmergencyId, "");
        if (emergencyId.equals("")) {
            stopSelf();
        }
        ParseQuery<ParseObject> query = new ParseQuery<>("Emergency");
        query.whereEqualTo("objectId", emergencyId);
        query.getFirstInBackground((parseObject, e) -> {
            if (e == null) {
                Timber.d("service: got mEmergency object.");
                mEmergency = new Emergency();
                mEmergency.fromParseObject(parseObject);
                mTrackLocationMaxTill = new Date(mEmergency.createdAt.getTime() + x_EcoPreferences.LocationTrackingDuration);
                if (mTrackLocationMaxTill.before(new Date())) {
                    stopSelf();
                    return;
                }
                // TODO: What happens if there is access only to COARSE_LOCATION? => Tries to use GPS which needs FINE_LOCATION => SecurityException
                if (!permissionGranted(ACCESS_FINE_LOCATION) && !permissionGranted(ACCESS_COARSE_LOCATION)) {
                    return;
                }
                mLocationManager = ((LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE));
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 0, x_LocationTrackingService.this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 0, x_LocationTrackingService.this);
                Timber.d("locationTrackingService: requested location updates.");
            }
        });


    }

    private void storeCurrentLocation(Location location){
        Timber.d("locationTrackingService onLocationChanged");
        if (mTrackLocationMaxTill.before(new Date())) {
            Timber.d("stopping location tracking.");
            stopSelf();
            return;
        }
        final SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(getApplicationContext());
        final ParseObject loc = new ParseObject("LocationTracking");
        final ParseObject emergencyState = new ParseObject("EmergencyState");
        final ParseObject emergencyObject = new ParseObject("Emergency");
        emergencyState.setObjectId(pref.getString(x_EcoPreferences.ActiveEmergencyStateId, ""));
        emergencyObject.setObjectId(pref.getString(x_EcoPreferences.ActiveEmergencyId, ""));
        Timber.d("EmergencyState: %s", emergencyState.getObjectId());
        Timber.d("EmergencyObject: %s", emergencyObject.getObjectId());
        loc.put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        loc.put("emergencyStateRelation", emergencyState);
        loc.put("emergencyRelation", emergencyObject);
        loc.put("userRelation", ParseUser.getCurrentUser());
//        ParseACL acl = new ParseACL();
//        acl.setPublicWriteAccess(true);
//        acl.setPublicReadAccess(true);
//        loc.setACL(acl);
        loc.saveInBackground();
        Timber.d("Location sent: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
        double dist = EmergencyReceiver.getDistance(location.getLatitude(), location.getLongitude(), (double)pref.getFloat(x_EcoPreferences.EmergencyLatitude, 0f), (double)pref.getFloat(x_EcoPreferences.EmergencyLongitude, 0f));
        if (dist < 10) {
            emergencyState.put("state", 4);
            emergencyState.put("arrivedAt", new Date());
            try {
                emergencyState.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Timber.d("User has arrived at the mEmergency. stopping tracking.");
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Timber.d("locationTrackingService onDestry()");
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.storeCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
