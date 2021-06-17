package com.aurel.ecorescue.x_tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.model.Emergency;
import com.aurel.ecorescue.service.PinDialogManager;
import com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl;
import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.view.dialogs.ReadyForEmergencyDialog;
import com.aurel.ecorescue.view.x_emergency.ExpiredEmergencyActivity;
import com.aurel.ecorescue.view.x_emergency.IncomingEmergencyActivity;
import com.aurel.ecorescue.view.x_emergency.AcceptEmergencyActivity;
import com.aurel.ecorescue.view.x_emergency.x_PinDialogListener;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Created by daniel on 7/20/17.
 */

public class x_EmergencyActivityManager implements x_PinDialogListener {

    private static Emergency sActiveEmergency;
    private Activity mActivity;
    private ReadyForEmergencyDialog mReadyForEmergencyDialog;
    PinDialogManager mXPinDialogManager;

    private boolean isBroadcastReceiverRegistered = false;

    public x_EmergencyActivityManager(Activity activity) {
        this.mActivity = activity;
        mReadyForEmergencyDialog = new ReadyForEmergencyDialog();
        mXPinDialogManager = new PinDialogManager(activity, this);
    }

    public static Emergency getActiveEmergency() {
        return sActiveEmergency;
    }

    private BroadcastReceiver activityEmergencyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("EcoRescue", "emergency recieved in MainActivity!");
            SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mActivity);
            ParseQuery<ParseObject> query = new ParseQuery<>("Emergency");
            query.whereEqualTo("objectId", pref.getString(x_EcoPreferences.ActiveEmergencyId, ""));
            query.getFirstInBackground((parseObject, e) -> {
                if (e == null) {
                    Log.d("EcoRescue", "active emergency found");
                    sActiveEmergency = new Emergency();
                    sActiveEmergency.fromParseObject(parseObject);
                    long duration = sActiveEmergency.createdAt.getTime() + x_EcoPreferences.EmergencyActiveDurationInMillis - new Date().getTime();
                    makeVisibleForDuration(duration);
                } else {
                    Log.d("EcoRescue", "error downloading latest emergency. " + e.getLocalizedMessage());
                    hide();
                }
            });
        }
    };

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(mActivity.getString(R.string.emergency_broadcast));
        isBroadcastReceiverRegistered = true;
        mActivity.registerReceiver(activityEmergencyReceiver, filter);
    }

    public void unregisterReceiver() {
        mActivity.unregisterReceiver(activityEmergencyReceiver);
        isBroadcastReceiverRegistered = false;
    }

    public boolean isBroadcastReceiverRegistered(){
        return isBroadcastReceiverRegistered;
    }

    private void makeVisibleForDuration(long duration) {
        Log.d("EcoRescue", "makeVisibleForDuration " + duration);
        if (duration > 500) {
            makeVisible();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(() -> hide());
                }
            }, duration);
        } else {
            hide();
        }
    }

    private void showExpiredAlert() {
//        if (!EmergencyAcceptedOnTimeUtils.isAcceptedOnTime(mActivity)) {
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mActivity);
        String emergencyId = pref.getString(x_EcoPreferences.ActiveEmergencyId, "");
        Timber.d("Expecting emergencyId: %s", emergencyId);
        if (!EmergencyAcceptedOnTimeUtils.isEmergencyAccepted(emergencyId, mActivity)) {
            Intent intent = new Intent(mActivity, ExpiredEmergencyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
        }
    }


    public void makeVisible() {
        /*if (mView == null)
            initView();
        snackbarEmergency.setVisibility(VISIBLE);
        requestDistance();*/
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mActivity);
        if (pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false)) {
            //TextView title = mView.findViewById(R.id.emergencybutton_title);
            //title.setText(R.string.emergency_active);
            // TODO.PSC: implement direct switch to acceptedEmergencyActivity
            startEmergencyActivity(sActiveEmergency.objectId);
        } else {
            Intent intent = new Intent(mActivity, IncomingEmergencyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
        }
    }


    public void hide() {
        /*if (mView == null)
            return;
        snackbarEmergency.setVisibility(GONE);*/
    }

    public void checkForActiveEmergency(boolean fromNotification) {
        Log.d("EcoRescue", "EmergencyButtonManager checkForActiveEmergency()");
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mActivity);
        if (pref.getString(x_EcoPreferences.ActiveEmergencyId, "").equals("")) {
            Log.d("EcoRescue", "EmergencyButtonManager: no active emergency found.");
            hide();
            return;
        }
        final Date activeEmergencyTime = new Date(pref.getLong(x_EcoPreferences.EmergencyActiveTime, 0));
        boolean hasActiveEmergency = pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
        boolean activeEmergencyWithinTimerange = activeEmergencyTime.after(new Date());
        Log.d("EcoRescue", "activeEmergencyWithinTimerange=" + activeEmergencyWithinTimerange + " hasEmergencyAccepted=" + hasActiveEmergency);
        if (!hasActiveEmergency && !activeEmergencyWithinTimerange) {
            Log.d("EcoRescue", "EmergencyButtonManager.checkForActiveEmergency: activeEmergencyTime before now and no acceptedEmergency.");
            if (fromNotification) {
                showExpiredAlert();
            }
            return;
        } else if(fromNotification) {
            NotificationServiceImpl.stopCurrentAlert(mActivity);
        }
        ParseQuery<ParseObject> query = new ParseQuery<>("Emergency");
        query.whereEqualTo("objectId", pref.getString(x_EcoPreferences.ActiveEmergencyId, ""));
        query.getFirstInBackground((parseObject, e) -> {
            if (e == null) {
                Log.d("EcoRescue", "EmergencyButtonManager: active emergency found");
                sActiveEmergency = new Emergency();
                sActiveEmergency.fromParseObject(parseObject);
                SharedPreferences pref1 = x_EcoPreferences.GetSharedPreferences(mActivity);
                if (pref1.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false)) {
                    makeVisible();
                } else {
                    long duration = sActiveEmergency.createdAt.getTime() + x_EcoPreferences.EmergencyActiveDurationInMillis - new Date().getTime();
                    makeVisibleForDuration(duration);
                }
            } else {
                Log.d("EcoRescue", "EmergencyButtonManager : error downloading latest emergency. " + e.getLocalizedMessage());
                hide();
            }
        });
    }

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mXPinDialogManager.askForPin();
        }
};

    private void startEmergencyActivity(String activeEmergencyReadyId) {
        Log.d("EcoRescue", "going right to the emergency details. ActiveEmergencyReadyId=" + activeEmergencyReadyId);
        Intent emergencyIntent = new Intent(mActivity, AcceptEmergencyActivity.class);
        mActivity.startActivity(emergencyIntent);
    }


    @Override
    public void onPinSuccess() {
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mActivity);
        if (pref.getBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false)
                || pref.getString(x_EcoPreferences.ActiveEmergencyReadyId, "").equals(sActiveEmergency.objectId)) {
            startEmergencyActivity(sActiveEmergency.objectId);
        } else {
            if (mReadyForEmergencyDialog.isDetached()) {
                mReadyForEmergencyDialog.dismiss();
            }
            mReadyForEmergencyDialog.activity = mActivity;
            mReadyForEmergencyDialog.emergency = sActiveEmergency;
            mReadyForEmergencyDialog.show(mActivity.getFragmentManager(), "readyForEmergency");
        }
    }
}

