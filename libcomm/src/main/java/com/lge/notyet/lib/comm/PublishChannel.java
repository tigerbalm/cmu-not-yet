package com.lge.notyet.lib.comm;

abstract public class PublishChannel extends NotificationChannel {

    protected PublishChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void listen() {
    }

    @Override
    public final void unlisten() {
    }

    @Override
    public final void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
