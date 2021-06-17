package com.aurel.ecorescue.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.aurel.ecorescue.R
import com.aurel.ecorescue.parse.UserDao
import com.parse.ParseAnonymousUtils
import com.parse.ParseException
import com.parse.ParseGeoPoint
import com.parse.ParseUser
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


private const val UPDATE_INTERVAL = 1000L * 60 * 60 * 3 // 3 Hours in millis
private const val MIN_DISPLACEMENT = 400f // 400 meters

// Test mode
//private const val UPDATE_INTERVAL = 1000L * 60 * 60 // 60 Minutes in millis
//private const val MIN_DISPLACEMENT = 0f // 0 m

class BackgroundLocationService: Service() {

    companion object {

        @JvmStatic
        fun startBackgroundService(context: Context){
            if (ParseUser.getCurrentUser() == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, BackgroundLocationService::class.java))
            } else {
                context.startService(Intent(context, BackgroundLocationService::class.java))
            }
        }

        @JvmStatic
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ROOT)

    }

    override fun onBind(intent: Intent?): IBinder? = null

    private lateinit var locationManager: LocationManager


    private fun requestLocationUpdates() {
//        if (!LocationPermissionHelper.isLocationAuthorized(this)) {
//            Timber.d("ERROR: Location permission is not authorized!")
//            return
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Timber.d("ERROR: Location permission is not authorized!")
            return
        }
        locationManager.removeUpdates(locationListener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, MIN_DISPLACEMENT, locationListener)
    }


    private val locationListener = object: LocationListener {
        override fun onLocationChanged(location: Location) {
            Timber.d("Location Updated: %s", location)
            val currentLocation = ParseGeoPoint(location.latitude, location.longitude)
            updateUserLocation(currentLocation)
//        monitorLocation(location)
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private fun updateUserLocation(geoPoint: ParseGeoPoint, prefs: SharedPreferences? = null) {
//        val user = ParseUser.getCurrentUser()
        UserDao.updateUserLocation(geoPoint)
//        if (user != null && user.isAuthenticated && !ParseAnonymousUtils.isLinked(user)) {
//            user.put("location", geoPoint)
//            user.saveInBackground { e: ParseException? ->
//                if (e != null) {
//                    Timber.d(e)
//                } else {
//                    Timber.d("LocationUpdated")
//                }
////                monitorLocationUpdate(geoPoint, e, prefs)
//            }
//        }
    }

    private fun monitorLocation(location: Location){
        val prefs = applicationContext.getSharedPreferences("Loc", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val strList = prefs.getString("list", null)
        val list = strList?.split("#")?.toMutableList() ?: mutableListOf()
        list.add("${simpleDateFormat.format(Date())},lat=${location.latitude},lng=${location.longitude}")
        editor.putString("list", TextUtils.join("#", list)).apply()
        Timber.d("LocationSet: %s", list)
    }

    private fun monitorLocationUpdate(geoPoint: ParseGeoPoint, e: ParseException?, prefs: SharedPreferences? = null){
        if (prefs == null) return
        val strList = prefs.getString("updates", null)
        val list = strList?.split("#")?.toMutableList() ?: mutableListOf()
        val sdf = simpleDateFormat
        val date = sdf.format(Date())
        if (e != null) {
            list.add("$date, Error: $e")
        } else {
            list.add(date + ",lat=" + geoPoint.latitude + ",lng=" + geoPoint.longitude)
        }
        prefs.edit().putString("updates", TextUtils.join("#", list)).apply()
    }

    override fun onCreate() {
        super.onCreate()
        var icon: Int = R.mipmap.ic_launcher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) icon = R.drawable.ic_ecorescue_material
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "MSR_is_running"
            val channel = NotificationChannel(CHANNEL_ID,
                    "Meine Stadt rettet is running",
                    NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_is_running_in_background_title))
                    .setContentText(getString(R.string.app_is_running_in_background_content))
                    .setSmallIcon(icon).build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        startBackgroundService(applicationContext)
    }
}
