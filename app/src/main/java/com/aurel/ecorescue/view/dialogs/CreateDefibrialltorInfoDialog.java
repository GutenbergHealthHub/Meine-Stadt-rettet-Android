package com.aurel.ecorescue.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.view.MainActivity;
import com.aurel.ecorescue.view.f_login_register.LoginRegisterActivity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by daniel on 11/11/2017.
 */

public class CreateDefibrialltorInfoDialog extends DialogFragment {

    public boolean userIsRegistered;
    public Activity activity;

    public CreateDefibrialltorInfoDialog(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.create_defi_info_title))
                .setMessage(getString(R.string.create_defi_info));

        if(userIsRegistered) {
            builder.setPositiveButton(getString(R.string.create_defi_info_status), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            builder.setPositiveButton(getString(R.string.create_defi_info_register), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent loginIntent = new Intent(activity, LoginRegisterActivity.class);
                    startActivity(loginIntent);
                }
            });
        }

        return builder.create();
    }
}
