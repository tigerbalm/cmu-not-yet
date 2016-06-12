package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */
abstract public class Uri {

    private String mPath = null;

    public Uri(String path) {
        mPath = path;
    }
    public String getPath() {
        return mPath;
    }
    protected void setPath(String path) {
        mPath = path;
    }

    public String toString() {
        return mPath;
    }

    abstract public boolean isSuperOf(Uri arg);
}
