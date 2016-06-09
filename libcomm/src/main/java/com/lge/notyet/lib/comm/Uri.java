package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */
public class Uri {

    public final static String ALL = "#";
    public final static String ROOT = "/";

    private String mPath;

    public Uri(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }
}
