package com.aurel.ecorescue.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.Navigation;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.StarterApplication;
import com.aurel.ecorescue.receivers.AlarmReceiver;
import com.aurel.ecorescue.service.BackgroundLocationService;
import com.aurel.ecorescue.service.PinDialogManager;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.utils.Android;
import com.aurel.ecorescue.utils.BlueDialogListener;
import com.aurel.ecorescue.utils.BlueDialogUtils;
import com.aurel.ecorescue.utils.NavigationDrawer;
import com.aurel.ecorescue.view.x_emergency.IncomingEmergencyActivity;
import com.aurel.ecorescue.x_tools.x_EmergencyActivityManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Calendar;

import timber.log.Timber;

import static com.aurel.ecorescue.view.LocationPermissionHelperKt.BACKGROUND_LOCATION_REQUEST_CODE;
import static com.aurel.ecorescue.view.LocationPermissionHelperKt.FINE_LOCATION_REQUEST_CODE;


public class MainActivity extends AppCompatActivity {

    public static final String PROFILE = "PROFILE_DATA";

    private PinDialogManager mPinDialog;
    private SessionManager mStoreData;
    private x_EmergencyActivityManager mXEmergencyActivityManager;
    boolean mCameFromNotification;

    private NavigationDrawer mDrawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mDrawerHelper = new NavigationDrawer(this, Navigation.findNavController(this, R.id.fragment));

        if (getIntent().getExtras() != null && getIntent().getAction() != null) {
            mCameFromNotification = getIntent().getAction().equals(IncomingEmergencyActivity.EMERGENCY);
        }

        mStoreData = new SessionManager(this);
        mPinDialog = new PinDialogManager(this);
        mXEmergencyActivityManager = new x_EmergencyActivityManager(this);

        if (!(checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 201);
        if (!(checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 202);

        safeInstallationToParse();
    }


    public static void checkFineLocationPermission(Activity activity){
        if (!LocationPermissionHelper.isFineLocationGranted(activity)) {
            showFineLocationExplanation(activity);
        } else {
            checkBackgroundLocationPermission(activity);
        }
    }

    public static void checkBackgroundLocationPermission(Activity activity){
        if (Android.isAndroid10AndAbove() && !LocationPermissionHelper.isBackgroundLocationGranted(activity)) {
            showBackgroundLocationExplanation(activity);
        } else {
            BackgroundLocationService.startBackgroundService(activity.getApplicationContext());
            safeInstallationToParse();
        }
    }

    public static void showFineLocationExplanation(Activity activity){
        BlueDialogUtils dialog = new BlueDialogUtils(activity, new BlueDialogListener(){
            @Override public void onPositiveClick() {
                LocationPermissionHelper.requestFineLocationPermission(activity);
            }
            @Override public void onNegativeClick() {}
            @Override public void onNeutralClick() {}
        }, true);
        dialog.setContentText("App requires Location Permission in order to send you appropriate emergency calls in your area. Please choose option Allow all the time if available");
        dialog.setPositiveText("OK");
        dialog.setNegativeText("Cancel");
        dialog.show();
    }

    public static void showBackgroundLocationExplanation(Activity activity){
        BlueDialogUtils dialog = new BlueDialogUtils(activity, new BlueDialogListener(){
            @Override public void onPositiveClick() {
                LocationPermissionHelper.requestBackgroundLocationPermission(activity);
            }
            @Override public void onNegativeClick() {}
            @Override public void onNeutralClick() {}
        }, true);
        dialog.setContentText("App requires Background Location Permission in order to send you appropriate emergency calls in your area. Please choose option Allow all the time");
        dialog.setPositiveText("OK");
        dialog.setNegativeText("Cancel");
        dialog.show();
    }



    public void setUpAlarmReceiverStart(int hours, int mins){
        Timber.d("Setting alarm to: %s:%s", hours, mins);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 11, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void setUpAlarmReceiverFinish(int hours, int mins){
        Timber.d("Setting alarm to: %s:%s", hours, mins);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 12, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, mins);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }


    private static void safeInstallationToParse() {
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        if (StarterApplication.context ==null || StarterApplication.context.get()==null) return;
        parseInstallation.put("GCMSenderId", StarterApplication.context.get().getResources().getString(R.string.gcm_defaultSenderId));
        parseInstallation.put("userRelation", ParseUser.getCurrentUser());
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()!=null) {
                        Timber.w(task.getException(), "Fetching FCM registration token failed");
                        // Get new FCM registration token
                        String token = task.getResult();
                        Timber.d("Got Token: %s", token);
                        parseInstallation.setDeviceToken(token);
                        parseInstallation.setPushType("gcm");
                    }

                    parseInstallation.saveInBackground(e -> {
                        if (e==null) {
                            Timber.d(e, "Installation updated");
                        } else {
                            Timber.d(e, "Error: safeInstallationToParse");
                        }
                    });
                    ParsePush.subscribeInBackground("global");
                });
    }

    public NavigationDrawer getNavigationDrawer(){
        return mDrawerHelper;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStoreData.isLoggedIn()) {
            safeInstallationToParse();
            if (!mXEmergencyActivityManager.isBroadcastReceiverRegistered())
                mXEmergencyActivityManager.registerReceiver();
            mXEmergencyActivityManager.checkForActiveEmergency(mCameFromNotification);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStoreData.isLoggedIn()) {
            mPinDialog.close();
            if (mXEmergencyActivityManager.isBroadcastReceiverRegistered())
                mXEmergencyActivityManager.unregisterReceiver();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mStoreData.isLoggedIn()) {
            mPinDialog.close();
            if (mXEmergencyActivityManager.isBroadcastReceiverRegistered())
                mXEmergencyActivityManager.unregisterReceiver();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("onRequestPermissionsResult: %s", Arrays.asList(permissions));
        switch (requestCode) {
            case FINE_LOCATION_REQUEST_CODE: {
                if (LocationPermissionHelper.isFineLocationGranted(this) && Android.isAndroid10AndAbove()) {
                    showBackgroundLocationExplanation(this);
                } else if (LocationPermissionHelper.isFineLocationGranted(this)) {
                    BackgroundLocationService.startBackgroundService(getApplicationContext());
                    safeInstallationToParse();
                } else {
                    Toast.makeText(this, "Location permission declined!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case BACKGROUND_LOCATION_REQUEST_CODE: {
                if (LocationPermissionHelper.isLocationAuthorized(this)) {
                    BackgroundLocationService.startBackgroundService(getApplicationContext());
                    safeInstallationToParse();
                } else if (Android.isAndroid6AndAbove() && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    showBackgroundLocationExplanation(this);
                } else {
                    showLocationDeclinedManualSettingsOption();
                }
                break;
            }
        }
    }

    public void showLocationDeclinedManualSettingsOption(){
        String explainText = "Can request only one set of permissions at a time. Open settings manually";
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), explainText, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Open", v -> openSettings(this));
        snackbar.show();
    }

    private static void openSettings(Context context){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }
}
