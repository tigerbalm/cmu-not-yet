package com.lge.notyet.attendant.util;

public class Log {

    private static final boolean LOGV = false;

    public static void log(String tag, String log) {
        logd(tag, log);
    }

    public static void logd(String tag, String log) {
        System.out.println(System.currentTimeMillis() + " [" + tag + "] " + log);
    }

    public static void logv(String tag, String log) {
        if (!LOGV) return;
        System.out.println(System.currentTimeMillis() + " [" + tag + "] " + log);
    }
}
