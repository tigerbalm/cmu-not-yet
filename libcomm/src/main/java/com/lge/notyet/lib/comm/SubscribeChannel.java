package com.lge.notyet.lib.comm;

abstract public class SubscribeChannel extends NotificationChannel {

    protected SubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void notify(NetworkMessage message) {

    }
}
