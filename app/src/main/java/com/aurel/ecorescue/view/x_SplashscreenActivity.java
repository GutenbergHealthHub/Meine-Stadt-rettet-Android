package com.aurel.ecorescue.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.aurel.ecorescue.BuildConfig;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;

import timber.log.Timber;

public class x_SplashscreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("AppId: %s", BuildConfig.APPLICATION_ID);
        Timber.d("PackageName: %s", getApplication().getPackageName());


        SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(this);
        if (pref.getBoolean("not_first_time", false)) {
            Intent intent = new Intent(x_SplashscreenActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(x_SplashscreenActivity.this, IntroActivity.class);
            startActivity(intent);
        }

        finish();
    }



}
