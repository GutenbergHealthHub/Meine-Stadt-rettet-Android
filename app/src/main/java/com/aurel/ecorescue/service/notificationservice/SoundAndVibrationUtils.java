package com.aurel.ecorescue.service.notificationservice;

import android.net.Uri;
import com.aurel.ecorescue.BuildConfig;
import com.aurel.ecorescue.R;
import java.util.HashMap;
import java.util.Map;

public class SoundAndVibrationUtils {


    public static Map<String, Integer> sSoundRessourceMap = new HashMap<>();

    static {
        sSoundRessourceMap.put("horn", R.raw.horn);
        sSoundRessourceMap.put("siren", R.raw.siren);
        sSoundRessourceMap.put("alarm", R.raw.alarm);
        sSoundRessourceMap.put("alarm2", R.raw.alarm2);
        sSoundRessourceMap.put("alarm3", R.raw.alarm3);
        sSoundRessourceMap.put("alarm4", R.raw.alarm4);
        sSoundRessourceMap.put("alarm5", R.raw.alarm5);
        sSoundRessourceMap.put("alarm6", R.raw.alarm6);
        sSoundRessourceMap.put("alarm7", R.raw.alarm7);
        sSoundRessourceMap.put("alarm8", R.raw.alarm8);
    }

    public static Uri getSoundRessourceUriByName(String soundName) {
        return Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + getSoundRessourceByName(soundName));
    }

    private static int getSoundRessourceByName(String soundName) {
        if(soundName != null) {
            Integer ressource = sSoundRessourceMap.get(soundName.toLowerCase());
            if (ressource != null) {
                return ressource.intValue();
            }
        }
        return R.raw.horn;
    }
}
