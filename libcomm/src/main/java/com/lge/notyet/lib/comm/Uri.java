package com.lge.notyet.lib.comm;

abstract public class Uri<T> {

    private T mLocation = null;

    protected Uri(T location) {
        mLocation = location;
    }

    public final T getLocation() {
        return mLocation;
    }

    public final String toString() {
        return mLocation.toString();
    }

    // Need to implement, because in pub/sub pattern, it may be wildcard presentation.
    abstract public boolean isSuperOf(Uri arg);
}
