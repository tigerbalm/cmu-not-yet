package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

abstract public class NotificationChannel extends NetworkChannel {

    private static final String LOG_TAG = "N-Channel";

    protected NotificationChannel (INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public boolean notify(NetworkMessage message) {

        if (!getNetworkConnection().isConnected()) return false;
        getNetworkConnection().send(this, message);
        return true;
    }

    @Override
    public final void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
    }

    @Override
    public final void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
    }

    @Override
    public final void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
    }
}
