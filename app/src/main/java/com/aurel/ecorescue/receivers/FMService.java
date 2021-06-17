package com.aurel.ecorescue.receivers;

import androidx.annotation.NonNull;

import com.aurel.ecorescue.R;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.fcm.ParseFirebaseMessagingService;

import java.util.Objects;

import timber.log.Timber;

public class FMService extends ParseFirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        super.onMessageReceived(message);
        Timber.d("Message: %s", message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if (ParseUser.getCurrentUser()==null) return;
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("GCMSenderId", getResources().getString(R.string.gcm_defaultSenderId));
        parseInstallation.put("userRelation", ParseUser.getCurrentUser());
        parseInstallation.setDeviceToken(token);
        parseInstallation.setPushType("gcm");
        parseInstallation.saveInBackground();
        ParsePush.subscribeInBackground("global");
    }
}
