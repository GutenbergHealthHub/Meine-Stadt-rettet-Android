package com.aurel.ecorescue.utils;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class OffDutyUtils {



    public static boolean isInOffDutyHours(String h1, String h2){
        if (h1==null || h2==null) return false;
        if (h1.isEmpty() || h2.isEmpty()) return false;
        int startTime = Integer.parseInt(h1.replace(":", ""));
        int finishTime = Integer.parseInt(h2.replace(":", ""));
        Date date = new Date();
        int currentHours = date.getHours() * 100 + date.getMinutes();
        Timber.d("Start: %s, Finish: %s, Current: %s", startTime, finishTime, currentHours);

        if (finishTime>=startTime) {
            return startTime <= currentHours && currentHours < finishTime;
        } else {
            // startTime (2300) > finishTime (0700), currentHours (0500)
            return startTime >= currentHours && currentHours<=finishTime;
        }

    }

    public static boolean isInOffDutyDays(List<Integer> days){
        Date date = new Date();
        int currentDay = date.getDay();
        if (currentDay==0) currentDay = 7;
        return days.contains(currentDay);
    }
}
