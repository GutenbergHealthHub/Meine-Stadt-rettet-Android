package com.aurel.ecorescue.x_tools;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.util.TypedValue;

/**
 * Created by daniel on 7/16/17.
 */

public class x_Helper {
    public static int GetPixelsForDPValue(Context context, int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static boolean permissionGranted(Context context, String accessFineLocation) {
        return ActivityCompat.checkSelfPermission(context, accessFineLocation) == PackageManager.PERMISSION_GRANTED;
    }

}
