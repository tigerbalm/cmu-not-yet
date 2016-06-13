package com.lge.notyet.lib.comm;

import java.util.Arrays;
import java.util.List;

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

    public final List<String> getPathSegments() {
        return Arrays.asList(mLocation.toString().split("/"));
    }

    // Need to implement, because in pub/sub pattern, it may be wildcard presentation.
    abstract public boolean isSuperOf(Uri arg);
}
