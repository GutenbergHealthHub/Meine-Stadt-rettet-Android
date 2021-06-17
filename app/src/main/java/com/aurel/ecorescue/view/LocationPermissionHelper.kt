package com.aurel.ecorescue.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.aurel.ecorescue.service.BackgroundLocationService.Companion.startBackgroundService
import com.aurel.ecorescue.utils.Android.isAndroid10AndAbove
import timber.log.Timber
import java.util.*

const val FINE_LOCATION_REQUEST_CODE = 90
const val COARSE_LOCATION_REQUEST_CODE = 91
const val BACKGROUND_LOCATION_REQUEST_CODE = 92

object LocationPermissionHelper {

    @JvmStatic
    fun isFineLocationGranted(context: Context) = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun isCoarseLocationGranted(context: Context) = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun isBackgroundLocationGranted(context: Context) = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun requestFineLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_REQUEST_CODE)
    }

    @JvmStatic
    fun requestBackgroundLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), BACKGROUND_LOCATION_REQUEST_CODE)
    }

    @JvmStatic
    fun isLocationAuthorized(context: Context?): Boolean {
        context?.let {
            val hasAccessFineLocation = isFineLocationGranted(context)
            val hasAccessCoarseLocation = isCoarseLocationGranted(context)
            var hasAccessBackgroundLocation = true
            if (isAndroid10AndAbove()) {
                hasAccessBackgroundLocation = isBackgroundLocationGranted(context)
            }
            return hasAccessFineLocation && hasAccessCoarseLocation && hasAccessBackgroundLocation
        } ?: return false
    }


}