package com.lge.notyet.lib.comm;

abstract public class RequestChannel extends RequestResponseChannel {

    protected RequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
