package com.aurel.ecorescue;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.multidex.MultiDex;

import com.aurel.ecorescue.parse.ControlCenter;
import com.aurel.ecorescue.utils.AppExecutors;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;

import java.lang.ref.WeakReference;

import timber.log.Timber;


public class StarterApplication extends Application {

    public final static String AppThemeEco = "ecorescue";
    public static String AppTheme = AppThemeEco;

    public static AppExecutors appExecutors = new AppExecutors();

    public static WeakReference<Context> context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        context = new WeakReference<>(this);
        //Register parse classes
        ParseObject.registerSubclass(ControlCenter.class);
        // Add your initialization code here
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        // set up parse
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.PARSE_APP_ID)
                .clientKey(BuildConfig.PARSE_CLIENT_KEY)
                .server(BuildConfig.PARSE_SERVER_URL)
                .build()
        );


        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
            editor.putBoolean(x_EcoPreferences.AppComesFromBackground, true);
            editor.apply();
        }
    }
}
