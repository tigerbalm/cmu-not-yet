package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

abstract public class SubscribeChannel extends NotificationChannel {

    protected SubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final boolean notify(NetworkMessage message) {
        return false;
    }
}
