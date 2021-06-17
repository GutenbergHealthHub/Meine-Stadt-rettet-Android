package com.aurel.ecorescue.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.aurel.ecorescue.R;

/**
 * Created by daniel on 6/21/17.
 */

public class LoadingDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

}
