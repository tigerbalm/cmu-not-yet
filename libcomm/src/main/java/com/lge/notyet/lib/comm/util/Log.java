package com.lge.notyet.lib.comm.util;

public class Log {

    private static final boolean LOGV = false;

    public static void logd(String tag, String log) {
        System.out.println(System.currentTimeMillis() + " [" + tag + "] " + log);
    }

    public static void logv(String tag, String log) {
        if (!LOGV) return;
        System.out.println(System.currentTimeMillis() + " [" + tag + "] " + log);
    }
}
