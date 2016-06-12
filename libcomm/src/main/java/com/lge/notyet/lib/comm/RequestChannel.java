package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

abstract public class RequestChannel extends RequestResponseChannel {

    protected RequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
