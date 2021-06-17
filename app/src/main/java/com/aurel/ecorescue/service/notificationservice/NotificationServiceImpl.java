package com.aurel.ecorescue.service.notificationservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.aurel.ecorescue.BuildConfig;
import com.aurel.ecorescue.R;
import com.aurel.ecorescue.utils.EmergencyAcceptedOnTimeUtils;
import com.aurel.ecorescue.x_tools.x_EcoPreferences;
import com.aurel.ecorescue.view.x_emergency.AcceptEmergencyActivity;

import java.util.List;

import timber.log.Timber;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/**
 * Created by aurel on 13-Oct-16.
 * <p>
 * Updated by Tobias Hollarek 20-Jan-2019
 */

public class NotificationServiceImpl implements NotificationService {


    public static final int DEFAULT_NOTIFICATION_ID = 4561;
    public static final int EMERGENCY_NOTIFICATION_ID = DEFAULT_NOTIFICATION_ID + 1;
    public static final String DELETE_ACTION = BuildConfig.APPLICATION_ID + ".ACTION_STOP_ALARM";
    public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "DEFAULT_" + String.valueOf(DEFAULT_NOTIFICATION_ID);
    public static final String LEGACY_EMERGENCY_NOTIFICATION_CHANNEL_ID = "EMERGENCY_" + String.valueOf(DEFAULT_NOTIFICATION_ID);
    private static final String DEFAULT_CHANNEL_NAME = "Notification";
    private static final int NOTIFICATION_ALARM_INTERVAL = 10000;
    private static final long[] ONE_SECOND_VIBRATION_PATTERN = new long[]{200, 300, 200, 300};
    private static CountDownTimer sNotificationRepeatTimer = null;

    private Context mContext;
    private boolean notificationsEnabled = true;


    public NotificationServiceImpl(Context context) {
        this.mContext = context;
        if (mContext!=null) {
            SharedPreferences sharedPreferences = x_EcoPreferences.GetSharedPreferences(mContext);
            notificationsEnabled = sharedPreferences.getBoolean("bypassDND", true);
        }
    }

    public static void stopCurrentAlert(Context context) {
        if(sNotificationRepeatTimer != null) {
            sNotificationRepeatTimer.cancel();
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(EMERGENCY_NOTIFICATION_ID);
        }
    }

    public void showEmergencyNotification(String title, String message, Intent intent, String sound) {
        Log.d("EcoRescue", "received emergency");
        if (TextUtils.isEmpty(message)) {
            return;
        }

        String channelId = createOrGetEmergencyNotificationChannel(sound);
        Notification notification = createNotification(channelId, title, message, getIcon(), getResultIntent(intent), getDeleteIntent());
        showNotificationLooping(EMERGENCY_NOTIFICATION_ID, notification);

        if (isAppIsInBackground(mContext)) {
            AlarmManager alarmManager = getAlarmManager();
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, x_EcoPreferences.EmergencyActiveDurationInMillis, getDeleteIntent());
            alarmManager.cancel(getUpdateIntent(intent));
//            SharedPreferences pref = x_EcoPreferences.GetSharedPreferences(mContext);
//            String expiredDescription = mContext.getString(R.string.notification_emergency_expired);
//            if (!pref.getBoolean(EcoPreferences.ActiveEmergencyAccepted, false)) {
//                String title = mContext.getResources().getString(R.string.new_emergency);
//                showEmergencyCancelledNotification(title, expiredDescription, mainActivityIntent);
//            }
        } else {
            Log.d("EcoRescue", "Sending broadcast that a new emergency got in.");
            Intent broadcast = new Intent();
            broadcast.setAction(mContext.getString(R.string.emergency_broadcast));
            mContext.sendBroadcast(broadcast);
        }
    }

    private PendingIntent getUpdateIntent(final Intent intent) {
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_NO_CREATE);
    }

    private void showNotificationLooping(int notificationID, Notification notification) {
        sNotificationRepeatTimer = new CountDownTimer(30000, NOTIFICATION_ALARM_INTERVAL ) {
            public void onTick(long millisUntilFinished) {
                if (mContext!=null) {
                    SharedPreferences preferences = x_EcoPreferences.GetSharedPreferences(mContext);
                    String id = preferences.getString(x_EcoPreferences.ActiveEmergencyId, "");
                    if (notificationsEnabled && !preferences.getBoolean("notificationAlreadyAccepted", false)) {
                        if (!EmergencyAcceptedOnTimeUtils.isEmergencyAccepted(id, mContext)) {
                            getNotificationManager().notify(notificationID, notification);
                        }
                    }
                } else if (notificationsEnabled) {
                    getNotificationManager().notify(notificationID, notification);
                }
            }
            public void onFinish() {
            }
        }.start();
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getDeleteIntent() {
        Intent deleteIntent = new Intent(DELETE_ACTION);
        return PendingIntent.getBroadcast(mContext, 0, deleteIntent, 0);
    }

    public void showEmergencyCancelledNotification(String emergencyId) {
        Intent intent = new Intent(mContext, AcceptEmergencyActivity.class);
        intent.putExtra("emergencyId", emergencyId);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        int icon = getIcon();

        String title = mContext.getString(R.string.notification_cancel_title);
        String message = mContext.getString(R.string.notification_cancel_message);
        PendingIntent resultPendingIntent = getResultIntent(intent);

        NotificationManager notificationManager = getNotificationManager();
        String channelId = createOrGetDefaultNotificationChannel();
        Notification notification = createNotification(channelId, title, message, icon, resultPendingIntent);
        if (notificationsEnabled)
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notification);
    }

    @Override
    public void showEmergencyExpiredNotification(String emergencyId) {

    }

    public void showGpsDisabledNotification() {
        String message = mContext.getResources().getString(R.string.cannot_get_emergencies);
        String title = mContext.getResources().getString(R.string.gps_off);
        int icon = R.drawable.heart_pulse;
        NotificationManager notificationManager = getNotificationManager();


        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent resultPendingIntent = getResultIntent(intent);

        String channelId = createOrGetDefaultNotificationChannel();
        Notification notification = createNotification(channelId, title, message, icon, resultPendingIntent);
        if (notificationsEnabled)
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, notification);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private String createOrGetEmergencyNotificationChannel(String sound) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Notification Channel for Android 8.0 and above
            Log.d("EcoRescue", "received emergency for 8.0");
            NotificationManager notificationManager = getNotificationManager();
            // find right or next free notification channel

            String channelId = "EMERGENCYCHANNEL_" + sound.toUpperCase();
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);


            if(notificationChannel == null ) {
                Log.d("EcoRescue", "Creating notification channel " + channelId);
                notificationChannel = new NotificationChannel(channelId, "Notfall-Benachrichtigung (" + sound + ")", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableVibration(true);
                long[] vibrationPattern = new long[NOTIFICATION_ALARM_INTERVAL / 1000 * ONE_SECOND_VIBRATION_PATTERN.length];
                for (int i = 0; i < NOTIFICATION_ALARM_INTERVAL / 1000; ++i) {
                    for (int j = 0; j < ONE_SECOND_VIBRATION_PATTERN.length; ++j) {
                        vibrationPattern[j + ONE_SECOND_VIBRATION_PATTERN.length * i] = ONE_SECOND_VIBRATION_PATTERN[j];
                    }
                }
                notificationChannel.setVibrationPattern(vibrationPattern); // { sleep, duration, sleep, duration, sleep, duration,...}
                notificationChannel.setSound(SoundAndVibrationUtils.getSoundRessourceUriByName(sound), new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build());
                getNotificationManager().createNotificationChannel(notificationChannel);
            }

            return notificationChannel.getId();
        }
        Log.d("EcoRescue", "Notification channels are not supported by this OS");
        return LEGACY_EMERGENCY_NOTIFICATION_CHANNEL_ID;
    }

    private String createOrGetDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Notification Channel for Android 8.0 and above
            Log.d("EcoRescue", "received emergency for 8.0");
            NotificationManager notificationManager = getNotificationManager();
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID);
            if(notificationChannel == null ) {
                Log.d("EcoRescue", "Creating notification channel " + DEFAULT_NOTIFICATION_CHANNEL_ID);
                notificationChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, DEFAULT_CHANNEL_NAME , NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableVibration(true);
                notificationChannel.setDescription("Standard Benachrichtigungen");
                notificationChannel.setName("Standard Benachrichtigungen");
                getNotificationManager().createNotificationChannel(notificationChannel);
            }
            return notificationChannel.getId();
        }
        Log.d("EcoRescue", "Notification channels are not supported by this OS");
        return DEFAULT_NOTIFICATION_CHANNEL_ID;
    }

    private Notification createNotification(String notificationChannelID, String title, String message, int icon, PendingIntent contentIntent) {
        return createNotification(DEFAULT_NOTIFICATION_CHANNEL_ID, title, message, icon, contentIntent, null);
    }

    private Notification createNotification(String notificationChannelID, String title, String message, int icon, PendingIntent contentIntent, PendingIntent deleteIntent) {
        Notification notification = new NotificationCompat.Builder(mContext, notificationChannelID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(icon)
                .setChannelId(notificationChannelID)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .build();
        notification.flags = Notification.FLAG_INSISTENT;
        return notification;
    }


    private PendingIntent getResultIntent(Intent intent) {
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private int getIcon() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return R.drawable.ic_ecorescue_material;
        else
            return R.mipmap.ic_launcher;
    }

    public static void clearNotifications(Context context){
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    private static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}


