package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

abstract public class RequestResponseChannel extends NetworkChannel {

    private static final String LOG_TAG = "R-Channel";

    protected RequestResponseChannel (INetworkConnection networkConnection) {
        super(networkConnection);
    }

    public boolean request(NetworkMessage message) {

        if (getNetworkConnection().isConnected() == false) return false;
        getNetworkConnection().request(this, message);
        return true;
    }

    @Override
    public final void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
