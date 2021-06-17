package com.aurel.ecorescue.service.notificationservice;

import android.content.Intent;

import android.app.Notification;

public interface NotificationService {

    void showEmergencyNotification(String title, String message, Intent intent, String sound);
    void showEmergencyCancelledNotification(String emergencyId);
    void showEmergencyExpiredNotification(String emergencyId);
    void showGpsDisabledNotification();

}
