package com.aurel.ecorescue.view.x_emergency;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnDistanceLoaded;
import com.aurel.ecorescue.model.Emergency;
import com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl;
import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.view.ThemedActivity;
import com.aurel.ecorescue.view.map.GetDistanceTask;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.x_tools.x_EmergencyActivityManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class IncomingEmergencyActivity extends ThemedActivity implements OnDistanceLoaded, View.OnClickListener {

    public static final String EMERGENCY = "EMERGENCY";
    private CountDownTimer mTimer;
    private Emergency mEmergency;
    private TextView mEmergencyDistanceTextView;
    private Context context;

    private static int REQUESTCODE_CHECK_PIN_FOR_EMERGENCY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_emergency);
        findViewById(R.id.content).setOnClickListener(this);
        mEmergency = x_EmergencyActivityManager.getActiveEmergency();
        context = this;

        mEmergencyDistanceTextView = findViewById(R.id.emergencyInDistance);
        mEmergencyDistanceTextView.setVisibility(View.GONE);
        requestDistance();
        loadEmergencyState();
        mTimer = createCountdownTimer();
        mTimer.start();
    }

    @Override
    public void onClick(View v) {
        NotificationServiceImpl.stopCurrentAlert(v.getContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = createCountdownTimer();
        mTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    public void acceptEmergency(View view) {
        Intent pinIntent = new Intent(this, PinCheckActivity.class);
        startActivityForResult(pinIntent, REQUESTCODE_CHECK_PIN_FOR_EMERGENCY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==REQUESTCODE_CHECK_PIN_FOR_EMERGENCY) {
            if(resultCode == RESULT_OK) {
                Intent emergencyIntent = new Intent(this, AcceptEmergencyActivity.class);
                //emergencyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(emergencyIntent);
                mTimer.cancel();
                finish();
            }
        }
    }

    CountDownTimer createCountdownTimer() {
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(this);
        String emergencyId = pref.getString(x_EcoPreferences.ActiveEmergencyId, "");
        Timber.d("Expecting emergencyId: %s", emergencyId);
        long time = pref.getLong(x_EcoPreferences.EmergencyActiveTime, 0) - new Date().getTime();
        if (time < 0) {
            time = 1000;
        }

        TextView minutesTextView = findViewById(R.id.emergencyCountDownMinutes);
        TextView secondsTextView = findViewById(R.id.emergencyCountDownSeconds);

        IncomingEmergencyActivity activity = this;
        CountDownTimer timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false)) {
                    this.cancel();
                    this.onFinish();
                } else {
                    minutesTextView.setText(String.format(Locale.US, "%02d", ((int) millisUntilFinished / 1000) / 60));
                    secondsTextView.setText(String.format(Locale.US, "%02d", ((int) millisUntilFinished / 1000) % 60));
                }
            }

            @Override
            public void onFinish() {
                minutesTextView.setText("00");
                secondsTextView.setText("00");
                Timber.d("Timer expired! %s", !EmergencyAcceptedOnTimeUtils.isAcceptedOnTime(context));
//                if (!pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, true)) {
//                    Intent intent = new Intent(activity, ExpiredEmergencyActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }

//                if (!EmergencyAcceptedOnTimeUtils.isAcceptedOnTime(context)){
                if (!EmergencyAcceptedOnTimeUtils.isEmergencyAccepted(emergencyId, context)){
                    Intent intent = new Intent(activity, ExpiredEmergencyActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    activity.finish();
                }

            }
        };
        return timer;
    }

    @SuppressLint("MissingPermission")
    private void requestDistance() {
        final GetDistanceTask distanceTask = new GetDistanceTask(this, mEmergency.geoPoint);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                mEmergencyDistanceTextView.setVisibility(View.VISIBLE);
                mEmergencyDistanceTextView.setText(R.string.emergency_distance_no_gps);
                return;
            }
            Log.d("EcoRescue", "got location");
            String url = GetDistanceTask.GetURLForDirections(this, location.getLatitude(), location.getLongitude(), mEmergency.geoPoint.getLatitude(), mEmergency.geoPoint.getLongitude());
            distanceTask.execute(url);
        }).addOnFailureListener(e -> Log.d("EcoRescue", "failed getting location " + e.getLocalizedMessage()));
    }

    @Override
    public void setDistanceAndDuration(float distance, int duration) {
        mEmergencyDistanceTextView.setVisibility(View.VISIBLE);
        mEmergencyDistanceTextView.setText(getString(R.string.emergency_distance_in_m, (int)(distance*1000)));
    }

    private void loadEmergencyState() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("EmergencyState");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("userRelation", ParseUser.getCurrentUser());
        query.whereEqualTo("emergencyRelation", mEmergency.parseEmergency);
        query.getFirstInBackground((emergencyStateObject, e) -> {
            if (e == null) {
                int state = emergencyStateObject.getInt("state");
                Log.d("EcoRescue", "got emergencystate. state =" + state);
                if (state == 1) {// received
                    setEmergencyStateReady(emergencyStateObject);
                }
            } else {
                Log.d("EcoRescue", "error getting emergencyState " + e.getLocalizedMessage());
            }
        });
    }

    private void setEmergencyStateReady(ParseObject emergencyStateObject) {
        if (emergencyStateObject.getInt("state") < 2) {
            emergencyStateObject.put("state", 2);
            emergencyStateObject.put("readyAt", new Date());
            emergencyStateObject.saveEventually();
            SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
            editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, mEmergency.objectId);
            editor.apply();
        }
    }

}
