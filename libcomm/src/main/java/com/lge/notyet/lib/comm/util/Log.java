package com.lge.notyet.lib.comm.util;

/**
 * Created by beney.kim on 2016-06-09.
 */
public class Log {

    private static final boolean LOGV = false;

    public static void logd(String tag, String log) {
        System.out.println(System.currentTimeMillis() + " [" + tag + "] " + log);
    }

    public static void logv(String tag, String log) {
        if (LOGV == false) return;
        System.out.println(System.currentTimeMillis() + "[" + tag + "] " + log);
    }
}
