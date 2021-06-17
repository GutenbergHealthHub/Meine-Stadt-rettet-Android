package com.aurel.ecorescue.data;

import com.aurel.ecorescue.interfaces.Callback;
import com.parse.LogOutCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class SettingsRepository {

    private static SettingsRepository sInstance;

    public static SettingsRepository getInstance() {
        if (sInstance == null) {
            synchronized (SettingsRepository.class) {
                if (sInstance == null) {
                    sInstance = new SettingsRepository();
                }
            }
        }
        return sInstance;
    }


    private SettingsRepository(){}


    public void testEmergencyNow(@Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        Timber.d("testEmergencyNow..");
        if (user!=null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userId", user.getObjectId());
            hashMap.put("installationId", ParseInstallation.getCurrentInstallation().getObjectId());
            ParseCloud.callFunctionInBackground("createTestAlarm", hashMap, (object, e) -> {
                if (e == null) {
                    Timber.d("Push sent. \nUserId=%s, \nInstallationId=%s, \nObjectId=%s", user.getObjectId(), ParseInstallation.getCurrentInstallation().getInstallationId(), ParseInstallation.getCurrentInstallation().getObjectId());
                    if (callback!=null) callback.onSuccess("testEmergencyNow", "success");
                } else {
                    Timber.e(e, "Error");
                    if (callback!=null) callback.onError("testEmergencyNow", e.getMessage());
                }
            });
        } else {
            if (callback!=null) callback.onError("testEmergencyNow", "not_activated");
        }
    }

    public List<Integer> getDutyOffDays(){
        ParseUser user = ParseUser.getCurrentUser();
        if (user.getList("dutyDays")!=null) {
            List<Integer> list = user.getList("dutyDays");
            Collections.sort(list);
            return list;
        } else {
            return new ArrayList<>();
        }
    }

    public void setDutyOffDays(List<Integer> list, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("dutyDays", list);
        saveInBackgroundCallback("setDutyOffDays", callback);
    }

    public String getDutyOffTimeFrom(){
        ParseUser user = ParseUser.getCurrentUser();
        Date date = user.getDate("dutyFrom");
        String result = "";
        if (date!=null) {
            result = String.format(Locale.GERMANY, "%02d:%02d", date.getHours(), date.getMinutes());
        }
        return result;
    }

    public String getDutyOffTimeTo(){
        ParseUser user = ParseUser.getCurrentUser();
        Date date = user.getDate("dutyTo");
        String result = "";
        if (date!=null) {
            result = String.format(Locale.GERMANY, "%02d:%02d", date.getHours(), date.getMinutes());
        }
        return result;
    }

    public void setDutyOffTimeFrom(Date date, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        if (date!=null) {
            user.put("dutyFrom", date);
        } else {
            user.put("dutyFrom", JSONObject.NULL);
        }
        saveInBackgroundCallback("setDutyOffTimeFrom", callback);
    }

    public void setDutyOffTimeTo(Date date, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        if (date!=null) {
            user.put("dutyTo", date);
        } else {
            user.put("dutyTo", JSONObject.NULL);
        }
        saveInBackgroundCallback("setDutyOffTimeTo", callback);
    }

    public boolean getDoNotNotifyMeAtHome(){
        ParseUser user = ParseUser.getCurrentUser();
        return user.getBoolean("dutyHome");
    }

    public void setDoNotNotifyMeAtHome(boolean doNotNotifyMeAtHome, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("dutyHome", doNotNotifyMeAtHome);
        saveInBackgroundCallback("setDoNotNotifyMeAtHome", callback);
    }

    public void deleteAccount(@Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        String email = user.getEmail();
        user.deleteInBackground(e -> {
            Timber.d("Trying to delete: %s \nDelete successful: %s", email, (e==null));
            if (e == null) {
                if (callback != null) callback.onSuccess("deleteAccount", "Account deleted!");
            } else {
                if (callback != null) callback.onError("deleteAccount", e.getMessage());
            }
        });
//        ParseUser.logOutInBackground(e -> {
//            if (e == null) {
//                Timber.d("User logged out!");
//            } else {
//                Timber.d("Failed to log out!");
//            }
//        });
    }

    public void logOut(){

    }

    public String getAlarmTone(){
        ParseUser user = ParseUser.getCurrentUser();
        String sound = user.getString("sound");
        if (sound!=null && !sound.isEmpty()) {
            return sound;
        } else {
            return "horn";
        }
    }

    public void setAlarmTone(String sound, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("sound", sound);
        saveInBackgroundCallback("sound", callback);
    }

    public boolean getReceiveTestAlarmOnSaturday(){
        ParseUser user = ParseUser.getCurrentUser();
        return user.getBoolean("receivesPracticeAlarm");
    }

    public void setReceiveTestAlarmOnSaturday(boolean receiveTestAlarmOnSaturday, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("receivesPracticeAlarm", receiveTestAlarmOnSaturday);
        saveInBackgroundCallback("receivesPracticeAlarm", callback);
    }

    private void saveInBackgroundCallback(String caller, @Nullable Callback callback){
        ParseUser user = ParseUser.getCurrentUser();
        user.saveInBackground(e -> {
            if (callback !=null) {
                if (e == null) {
                    callback.onSuccess(caller, "");
                } else {
                    callback.onError(caller, e.getMessage());
                }
            }
        });
    }
}
