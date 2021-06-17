package com.aurel.ecorescue.security;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.x_emergency.EmergencyAppCompatActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by aurel on 28-Nov-16.
 */

public class SecurityEditPassword extends EmergencyAppCompatActivity implements View.OnClickListener {


    public static Pattern pswNamePtrn = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}$");
    private TextView mTitle;
    private Button save;
    private ImageButton back;
    private EditText newPass, newPassRepeat, oldPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_change_password);
        newPass = (EditText) findViewById(R.id.new_pass);
        oldPass = (EditText) findViewById(R.id.old_pass);
        newPassRepeat = (EditText) findViewById(R.id.repeat_new_pass);
        mTitle = (TextView) findViewById(R.id.toolbar_title);
        mTitle.setText(getResources().getString(R.string.password));
        save = (Button) findViewById(R.id.next_page_button);
        back = (ImageButton) findViewById(R.id.last_page_button);
        save.setOnClickListener(this);
        back.setOnClickListener(this);
    }


    public boolean check() {



        String rPassword = newPassRepeat.getText().toString(), nPassword = newPass.getText().toString();
        if (rPassword.trim().equals("") || nPassword.trim().equals("")) {
            if (nPassword.trim().equals("")) {
                newPass.setError(getResources().getString(R.string.req_field));
            }
            if (rPassword.trim().equals("")) {
                newPassRepeat.setError(getResources().getString(R.string.req_field));
            }
        } else if (!nPassword.equals(rPassword)) {
            newPass.setError(getResources().getString(R.string.pass_missmatch));
            newPassRepeat.setError(getResources().getString(R.string.pass_missmatch));
            return false;
        }
        if (validatePassword(nPassword)) {
            Toast.makeText(this, getResources().getString(R.string.save), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static boolean validatePassword(String userName) {

        Matcher mtch = pswNamePtrn.matcher(userName);
        if (mtch.matches()) {
            return true;
        }
        return false;
    }

    public void validateOldPW(){
        String oldPassword = oldPass.getText().toString();
        ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), oldPassword, new LogInCallback(){
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(e == null){
                    saveData();
                } else {
                    Toast.makeText(SecurityEditPassword.this, R.string.old_password_wrong, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void saveData() {
        if (check()) {
            ParseUser c = ParseUser.getCurrentUser();
            c.setPassword(newPass.getText().toString());
            c.saveInBackground();
            onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_page_button:
                validateOldPW();
                break;
            case R.id.last_page_button:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
