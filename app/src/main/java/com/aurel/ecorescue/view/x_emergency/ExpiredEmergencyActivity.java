package com.aurel.ecorescue.view.x_emergency;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpiredEmergencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_emergency);
        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(this);
        long emergencyActiveUntil = pref.getLong(x_EcoPreferences.EmergencyActiveTime, 0);
        long emergencyStartTime = emergencyActiveUntil - x_EcoPreferences.EmergencyActiveDurationInMillis;
        Date emergencyDate = new Date(emergencyStartTime);
        long minutesSinceEmergencyStart = ( new Date().getTime() - emergencyStartTime) / (1000 * 60);
        long emergencyDurationInMin = x_EcoPreferences.EmergencyActiveDurationInMillis / (1000 * 60);

        long emergencyReceiveDate = pref.getLong("onReceiveDate", 0);
        if (emergencyReceiveDate!=0){

            Date date = new Date(emergencyReceiveDate);
            long different = (new Date()).getTime() - emergencyReceiveDate;

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            ((TextView)findViewById(R.id.emergency_expired_message1)).setText(getString(R.string.emergency_expired_message1, elapsedMinutes));
            ((TextView)findViewById(R.id.emergency_expired_message2)).setText(getString(R.string.emergency_expired_message2, emergencyDurationInMin));
            ((TextView)findViewById(R.id.emergency_date)).setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(date));

        } else {
            ((TextView)findViewById(R.id.emergency_expired_message1)).setText("");
            ((TextView)findViewById(R.id.emergency_expired_message2)).setText(getString(R.string.emergency_expired_message2, emergencyDurationInMin));
            ((TextView)findViewById(R.id.emergency_date)).setText("");
        }
//        ((TextView)findViewById(R.id.emergency_expired_message1)).setText(getString(R.string.emergency_expired_message1, minutesSinceEmergencyStart));
//        ((TextView)findViewById(R.id.emergency_expired_message2)).setText(getString(R.string.emergency_expired_message2, emergencyDurationInMin));
//        ((TextView)findViewById(R.id.emergency_date)).setText(new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(emergencyDate));

        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean(x_EcoPreferences.ActiveEmergencyAccepted, false);
        editor.putString(x_EcoPreferences.ActiveEmergencyReadyId, "");
        editor.putString(x_EcoPreferences.ActiveEmergencyId, "");
        editor.putLong(x_EcoPreferences.EmergencyActiveTime, 0);
//        editor.commit();
        editor.apply();
    }

    public void finishActivity(View view) {
        finish();
    }
}
