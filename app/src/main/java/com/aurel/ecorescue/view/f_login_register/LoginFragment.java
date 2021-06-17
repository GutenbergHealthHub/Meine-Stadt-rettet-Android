package com.aurel.ecorescue.view.f_login_register;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.StringRes;

import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.view.MainActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText mEmailView, mPasswordView;
    private SessionManager mSession;
    private Activity mActivity;
    private Button mButton;
    private Context context;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);
        context = getContext();
        mButton = fragmentView.findViewById(R.id.signInButton);
        mActivity = getActivity();
        mSession = new SessionManager(mActivity);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (mSession.isLoggedIn()) {
            ActivityCompat.finishAffinity(mActivity);
            // Start an intent for the logged in mActivity
            Intent loginIntent = new Intent(mActivity, MainActivity.class);
            loginIntent.putExtra("SESSION", true);
            loginIntent.putExtra("ACTIVATED", currentUser.getBoolean("activated"));
            ParseUser user = ParseUser.getCurrentUser();
//            try {
//                // TODO: Crashes the app
//                user.signUp();
//            } catch (ParseException ex) {
//                Timber.d("Error signing user");
//                ex.printStackTrace();
//            }
            UserRepository userRepository = UserRepository.getInstance();
            userRepository.refreshData(getContext());
            userRepository.reset();
            CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
            certificatesRepository.reset();
            SettingsRepository settingsRepository = SettingsRepository.getInstance();
            startActivity(loginIntent);
            mActivity.finish();
        }

        mEmailView = fragmentView.findViewById(R.id.signInEmail);
        mPasswordView = fragmentView.findViewById(R.id.signInPassword);

        for (int id : new int[]{R.id.signInButton, R.id.forgotPasswordButton, R.id.finishButton,}) {
            fragmentView.findViewById(id).setOnClickListener(this);
        }

        // Inflate the layout for this fragment
        return fragmentView;
    }

    public void forgotPassword() {
        String email = mEmailView.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(mActivity.getApplicationContext(), getResources().getText(R.string.login_email_required), Toast.LENGTH_SHORT).show();
            mEmailView.setError(getString(R.string.login_email_required));
        } else if (isValidEmail(email)) {
            requestResetPasswordMail(email);
        } else {
            Toast.makeText(mActivity.getApplicationContext(), getResources().getText(R.string.login_not_valid_email), Toast.LENGTH_SHORT).show();
            mEmailView.setError(getString(R.string.login_not_valid_email));
        }
    }

    public void signIn() {
        final Intent loginIntent = new Intent(mActivity, MainActivity.class);
        mButton.setEnabled(false);
        ParseUser.logInInBackground(mEmailView.getText().toString().toLowerCase(), mPasswordView.getText().toString(), (user, e) -> {
            if (user != null) {
                if (user.getBoolean("emailVerified")) {
                    //successful sign in
                    //Remove pin page for now
                    SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(mActivity);
                    editor.putBoolean(x_EcoPreferences.AppComesFromBackground, false);
                    editor.apply();
                    //
                    mSession.createLoginSession("EcoRescue", mEmailView.getText().toString());
                    loginIntent.putExtra("SESSION", false);
                    loginIntent.putExtra("ACTIVATED", user.getBoolean("activated"));
                    ActivityCompat.finishAffinity(mActivity);
//                    ParseUser user2 = ParseUser.getCurrentUser();
//                    try {
//                        user2.signUp();
//                    } catch (ParseException ex) {
//                        Timber.d("Error signing user");
//                        ex.printStackTrace();
//                    }
                    UserRepository userRepository = UserRepository.getInstance();
                    userRepository.refreshData(getContext());
                    userRepository.reset();
                    CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
                    certificatesRepository.reset();
                    startActivity(loginIntent);
                } else {
                    ParseUser.logOutInBackground(e1 -> {
                        if (e1 == null) {
                            UserRepository userRepository = UserRepository.getInstance();
                            userRepository.reset();
                            CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
                            certificatesRepository.reset();
                            if (context!=null) userRepository.refreshData(context);
                        }
                    });
                    Toast.makeText(mActivity.getApplicationContext(), getResources().getText(R.string.email_verification_failed), Toast.LENGTH_SHORT).show();
                    mButton.setEnabled(true);
                }
            } else {
                mButton.setEnabled(true);
                mPasswordView.getText().clear();
                Toast.makeText(mActivity.getApplicationContext(), getResources().getText(R.string.wrong_credentials), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.signInButton:
                signIn();
                break;
            case R.id.forgotPasswordButton:
                forgotPassword();
                break;
            case R.id.finishButton:
                mActivity.finish();
                break;
        }
    }


    private void requestResetPasswordMail(String email) {
        ParseUser.requestPasswordResetInBackground(email,
                new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            showToast(R.string.reset_password_success);
                        } else {
                            if (e.getCode() == com.parse.ParseException.INVALID_EMAIL_ADDRESS) {
                                showToast(R.string.password_reset_error_invalid_email);
                            }
                            if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                showToast(R.string.password_reset_error_connection_failed);
                            } else
                                showToast(R.string.reset_password_error);
                        }
                    }
                });
    }

    private void showToast(@StringRes int textResource) {
        Toast.makeText(mActivity.getApplicationContext(), textResource, Toast.LENGTH_LONG).show();
    }

    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}

