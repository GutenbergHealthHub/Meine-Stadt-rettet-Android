package com.aurel.ecorescue.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnDistanceLoaded;
import com.aurel.ecorescue.model.Emergency;
import com.aurel.ecorescue.view.x_emergency.AcceptEmergencyActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.view.map.GetDistanceTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

/**
 * Created by daniel on 7/18/17.
 */

public class ReadyForEmergencyDialog extends DialogFragment implements OnDistanceLoaded {

    public Activity activity;
    public Emergency emergency;
    Dialog dialog;
    String timeString;
    TextView txtDistance, txtDuration;
    CountDownTimer timer;
    int duration;
    float distance;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] list = {
                getString(R.string.map_filter_defibrilator),
                getString(R.string.map_filter_hospital),
                getString(R.string.map_filter_pharmacy),
                getString(R.string.map_filter_fire),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Set the dialog title
        builder.setTitle(R.string.dialog_rdy_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setView(inflater.inflate(R.layout.x_dialog_emergency, null))
                // Set the action buttons
                .setPositiveButton(R.string.dialog_rdy_Accept, (dialog, id) -> {
                    dismiss();
                    Intent emergencyIntent = new Intent(getActivity(), AcceptEmergencyActivity.class);
                    startActivity(emergencyIntent);
                });
        dialog = builder.create();
        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        requestDistance();
        createCountdown();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    private void requestDistance() {
        final GetDistanceTask distanceTask = new GetDistanceTask(this, emergency.geoPoint);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                return;
            }
            Log.d("EcoRescue", "got location");
            String url = GetDistanceTask.GetURLForDirections(activity, location.getLatitude(), location.getLongitude(), emergency.geoPoint.getLatitude(), emergency.geoPoint.getLongitude());
            distanceTask.execute(url);
        }).addOnFailureListener(e -> Log.d("EcoRescue", "failed getting location " + e.getLocalizedMessage()));
    }

    void createCountdown() {
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(activity);
        long time = pref.getLong(x_EcoPreferences.EmergencyActiveTime, 0) - new Date().getTime();
        if (time < 0) {
            time = 1000;
        }
        timer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        timer.start();
    }

    private void updateMessage() {
        if (getDialog() != null) {
            txtDistance = (TextView) getDialog().findViewById(R.id.distance);
            txtDuration = (TextView) getDialog().findViewById(R.id.duration);
            if (txtDistance != null) {
                String dist = getString(R.string.emergency_distancedialog, distance);
                txtDistance.setText(dist);
            }
            if (txtDuration != null) {
                String dist = getString(R.string.emergency_durationdialog, duration);
                txtDuration.setText(dist);
            }
        }
    }

    @Override
    public void setDistanceAndDuration(float distance, int duration) {
        this.distance = distance;
        this.duration = duration;
        updateMessage();
    }
}
