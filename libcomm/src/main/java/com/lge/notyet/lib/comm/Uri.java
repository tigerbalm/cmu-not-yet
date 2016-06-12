package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */
public class Uri {

    private String mPath = null;

    public Uri(String path) {
        mPath = path;
    }
    public String getPath() {
        return mPath;
    }

    public String toString() {
        return mPath;
    }
}
