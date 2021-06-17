package com.aurel.ecorescue.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import androidx.core.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.security.SecurityEditPin;
import com.aurel.ecorescue.view.x_emergency.x_PinDialogListener;
import com.parse.ParseUser;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by aurel on 14-Sep-16.
 */
public class PinDialogManager implements View.OnClickListener, DialogInterface.OnKeyListener {

    private static final String KEY_NAME = "example_key";

    private TextView pin, pinHeader, wrongPin;
    private Button one, two, three, four, five, six, seven, eight, nine, zero, delete, logOut;
    private String mPinText;
    private String mHiddenPin;
    private Dialog mDialog;
    private Activity mActivity;
    private SessionManager mSessionManager;
    private Vibrator mVibrator;
    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private Cipher mCipher;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintHandler mHelper;
    private x_PinDialogListener mListener;
    private boolean mFingerprintDisabled;

    public PinDialogManager(Activity activity) {
        mFingerprintDisabled = false;
        mActivity = activity;
        mSessionManager = new SessionManager(activity);
    }

    public PinDialogManager(Activity activity, x_PinDialogListener listener) {
        mFingerprintDisabled = false;
        mActivity = activity;
        mSessionManager = new SessionManager(activity);
        this.mListener = listener;
    }

    public void askForPin() {
        mPinText = "";
        mHiddenPin = "";
        showPinDialog();
        if (isFingerprintDesired()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                fingerprintAuthentication();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void fingerprintAuthentication() {
        mKeyguardManager = (KeyguardManager) mActivity.getSystemService(KEYGUARD_SERVICE);
        mFingerprintManager = (FingerprintManager) mActivity.getSystemService(FINGERPRINT_SERVICE);

        if (!mKeyguardManager.isKeyguardSecure()) {
            Toast.makeText(mActivity, "Lock screen security not enabled in Settings", Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(mActivity, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mActivity, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
            return;
        }

        if(generateKey()) {
            if (cipherInit()) {

                mCryptoObject = new FingerprintManager.CryptoObject(mCipher);
                // removed because of refactoring, see PinCheckActivity
                //mHelper = new FingerprintHandler(mActivity, this);

                mHelper.startAuth(mFingerprintManager, mCryptoObject);
            } else {
                mFingerprintDisabled = true;
            }
        } else {
            mFingerprintDisabled = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected boolean generateKey() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            return false;
        }

        try {
            mKeyStore.load(null);
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            if(e instanceof KeyPermanentlyInvalidatedException){
                return false;
            } else {
                throw new RuntimeException("Failed to init Cipher", e);
            }
        }
    }

    private void showPinDialog() {
        DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Dialog d = pinDialog();
        d.show();
        d.getWindow().setLayout((6 * width) / 7, (3 * height) / 4);
    }

    private Dialog pinDialog() {
        mDialog = new Dialog(mActivity);
        mDialog.setContentView(R.layout.x_pin_dialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mDialog.setOnKeyListener(this);

        pinHeader = mDialog.findViewById(R.id.titlePin);
        pin = mDialog.findViewById(R.id.pinInput);
        one = mDialog.findViewById(R.id.button1);
        two = mDialog.findViewById(R.id.button2);
        three = mDialog.findViewById(R.id.button3);
        four = mDialog.findViewById(R.id.button4);
        five = mDialog.findViewById(R.id.button5);
        six = mDialog.findViewById(R.id.button6);
        seven = mDialog.findViewById(R.id.button7);
        eight = mDialog.findViewById(R.id.button8);
        nine = mDialog.findViewById(R.id.button9);
        zero = mDialog.findViewById(R.id.button0);
        delete = mDialog.findViewById(R.id.buttonDeleteBack);
        logOut = mDialog.findViewById(R.id.logOut);
        wrongPin = mDialog.findViewById(R.id.wrongPin);
        pinHeader.setVisibility(View.VISIBLE);
        logOut.setVisibility(View.VISIBLE);

        TextView fingerprintInfo = mDialog.findViewById(R.id.fingerprint);
        if(isFingerprintDesired()){
            fingerprintInfo.setVisibility(View.VISIBLE);
        } else {
            fingerprintInfo.setVisibility(View.GONE);
        }

        logOut.setOnClickListener(this);
        pinHeader.setOnClickListener(this);
        pin.setOnClickListener(this);
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        delete.setOnClickListener(this);

        return mDialog;
    }

    private void closePinDialog() {
        mDialog.dismiss();
        minimizeApp();
    }

    @Override
    public void onClick(View view) {
        if (mPinText.length() < SecurityEditPin.pinLength) {
            wrongPin.setVisibility(View.GONE);
            switch (view.getId()) {
                case R.id.button0:
                    mPinText += "0";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button1:
                    mPinText += "1";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button2:
                    mPinText += "2";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button3:
                    mPinText += "3";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button4:
                    mPinText += "4";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button5:
                    mPinText += "5";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button6:
                    mPinText += "6";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button7:
                    mPinText += "7";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button8:
                    mPinText += "8";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.button9:
                    mPinText += "9";
                    mHiddenPin += "*";
                    pin.setText(mHiddenPin);
                    break;
                case R.id.buttonDeleteBack:
                    if (mHiddenPin.length() > 0) {
                        mHiddenPin = mHiddenPin.substring(0, mHiddenPin.length() - 1);
                        mPinText = mPinText.substring(0, mPinText.length() - 1);
                        pin.setText(mHiddenPin);
                    }
                    break;
                case R.id.logOut:
                    mSessionManager.logoutUser(mActivity);
                    ParseUser.logOut();
                    logOut.setVisibility(View.GONE);
                    break;
            }
        }

        //check for pin correctness
        if (mPinText.length() == SecurityEditPin.pinLength) {
            String userPin = ParseUser.getCurrentUser().getString("code");
            if (!userPin.equals(mPinText)) {
                pin.setText("");
                mPinText = "";
                mHiddenPin = "";
                wrongPin.setVisibility(View.VISIBLE);
                long[] pattern = {0, 500, 100, 500};
                mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(pattern, -1);
            } else {
                logOut.setVisibility(View.GONE);
                pinHeader.setVisibility(View.GONE);
                close();
            }
        }
    }

    public Dialog check() {
        return mDialog;
    }

    public void close() {
        if( mListener != null){
            mListener.onPinSuccess();
        }
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
        if (mHelper != null) {
            mHelper.stopListening();
        }
    }

    //force application in background
    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(startMain);
    }

    @Override
    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                closePinDialog();
                break;
            default:
                return true;
        }
        return false;
    }

    private boolean isFingerprintDesired() {
        ParseUser user = ParseUser.getCurrentUser();
        if(mFingerprintDisabled || user == null) {
            return false;
        }
        return user.getBoolean("touchID");
    }
}
