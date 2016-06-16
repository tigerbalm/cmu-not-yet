package com.lge.notyet.lib.comm;

abstract public class RequestResponseChannel extends NetworkChannel {

    protected RequestResponseChannel (INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public boolean request(NetworkMessage message) {
        return getNetworkConnection().isConnected() && getNetworkConnection().request(this, message);
    }

    @Override
    public final void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
