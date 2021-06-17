package com.aurel.ecorescue.x_tools;

import android.widget.DatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by daniel on 6/16/17.
 */

public class x_DateFormatter {
    public static DateFormat GetStandardDateFormatter(){
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm");
    }

    public static DateFormat GetSimpleDateFormatter(){
        return new SimpleDateFormat("dd.MM.yyyy");
    }

    public static Date GetDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        return calendar.getTime();
    }
}
