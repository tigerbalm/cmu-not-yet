package com.lge.notyet.lib.comm;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class NetworkChannel implements IOnNotify, IOnRequest, IOnResponse, IOnTimeout {

    private final UUID mUuid;
    private INetworkConnection mNetworkConnection = null;
    private AtomicBoolean mOpen = null;

    protected NetworkChannel (INetworkConnection networkConnection) {
        mNetworkConnection = networkConnection;
        mOpen = new AtomicBoolean(false);
        mUuid = UUID.randomUUID();
    }

    public void listen() {
        if (mNetworkConnection.isConnected()) {
            mNetworkConnection.subscribe(this);
            mOpen.set(true);
        }
    }

    public void unlisten() {
        if (mNetworkConnection.isConnected()) {
            mNetworkConnection.unsubscribe(this);
            mOpen.set(false);
        }
    }

    protected INetworkConnection getNetworkConnection() {
        return mNetworkConnection;
    }

    public String getHashKey() {
        return getChannelDescription().toString() + "-" + mUuid.toString();
    }

    abstract public Uri getChannelDescription();
}
