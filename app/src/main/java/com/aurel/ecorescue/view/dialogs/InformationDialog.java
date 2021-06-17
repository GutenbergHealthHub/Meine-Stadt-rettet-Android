package com.aurel.ecorescue.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.aurel.ecorescue.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by daniel on 6/21/17.
 */

public class InformationDialog extends DialogFragment {

    private String title, message;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        if(!title.equals("")){
            builder.setTitle(title);
        }
        title = "";
        message = "";
        return builder.create();
    }

    public void setTitleAndMessage(String title, String message){
        this.title = title;
        this.message = message;
    }
}
