package com.aurel.ecorescue.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public String getReadableDate(Date date){
        if (date == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%02d.%02d.%4d", day, month, year);
    }
}
