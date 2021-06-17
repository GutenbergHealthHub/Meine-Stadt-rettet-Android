package com.aurel.ecorescue.utils;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.aurel.ecorescue.R;

import java.util.HashMap;

public class SoundUtils {

    private static final HashMap<String, String> SOUND_NAMES = new HashMap<String, String>(){{
        put("horn", "Horn");
        put("siren", "Siren");
        put("alarm", "Alarm");
        put("alarm2", "Alarm 2");
        put("alarm3", "Alarm 3");
        put("alarm4", "Alarm 4");
        put("alarm5", "Alarm 5");
        put("alarm6", "Alarm 6");
        put("alarm7", "Alarm 7");
        put("alarm8", "Alarm 8");
    }};

    private static final HashMap<String, Integer> SOUND_RESOURCES = new HashMap<String, Integer>(){{
        put("horn", R.raw.horn);
        put("siren", R.raw.siren);
        put("alarm", R.raw.alarm);
        put("alarm2", R.raw.alarm2);
        put("alarm3", R.raw.alarm3);
        put("alarm4", R.raw.alarm4);
        put("alarm5", R.raw.alarm5);
        put("alarm6", R.raw.alarm6);
        put("alarm7", R.raw.alarm7);
        put("alarm8", R.raw.alarm8);
    }};


    public static String convertToServerSoundName(@NonNull String readableSoundName){
        return readableSoundName.toLowerCase().replace(" ", "");
    }

    public static String convertToReadableSoundName(String serverSoundName){
        return SOUND_NAMES.get(serverSoundName);
    }

    public static String[] getReadableSoundNames(){
        return new String[]{"Horn", "Siren", "Alarm", "Alarm 2", "Alarm 3", "Alarm 4", "Alarm 5", "Alarm 6", "Alarm 7", "Alarm 8"};
    }


    private MediaPlayer mPlayer;
    private Context mContext;

    public SoundUtils(Context context){
        this.mContext = context;
        mPlayer = new MediaPlayer();
    }

    public void playSound(String sound, boolean loop) {
        int resId = getResId(sound);
        mPlayer = MediaPlayer.create(mContext, resId);
        mPlayer.setLooping(loop);
        mPlayer.start();
    }

    public void stopSound(){
        if (mPlayer!=null) {
            mPlayer.stop();
        }
    }

    private int getResId(String sound) {
        switch (sound){
            case "siren":
                return R.raw.siren;
            case "alarm":
                return R.raw.alarm;
            case "alarm2":
                return R.raw.alarm2;
            case "alarm3":
                return R.raw.alarm3;
            case "alarm4":
                return R.raw.alarm4;
            case "alarm5":
                return R.raw.alarm5;
            case "alarm6":
                return R.raw.alarm6;
            case "alarm7":
                return R.raw.alarm7;
            case "alarm8":
                return R.raw.alarm8;
            default:
                return R.raw.horn;
        }
    }
}
