package com.lge.notyet.lib.comm;

abstract public class ClientChannel extends RequestResponseChannel {

    protected ClientChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
