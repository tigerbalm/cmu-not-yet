package com.lge.notyet.lib.comm;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class NetworkChannel implements IOnNotify, IOnRequest, IOnResponse, IOnTimeout {

    private final UUID mUuid;
    private final INetworkConnection mNetworkConnection;
    private final AtomicBoolean mOpen;

    protected NetworkChannel (INetworkConnection networkConnection) {
        mUuid = UUID.randomUUID();
        mNetworkConnection = networkConnection;
        mOpen = new AtomicBoolean(false);
    }

    public void listen() {
        if (mNetworkConnection.isConnected() && !mOpen.get()) {
            mNetworkConnection.subscribe(this);
            mOpen.set(true);
        }
    }

    public void unlisten() {
        if (mNetworkConnection.isConnected() && mOpen.get()) {
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
