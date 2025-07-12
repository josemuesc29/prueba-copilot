package com.imaginamos.farmatodo.backend.order;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateConstants {
    public static final TimeZone COLOMBIA_TIMEZONE = TimeZone.getTimeZone("America/Bogota");
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT;

    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        SIMPLE_DATE_FORMAT.setTimeZone(COLOMBIA_TIMEZONE);
    }
}
