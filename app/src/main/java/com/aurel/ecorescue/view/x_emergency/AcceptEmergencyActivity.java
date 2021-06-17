package com.aurel.ecorescue.view.x_emergency;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Emergency;
import com.aurel.ecorescue.ui.emergency.IdentificationCardActivity;
import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.view.ThemedActivity;
import com.aurel.ecorescue.view.dialogs.GPSDisabledDialog;
import com.aurel.ecorescue.view.map.GetDirectionsTask;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class AcceptEmergencyActivity extends ThemedActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    public static String EMPTY = "EMPTY";

    private Emergency emergency;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private ParseObject emergencyStateObject;
    private int state;
    private GetDirectionsTask directionsTask;
    private String emergencyId = EMPTY;

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_emergency);
        cancelNotification(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_emergency);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_call) {
                callControlcenter(null);
            }
            return false;
        });

//        EmergencyAcceptedOnTimeUtils.setLastEmergencyAcceptedTime(this);
        SharedPreferences prefs = x_EcoPreferences.GetSharedPreferences(this);
        EmergencyAcceptedOnTimeUtils.addAcceptedEmergency(prefs.getString(x_EcoPreferences.ActiveEmergencyId, ""), this);

        findViewById(R.id.ibtn_identification_card).setOnClickListener(v -> {
            startActivity(new Intent(this, IdentificationCardActivity.class));
        });

        GPSDisabledDialog.checkGPS(this);
        emergencyId = getIntent().hasExtra("emergencyId") ? getIntent().getStringExtra("emergencyId") : EMPTY;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(getLayoutInflater().inflate(R.layout.alert_emergency_finished, null));
        mAlertDialog = dialogBuilder.create();

        loadEmergency();
    }

    public void cancelNotification(Context ctx) {
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean("notificationAlreadyAccepted", true);
//        editor.commit();
        editor.apply();
//        nMgr.cancel(notifyId);
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean("notificationAlreadyAccepted", true);
//        editor.commit();
        editor.apply();
    }

    @Override
    public void onStop(){
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean("notificationAlreadyAccepted", false);
//        editor.commit();
        editor.apply();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean("notificationAlreadyAccepted", false);
//        editor.commit();
        editor.apply();
        super.onDestroy();
        Timber.d("Destroyed");
    }
    private void loadEmergency() {
        Log.d("EcoRescue", "loadEmergency with emergencyId " + emergencyId);
        ParseQuery<ParseObject> query = new ParseQuery<>("Emergency");
        if (!emergencyId.equals(EMPTY)) {//We are loading an already finished emergency
            query.whereEqualTo("objectId", emergencyId);
        } else { //We are opening an active emergency
            SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(this);
            query.whereEqualTo("objectId", pref.getString(x_EcoPreferences.ActiveEmergencyId, ""));
        }
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.getFirstInBackground((parseEmergency, e) -> {
            if (e == null) {
                Emergency emergency = new Emergency();
                emergency.fromParseObject(parseEmergency);
                setEmergency(emergency);
            } else {
                Log.d("EcoRescue", "EmergencyTabActivity: error getting emergency. " + e.getLocalizedMessage());
            }
        });
    }


    private void loadEmergencyState() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("EmergencyState");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("userRelation", ParseUser.getCurrentUser());
        query.whereEqualTo("emergencyRelation", emergency.parseEmergency);
        Log.d("EcoRescue", "loadEmergencyState for emergency " + emergency.objectId);
        query.getFirstInBackground((parseObject, e) -> {
            if (e == null) {
                emergencyStateObject = parseObject;
                state = emergencyStateObject.getInt("state");
                Log.d("EcoRescue", "got emergencystate. state =" + state);
                if (state == 2) {// ready
                    setEmergencyStateAccepted();
                }
                if (state == 3) { //accepted
                    startLocationTracking();
                }
                if (state == 7) {//cancelled by the control center
                    SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
                    editor.putBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
                    editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, "");
                    editor.putString(x_EcoPreferences.ActiveEmergencyId, "");
                    editor.putLong(x_EcoPreferences.EmergencyActiveTime, 0);
//                    editor.commit();
                    editor.apply();
                    Timber.d("Destroyed: loadEmergencyState");
                    finish();
                    Toast.makeText(getApplicationContext(), getString(R.string.emergency_cancelled_info), Toast.LENGTH_SHORT);
                }
            } else {
                Log.d("EcoRescue", "error getting emergencyState " + e.getLocalizedMessage());
            }
        });
    }

    public void setEmergency(Emergency emergency) {
        this.emergency = emergency;
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        fillInfos();
        loadEmergencyState();
    }

    private void placeMarker() {
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng ll = new LatLng(emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude());
        markerOptions.position(ll);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.mapmarker_alarm_v2)));
        mMap.addMarker(markerOptions);
    }

    private void zoomMap(double lat, double lng) {
        //Calculate the markers to get their position
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        LatLng self = new LatLng(lat, lng);
        LatLng detail = new LatLng(emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude());
        b.include(self);
        b.include(detail);
        LatLngBounds bounds = b.build();
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, pxPadding);
        mMap.moveCamera(cu);
    }

    private void fillInfos() {
        ((TextView) findViewById(R.id.label_description)).setText(emergency.information);
        ((TextView) findViewById(R.id.label_address)).setText(emergency.address);
        ((TextView) findViewById(R.id.label_object)).setText(emergency.objectName);
        ((TextView) findViewById(R.id.label_patient)).setText(emergency.patientName);
        ((TextView) findViewById(R.id.label_patient)).setText(emergency.patientName);
        ((TextView) findViewById(R.id.label_reporter)).setText(emergency.indicator);
        ((TextView) findViewById(R.id.label_geolocation)).setText(emergency.geoPoint.getLatitude() + ", " + emergency.geoPoint.getLongitude());
        ((TextView) findViewById(R.id.label_emergency_number)).setText(emergency.emergencyNumberDC);
        ((TextView) findViewById(R.id.label_keyword)).setText(emergency.keyword);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("EcoRescue", "Error. Need permission to continue");

        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMarkerClickListener(this);
        buildGoogleApiClient();
        placeMarker();

        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(10 * 1000);
        mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        zoomMap(location.getLatitude(), location.getLongitude());
        findDirections(location.getLatitude(), location.getLongitude());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    public void callControlcenter(View view) {
        if (isPermissionGranted()) {
//            Intent callIntent = new Intent(Intent.ACTION_CALL);
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + emergency.controlCenterNumber));
            startActivity(callIntent);
        }
    }

    private void stopLocationTracking() {
        Log.d("EcoRescue", "stopLocationTracking");
        Intent startIntent = new Intent(this, x_LocationTrackingService.class);
        this.stopService(startIntent);
    }

    private void startLocationTracking() {
        Log.d("EcoRescue", "getInstance");
        Intent startIntent = new Intent(this, x_LocationTrackingService.class);
        this.startService(startIntent);
    }

    public void showFinishDialog(View view) {
        mAlertDialog.show();

//        Timber.d("Report Required: %s : %s", emergency.controlCenterObjectId, emergency.reportRequired);
//        emergency.reportRequired = false;
//        if (emergency.reportRequired) {
//            mAlertDialog.show();
//        } else {
//            cleanUp();
//            finish();
//        }
    }

    public void closeAlert(View view) {
        mAlertDialog.hide();
    }

    public void emergencyFinished(View view) {
        if (emergencyStateObject==null) return;
        emergencyStateObject.put("endedAt", new Date());
        emergencyStateObject.put("state", 6);
        emergencyStateObject.saveEventually();
        state = 6;
        //Cleanup

        cleanUp();


        Intent reportIntent = new Intent(this, EmergencyFinishedActivity.class);
        reportIntent.putExtra("emergencyStateId", emergencyStateObject.getObjectId());
        reportIntent.putExtra("emergencyStateStatus", emergencyStateObject.getInt("state"));

        Timber.d("Destroyed: loadEmergencyState");
        if (emergency.reportRequired) {
            this.finish();
            startActivity(reportIntent);
        } else {
            finish();
        }

    }

    private void setEmergencyStateAccepted() {
        if (emergencyStateObject.getInt("state") < 3) {
            emergencyStateObject.put("state", 3);
            emergencyStateObject.put("acceptedAt", new Date());
            emergencyStateObject.saveEventually();
            state = 3;
            SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
            editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, emergency.objectId);
            editor.apply();
        }
    }

    private void cleanUp(){
        //clean up
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
        editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, "");
        editor.putString(x_EcoPreferences.ActiveEmergencyId, "");
        editor.putString(x_EcoPreferences.ActiveEmergencyStateId, "");
        editor.putLong(x_EcoPreferences.EmergencyActiveTime, 0);
//        editor.commit();
        editor.apply();
        stopLocationTracking();
        mAlertDialog.hide();
        mAlertDialog.dismiss();
    }

    public void emergencyCanceled(View view) {
        emergencyStateObject.put("cancelledAt", new Date());
        emergencyStateObject.put("state", 5);
        emergencyStateObject.saveEventually();
        state = 5;

        cleanUp();

        Intent reportIntent = new Intent(this, EmergencyFinishedActivity.class);

        reportIntent.putExtra("emergencyStateId", emergencyStateObject.getObjectId());
        reportIntent.putExtra("emergencyStateStatus", emergencyStateObject.getInt("state"));
        Timber.d("Destroyed: emergencyCanceled");

        if (emergency.reportRequired) {
            this.finish();
            startActivity(reportIntent);
        } else {
            finish();
        }
    }

    private void findDirections(double lat, double lng) {
        if (state >= 5) {
            return;
        }

        if (directionsTask != null) {
            directionsTask.clear();
            directionsTask.cancel(true);
        }
        directionsTask = new GetDirectionsTask(mMap, findViewById(R.id.label_distance_time), findViewById(R.id.label_distance_meters));
        directionsTask.execute(GetDirectionsTask.GetURLForDirections(this, lat, lng, emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude()));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    public void startEmergencyOperation(View view) {
        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean(x_EcoPreferences.ActiveEmergencyAccepted, true);
        editor.putString(x_EcoPreferences.ActiveEmergencyStateId, emergencyStateObject.getObjectId());
        editor.putFloat(x_EcoPreferences.EmergencyLatitude, (float) emergency.geoPoint.getLatitude());
        editor.putFloat(x_EcoPreferences.EmergencyLongitude, (float) emergency.geoPoint.getLongitude());
//        editor.commit();
        editor.apply();
        //open map
        String uriMap;
        if (emergency.patientName == null)
            emergency.patientName = "Patient";
        uriMap = String.format(Locale.ENGLISH, "geo:%f,%f?z=17&q=%f,%f(%s)", emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude(), emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude(), emergency.patientName);

        Intent intentMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uriMap));
        startActivity(intentMap);
        startLocationTracking();
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callControlcenter(null);
                }
                return;
            }
        }
    }
}
