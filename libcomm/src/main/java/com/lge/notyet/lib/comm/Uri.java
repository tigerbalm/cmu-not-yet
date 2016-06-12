package com.lge.notyet.lib.comm;

abstract public class Uri {

    private String mPath = null;

    protected Uri(String path) {
        mPath = path;
    }
    public String getPath() {
        return mPath;
    }
    public String toString() {
        return mPath;
    }
    abstract public boolean isSuperOf(Uri arg);
}
