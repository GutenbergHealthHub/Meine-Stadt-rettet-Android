package com.aurel.ecorescue.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aurel.ecorescue.data.UserRepository;

import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Time has reached");
        // Don't do anything if the app is not on the foreground
        UserRepository userRepository = UserRepository.getInstance();
        userRepository.updateStatus(context);

    }
}
