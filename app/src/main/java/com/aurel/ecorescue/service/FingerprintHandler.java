package com.aurel.ecorescue.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.core.app.ActivityCompat;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.x_emergency.PinCheckActivity;

/**
 * Created by aurel on 01-Nov-16.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal mCancellationSignal;
    private Context appContext;
    private PinCheckActivity p;

    public FingerprintHandler(Context context, PinCheckActivity instance) {
        appContext = context;
        p = instance;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        mCancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(appContext, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, mCancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
//        Toast.makeText(appContext, "Authentication error\n" + errString, Toast.LENGTH_SHORT).show();
//        p.fingerprintAuthentication();
        p.failure(appContext.getResources().getString(R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
//        Toast.makeText(appContext, "Authentication help\n" + helpString, Toast.LENGTH_SHORT).show();
        p.fingerprintAuthentication();
    }

    @Override
    public void onAuthenticationFailed() {
      //  Toast.makeText(appContext, "Authentication failed.", Toast.LENGTH_SHORT).show();
        p.fingerprintAuthentication();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
//        Toast.makeText(appContext, "Authentication succeeded.", Toast.LENGTH_SHORT).show();
        p.success();
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }


}