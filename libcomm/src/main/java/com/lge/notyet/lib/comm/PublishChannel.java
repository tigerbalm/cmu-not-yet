package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

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
    public final void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
