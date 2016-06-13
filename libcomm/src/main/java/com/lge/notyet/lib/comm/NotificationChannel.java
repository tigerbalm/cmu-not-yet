package com.lge.notyet.lib.comm;

abstract public class NotificationChannel extends NetworkChannel {

    protected NotificationChannel (INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public void notify(NetworkMessage message) {

        if (!getNetworkConnection().isConnected()) return;
        getNetworkConnection().send(this, message);
    }

    @Override
    public final void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
    }

    @Override
    public final void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
    }

    @Override
    public final void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
    }
}
