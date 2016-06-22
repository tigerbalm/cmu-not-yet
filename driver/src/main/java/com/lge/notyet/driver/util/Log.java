package com.lge.notyet.driver.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Log {

    private static final boolean LOGV = false;

    public static void log(String tag, String log) {
        logd(tag, log);
    }

    public static void logd(String tag, String log) {

        Calendar reservedTime = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.sss, MM/dd/yyyy z");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String reservedTimeString = sdf.format(reservedTime.getTime());

        System.out.println(reservedTimeString + " [" + tag + "] " + log);
    }

    public static void logv(String tag, String log) {

        if (!LOGV) {

            Calendar reservedTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.sss, MM/dd/yyyy z");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            String reservedTimeString = sdf.format(reservedTime.getTime());

            System.out.println(reservedTimeString + " [" + tag + "] " + log);
        }
    }
}
