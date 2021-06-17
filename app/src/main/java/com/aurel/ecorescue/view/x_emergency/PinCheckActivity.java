package com.aurel.ecorescue.view.x_emergency;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.service.FingerprintHandler;
import com.aurel.ecorescue.view.ThemedActivity;
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

public class PinCheckActivity extends ThemedActivity {

    private static final String KEY_NAME = "example_key";
    private EditText mEditTextPin;
    private ViewGroup mPinIndicator;
    private KeyStore mKeyStore;
    private Cipher mCipher;
    private boolean mFingerprintDisabled;
    private Context context;
    FingerprintHandler mFingerprintHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_check);
        this.context = this;
        PinCheckActivity activity = this;
        mPinIndicator = findViewById(R.id.pinIndicator);
        mEditTextPin = findViewById(R.id.pin_textview);
        mEditTextPin.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String pin = s.toString();
                for (int i = 0; i < mPinIndicator.getChildCount(); ++i) {
                    mPinIndicator.getChildAt(i).setBackground(getResources().getDrawable((i < pin.length()) ? R.drawable.msr_button_white : R.drawable.msr_button_outline));
                }

                if (pin.length() == 6) {
                    mEditTextPin.setText("");
                    String userPin = ParseUser.getCurrentUser().getString("code");
                    if (userPin==null) {success();}
                    if (!userPin.equals(pin)) {
                        activity.failure(getResources().getString(R.string.wrong_pin));
                    } else {
                        success();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        mEditTextPin.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_ENTER);
        mEditTextPin.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditTextPin, InputMethodManager.SHOW_IMPLICIT);

        findViewById(R.id.content).setOnClickListener(v -> {
            mEditTextPin.requestFocus();
            InputMethodManager imm2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm2.showSoftInput(mEditTextPin, InputMethodManager.SHOW_IMPLICIT);
        });

//        boolean showFingerprint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isFingerprintDesired();
//        findViewById(R.id.fingerprintImageButton).setVisibility( showFingerprint ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Fingerprint API only available on from Android 6.0 (M)
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (!fingerprintManager.isHardwareDetected()) {
                // Device doesn't support fingerprint authentication
                findViewById(R.id.fingerprintImageButton).setVisibility(View.GONE);
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                // User hasn't enrolled any fingerprints to authenticate with
                findViewById(R.id.fingerprintImageButton).setVisibility(View.GONE);
            } else {
                // Everything is ready for fingerprint authentication
                findViewById(R.id.fingerprintImageButton).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
//        if (mEditTextPin!=null) {
//            mEditTextPin.requestFocus();
//            InputMethodManager imm2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm2.showSoftInput(mEditTextPin, InputMethodManager.SHOW_IMPLICIT);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        mEditTextPin.setText("");
//        mEditTextPin.requestFocus();
//
//        boolean showFingerprint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isFingerprintDesired();
//        findViewById(R.id.fingerprintImageButton).setVisibility( showFingerprint ? View.VISIBLE : View.GONE);
    }

    public void activateFingerprint(View view) {
        fingerprintAuthentication();
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(getResources().getString(R.string.fingerprint_activated))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFingerprintHelper.stopListening();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void success() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    public void failure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        long[] pattern = {0, 500, 100, 500};
        ((Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pattern, -1);
    }

    private boolean isFingerprintDesired() {
        ParseUser user = ParseUser.getCurrentUser();
        if (mFingerprintDisabled || user == null) {
            return false;
        }
        return user.getBoolean("touchID");
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void fingerprintAuthentication() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this, "Lock screen security not enabled in Settings", Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
            return;
        }

        if (generateKey()) {
            if (cipherInit()) {
                FingerprintManager.CryptoObject mCryptoObject = new FingerprintManager.CryptoObject(mCipher);
                mFingerprintHelper = new FingerprintHandler(this, this);
                mFingerprintHelper.startAuth(fingerprintManager, mCryptoObject);
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

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            return false;
        }

        try {
            mKeyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
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
            if (e instanceof KeyPermanentlyInvalidatedException) {
                return false;
            } else {
                throw new RuntimeException("Failed to init Cipher", e);
            }
        }
    }
}
