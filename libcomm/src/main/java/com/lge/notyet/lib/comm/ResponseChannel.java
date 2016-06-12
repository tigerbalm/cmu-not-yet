package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-12.
 */

abstract public class ResponseChannel extends RequestResponseChannel  {

    protected ResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public final boolean request(NetworkMessage message) {
        return false;
    }

    @Override
    public final void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {

    }

    @Override
    public final void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
