package com.aurel.ecorescue.security;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.View;
import android.widget.Switch;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.profile.ProfileData;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.parse.ParseUser;


/**
 * Created by aurel on 29-Aug-16.
 */
public class SecurityActivityX extends EmergencyAppCompatActivity implements View.OnClickListener {


    Switch touchId;

    private Toolbar mToolbar;
    private ProfileData mProfile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_security_settings_view);
        touchId = findViewById(R.id.settings_touch_id_switch);
        mToolbar = findViewById(R.id.securityToolbar);
        setSupportActionBar(mToolbar);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProfile = getIntent().getParcelableExtra(MainActivity.PROFILE);
        if (mProfile == null) {
            mProfile = new ProfileData().createFromCurrentUser();
        }

        touchId.setChecked(mProfile.isTouchID());
        touchId.setOnCheckedChangeListener((compoundButton, b) -> {
            ParseUser c = ParseUser.getCurrentUser();
            c.put("touchID", b);
            c.saveInBackground();
            mProfile.setTouchID(b);
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    public void changePin(View view) {
        Intent p = new Intent(this, SecurityEditPin.class);
        p.putExtra(MainActivity.PROFILE, mProfile);
        startActivity(p);
    }

    public void changePassword(View view) {
        Intent pass = new Intent(this, SecurityEditPassword.class);
        pass.putExtra(MainActivity.PROFILE, mProfile);
        startActivity(pass);
    }
}
