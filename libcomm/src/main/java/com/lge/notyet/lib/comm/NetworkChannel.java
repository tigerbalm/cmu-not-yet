package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class NetworkChannel {

    private UUID mUuid;
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

    public boolean isOpen() {
        return mNetworkConnection.isConnected() && mOpen.get();
    }

    protected INetworkConnection getNetworkConnection() {
        return mNetworkConnection;
    }

    public String getHashKey() {
        return getChannelDescription().toString() + mUuid.toString();
    }

    abstract public Uri getChannelDescription();

    abstract public void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
    abstract public void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
    abstract public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message);
    abstract public void onTimeout(NetworkChannel networkChannel, NetworkMessage message);
}
