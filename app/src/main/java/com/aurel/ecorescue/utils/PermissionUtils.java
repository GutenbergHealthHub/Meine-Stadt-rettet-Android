package com.aurel.ecorescue.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    /*
     * Default request codes for permissions
     */
    public static final int REQUEST_FINE_LOCATION = 1;
    public static final int REQUEST_COARSE_LOCATION = 2;
    public static final int REQUEST_CAMERA = 3;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 4;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 5;

    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissions(Context context, String... permissions){
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



    public static void requestPermission(Activity activity, int requestCode){
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                requestPermission(activity, requestCode, Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case REQUEST_COARSE_LOCATION:
                requestPermission(activity, requestCode, Manifest.permission.ACCESS_COARSE_LOCATION);
                break;
            case REQUEST_CAMERA:
                requestPermission(activity, requestCode, Manifest.permission.CAMERA);
                break;
            case REQUEST_READ_EXTERNAL_STORAGE:
                requestPermission(activity, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                requestPermission(activity, requestCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
            default:
                break;
        }
    }

    public static void requestPermission(Activity activity, int requestCode, String permission){
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static void requestPermissions(Activity activity, int requestCode, String... permissions){
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }


}
