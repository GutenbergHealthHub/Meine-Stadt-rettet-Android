package com.aurel.ecorescue.data;

import com.aurel.ecorescue.utils.AppExecutors;
import com.parse.ParseException;
import com.parse.ParseUser;

public class SpontaneousHelperRepository {

    private static SpontaneousHelperRepository sInstance;

    public static SpontaneousHelperRepository getInstance() {
        if (sInstance == null) {
            synchronized (SpontaneousHelperRepository.class) {
                if (sInstance == null) {
                    sInstance = new SpontaneousHelperRepository();
                }
            }
        }
        return sInstance;
    }

    private SpontaneousHelperRepository(){
        AppExecutors appExecutors = new AppExecutors();
        appExecutors.networkIO().execute(() -> {
            try {
                ParseUser.getCurrentUser().fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isElderlyCareNotificationEnabled(){
        return ParseUser.getCurrentUser().getBoolean("sp_elderly");
    }
    public boolean isSupportForAmbulanceServiceNotificationEnabled(){
        return ParseUser.getCurrentUser().getBoolean("sp_ambulance");
    }
    public boolean isPhysicallyDifficultTaskNotificationEnabled(){
        return ParseUser.getCurrentUser().getBoolean("sp_physical");
    }
    public boolean isGeneralSpontaneousAssistanceNotificationEnabled(){
        return ParseUser.getCurrentUser().getBoolean("sp_general");
    }

    public void setElderlyCareNotification(boolean enabled) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("sp_elderly", enabled);
        user.saveInBackground();
    }
    public void setSupportForAmbulanceServiceNotification(boolean enabled) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("sp_ambulance", enabled);
        user.saveInBackground();
    }
    public void setPhysicallyDifficultTaskNotification(boolean enabled) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("sp_physical", enabled);
        user.saveInBackground();
    }
    public void setGeneralSpontaneousAssistanceNotification(boolean enabled) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("sp_general", enabled);
        user.saveInBackground();
    }

}
