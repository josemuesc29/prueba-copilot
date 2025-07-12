package com.imaginamos.farmatodo.model.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil {

    public static Date getDate(){
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"));
        calendar.setTime(new Date());
        return calendar.getTime();
    }

    public static Date addMinutesToDate(Date date, final int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

}
