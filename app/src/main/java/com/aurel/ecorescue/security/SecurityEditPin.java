package com.aurel.ecorescue.security;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.profile.ProfileData;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.dialogs.LoadingDialog;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import static com.aurel.ecorescue.R.id.last_page_button;

/**
 * Created by aurel on 12-Nov-16.
 */

public class SecurityEditPin extends EmergencyAppCompatActivity implements View.OnClickListener {

    private TextView pin, mTitle;
    private String pinText;
    private ProfileData mProfile;
    private ImageButton back;
    private Button save;
    public static final int pinLength = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_edit_security_pin);
        pinText = "";
        pin = (TextView) findViewById(R.id.pinInput);
        mTitle = (TextView) findViewById(R.id.toolbar_title);
        mTitle.setText(getResources().getString(R.string.pin));
        back = (ImageButton) findViewById(last_page_button);
        save = (Button) findViewById(R.id.next_page_button);
        save.setText(getResources().getString(R.string.save));
        save.setOnClickListener(this);
        back.setOnClickListener(this);


    }

    public void sendData() {
        if (check()) {
            ParseUser c = ParseUser.getCurrentUser();
            c.put("code", pin.getText().toString());
            final LoadingDialog dialog = new LoadingDialog();
            dialog.show(getFragmentManager(), "loading");

            c.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    dialog.dismiss();
                    finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProfile = (ProfileData) getIntent().getParcelableExtra(MainActivity.PROFILE);

        SharedPreferences.Editor editor = x_EcoPreferences.GetEditor(this);
        editor.putBoolean(x_EcoPreferences.AppComesFromBackground, true);
//        editor.commit();
        editor.apply();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.last_page_button:
                onBackPressed();
                break;
            case R.id.next_page_button:
                sendData();
                break;
            case R.id.button0:
                if (pinText.length() < pinLength) {
                    pinText += "0";
                    pin.setText(pinText);
                }
                break;
            case R.id.button1:
                if (pinText.length() < pinLength) {
                    pinText += "1";
                    pin.setText(pinText);
                }
                break;
            case R.id.button2:
                if (pinText.length() < pinLength) {
                    pinText += "2";
                    pin.setText(pinText);
                }
                break;
            case R.id.button3:
                if (pinText.length() < pinLength) {
                    pinText += "3";
                    pin.setText(pinText);
                }
                break;
            case R.id.button4:
                if (pinText.length() < pinLength) {
                    pinText += "4";
                    pin.setText(pinText);
                }
                break;
            case R.id.button5:
                if (pinText.length() < pinLength) {
                    pinText += "5";
                    pin.setText(pinText);
                }
                break;
            case R.id.button6:
                if (pinText.length() < pinLength) {
                    pinText += "6";
                    pin.setText(pinText);
                }
                break;
            case R.id.button7:
                if (pinText.length() < pinLength) {
                    pinText += "7";
                    pin.setText(pinText);
                }
                break;
            case R.id.button8:
                if (pinText.length() < pinLength) {
                    pinText += "8";
                    pin.setText(pinText);
                }
                break;
            case R.id.button9:
                if (pinText.length() < pinLength) {
                    pinText += "9";
                    pin.setText(pinText);
                }
                break;
            case R.id.buttonDeleteBack:
                if (pinText.length() > 0) {
                    pinText = pinText.substring(0, pinText.length() - 1);
                    pin.setText(pinText);
                }
                break;
        }
    }

    public boolean check() {
        if (pin.getText().length() < pinLength) {
            Toast.makeText(getApplication(), getResources().getString(R.string.pin_short), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(this, getResources().getString(R.string.save), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
