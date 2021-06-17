package com.aurel.ecorescue.view.f_login_register;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.data.CertificatesRepository;
import com.aurel.ecorescue.data.SettingsRepository;
import com.aurel.ecorescue.data.UserRepository;
import com.aurel.ecorescue.service.SessionManager;
import com.aurel.ecorescue.view.MainActivity;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import timber.log.Timber;

public class EmailVerificationActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        context = this;

        Handler handler = new Handler();
        int delay = 2000; //check every 2 seconds

        handler.postDelayed(new Runnable(){
            public void run(){
                ParseUser u = ParseUser.getCurrentUser();
                if (u != null) {
                    u.fetchInBackground((user, e) -> {
                        boolean isVerified = ParseUser.getCurrentUser().getBoolean("emailVerified");
                        if(!isVerified ) {
                            handler.postDelayed(this, delay);
                        } else {
                            findViewById(R.id.buttonCheckMail).setVisibility(View.GONE);
                            findViewById(R.id.buttonSkip).setVisibility(View.GONE);
                            findViewById(R.id.buttonFinished).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }, delay);
    }

    @Override
    public void onBackPressed(){
//        super.onBackPressed();
        finishActivity(null);
    }

    public void finishActivity(View view) {
        ParseUser.logOutInBackground(e -> {
            if (e == null) {
                UserRepository userRepository = UserRepository.getInstance();
                userRepository.reset();
                CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
                certificatesRepository.reset();
                SettingsRepository settingsRepository = SettingsRepository.getInstance();
                if (context!=null) userRepository.refreshData(context);
            }
        });
        startActivity(new Intent(this, LoginRegisterActivity.class));
        finish();
    }


    public void finishAfterVerifiedActivity(View view) {
        final Intent loginIntent = new Intent(this, MainActivity.class);
        new SessionManager(getApplicationContext()).createLoginSession("EcoRescue", ParseUser.getCurrentUser().getEmail());
        loginIntent.putExtra("SESSION", false);
        loginIntent.putExtra("ACTIVATED", ParseUser.getCurrentUser().getBoolean("activated"));
//        ParseUser user = ParseUser.getCurrentUser();
//        try {
//            user.signUp();
//        } catch (ParseException ex) {
//            Timber.d("Error signing user");
//            ex.printStackTrace();
//        }
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.reset();
        CertificatesRepository certificatesRepository = CertificatesRepository.getInstance();
        certificatesRepository.reset();
        SettingsRepository settingsRepository = SettingsRepository.getInstance();
        ActivityCompat.finishAffinity(this);
        startActivity(loginIntent);
        finish();
    }

    public void checkByEmailClient(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        try{
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), getString(R.string.no_email_app_found), Toast.LENGTH_SHORT).show();
        }
    }
}
