package com.aurel.ecorescue.view.x_emergency;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.service.notificationservice.NotificationServiceImpl;
import com.aurel.ecorescue.view.ThemedActivity;
import com.aurel.ecorescue.x_tools.x_EmergencyActivityManager;

import static com.aurel.ecorescue.StarterApplication.AppTheme;
import static com.aurel.ecorescue.StarterApplication.AppThemeEco;

/**
 * Created by daniel on 7/28/17.
 */

public class EmergencyAppCompatActivity extends ThemedActivity {

    protected x_EmergencyActivityManager xEmergencyActivityManager;

    @Override
    protected void onResume() {
        super.onResume();
        xEmergencyActivityManager.registerReceiver();
        xEmergencyActivityManager.checkForActiveEmergency(false);
        NotificationServiceImpl.stopCurrentAlert(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        xEmergencyActivityManager.unregisterReceiver();
        xEmergencyActivityManager.hide();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        xEmergencyActivityManager = new x_EmergencyActivityManager(this);
    }

    private void setTheme() {
        if(AppTheme.equals(AppThemeEco))
            setTheme(R.style.AppTheme);
        else
            setTheme(R.style.AppThemeASB);
    }
}
