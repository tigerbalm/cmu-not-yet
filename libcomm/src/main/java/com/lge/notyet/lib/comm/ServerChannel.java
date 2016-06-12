package com.lge.notyet.lib.comm;

abstract public class ServerChannel extends RequestResponseChannel  {

    protected ServerChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void request(NetworkMessage message) {

    }

    @Override
    public final void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {

    }

    @Override
    public final void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
