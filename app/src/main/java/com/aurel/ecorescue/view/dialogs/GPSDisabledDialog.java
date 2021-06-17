package com.aurel.ecorescue.view.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

import com.aurel.ecorescue.R;

/**
 * Created by daniel on 7/30/17.
 */

public class GPSDisabledDialog {

    public static void checkGPS(Context context){
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
           showSettingsAlert(context);
        }

    }

    private static void showSettingsAlert(final Context mContext) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog title
        alertDialog.setTitle(mContext.getResources().getString(R.string.warning_header));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getResources().getString(R.string.warning_body));

        // On pressing Settings button
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.settings),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton(mContext.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
}
