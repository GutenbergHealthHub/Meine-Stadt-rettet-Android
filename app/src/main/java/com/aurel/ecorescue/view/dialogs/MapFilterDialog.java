package com.aurel.ecorescue.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.aurel.ecorescue.R;
import com.aurel.ecorescue.interfaces.OnMapFilterApplyListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by daniel on 7/7/17.
 */

public class MapFilterDialog extends DialogFragment {

    private OnMapFilterApplyListener listener;
    private ArrayList mSelectedItems;
    public boolean filterDefi,filterHospital,filterPharmacy, filterFireDep, filterDoctor, filterDentist;

    public void SetListener(OnMapFilterApplyListener listener){
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList();  // Where we track the selected items
        final String[] list = {
                getString(R.string.map_filter_defibrilator),
                getString(R.string.map_filter_hospital),
                getString(R.string.map_filter_pharmacy),
                getString(R.string.map_filter_fire),
                getString(R.string.map_filter_doctor),
                getString(R.string.map_filter_dentist),
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.map_filter_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(list, new boolean[]{filterDefi, filterHospital, filterPharmacy, filterFireDep, filterDoctor, filterDentist},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                switch (which){
                                    case 0:
                                    filterDefi = isChecked;
                                        break;
                                    case 1:
                                        filterHospital = isChecked;
                                        break;
                                    case 2:
                                        filterPharmacy = isChecked;
                                        break;
                                    case 3:
                                        filterFireDep = isChecked;
                                        break;
                                    case 4:
                                        filterDoctor = isChecked;
                                        break;
                                    case 5:
                                        filterDentist= isChecked;
                                        break;

                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        listener.applyMapFilter(filterDefi,filterPharmacy,filterHospital,filterFireDep, filterDoctor, filterDentist);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}
