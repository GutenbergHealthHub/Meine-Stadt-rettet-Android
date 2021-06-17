package com.aurel.ecorescue.adapter;

/**
 * Created by aurel on 20-Nov-16.
 */

public class EmergencyItem extends ElementType {

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    private Notification notification;

    @Override
    public int getType() {
        return TYPE_EMERGENCY;
    }
}
