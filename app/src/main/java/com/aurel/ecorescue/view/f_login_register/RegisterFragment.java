package com.aurel.ecorescue.view.f_login_register;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.aurel.ecorescue.BuildConfig;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.util.Calendar;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {


    EditText emailEditText;

    EditText passwordEditText;

    EditText passwordRepeatEditText;

    private Button mButton;


    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_register, container, false);
        setUpView(fragmentView);

        for (int id : new int[]{R.id.registerButton, R.id.finishButton,}) {
            fragmentView.findViewById(id).setOnClickListener(this);
        }

        // Inflate the layout for this fragment
        return fragmentView;
    }

    public void setUpView(View view){
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        passwordRepeatEditText = view.findViewById(R.id.passwordRepeatEditText);
        mButton = view.findViewById(R.id.registerButton);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.registerButton:
                String email = emailEditText.getText().toString();
                if(!email.isEmpty() && !passwordEditText.getText().toString().isEmpty() && !passwordRepeatEditText.getText().toString().isEmpty()) {
                    if(passwordEditText.getText().toString().equals(passwordRepeatEditText.getText().toString())) {
                        String password = passwordRepeatEditText.getText().toString();
                        boolean hasUppercase = !password.equals(password.toLowerCase());
                        boolean hasLowercase = !password.equals(password.toUpperCase());
                        boolean hasNumber   = password.matches(".*[0-9]+.*");
                        if(password.length() >= 6 && hasUppercase && hasLowercase && hasNumber) {
                            if(isValidEmailAddress(email)) {
                                createNewProfile();
                            } else {
                                Toast.makeText(getActivity(), R.string.wrong_email_format, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.password_not_conform), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_password_and_repeat), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.fill_all_fileds), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.finishButton:
                getActivity().finish();
                break;
        }
    }

    public void createNewProfile() {
            final ParseUser newUser = new ParseUser();
            newUser.setUsername(((EditText)getActivity().findViewById(R.id.emailEditText)).getText().toString().toLowerCase());
            newUser.setEmail(((EditText)getActivity().findViewById(R.id.emailEditText)).getText().toString().toLowerCase());
            newUser.setPassword(((EditText)getActivity().findViewById(R.id.passwordEditText)).getText().toString());
            newUser.put("sound", "horn");
            if (BuildConfig.APPLICATION_ID.equals("ecorium.MeineStadtRettet")){
                newUser.put("origin", "asb");
            }
            newUser.put("activated", false);
            newUser.put("pausedUntil", Calendar.getInstance().getTime());
            mButton.setEnabled(false);
            newUser.signUpInBackground(e -> {
                if (e == null) {
                    ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
                    parseInstallation.put("GCMSenderId", getResources().getString(R.string.gcm_defaultSenderId));
                    parseInstallation.put("userRelation", newUser);
                    parseInstallation.saveInBackground();
                    ParsePush.subscribeInBackground("global");
//                    ParseUser user = ParseUser.getCurrentUser();
////                    try {
////                        user.signUp();
////                    } catch (ParseException ex) {
////                        Timber.d("Error signing user");
////                        ex.printStackTrace();
////                    }
                    UserRepository userRepository = UserRepository.getInstance();
                    userRepository.reset();
                    CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
                    certificatesRepository.reset();

                    Intent emailVerificationIntent = new Intent(getActivity(), EmailVerificationActivity.class);
                    startActivity(emailVerificationIntent);
                    getActivity().finish();

                } else {
                    mButton.setEnabled(true);
                    Log.d("ERROR", e.getCode() + "");
                    Log.d("ERROR", e.getLocalizedMessage() + "");
                    if (e.getCode() == 202) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.username_exists), Toast.LENGTH_SHORT).show();
                    } else {
                        Timber.d(e);
                        Toast.makeText(getActivity(), getResources().getString(R.string.problem), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
