package com.lge.notyet.lib.comm;

abstract public class RequestResponseChannel extends NetworkChannel {

    protected RequestResponseChannel (INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public void request(NetworkMessage message) {

        if (!getNetworkConnection().isConnected()) return;
        getNetworkConnection().request(this, message);
    }

    @Override
    public final void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
