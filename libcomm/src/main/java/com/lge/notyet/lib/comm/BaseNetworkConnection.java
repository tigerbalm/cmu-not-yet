package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-11.
 * This class made for RedundancyConnection, because I want to make limitation that RedundancyConnection can receive only this type as parameter of Constructor (Not all INetworkConnection).
 * Also, to support decorated pattern, it needs to hook listener.
 */

abstract public class BaseNetworkConnection extends INetworkConnection {

    // Internal Variables
    protected String mName = null;
    protected INetworkCallback mNetworkCallback = null;

    protected static final int REQUEST_MESSAGE_PENDING_TIME = 5;

    protected BaseNetworkConnection(String name) {
        mName = name;
    }

    synchronized INetworkCallback getNetworkCallback() {
        return mNetworkCallback;
    }

    synchronized INetworkCallback hookNetworkCallback(INetworkCallback networkCallback) {
        INetworkCallback ret = mNetworkCallback;
        mNetworkCallback = networkCallback;
        return ret;
    }
}
