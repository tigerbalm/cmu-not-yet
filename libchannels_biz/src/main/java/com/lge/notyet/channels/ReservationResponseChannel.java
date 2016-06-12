package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.*;

public class ReservationResponseChannel extends ResponseChannel {

    protected ReservationResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return null;
    }

    @Override
    public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

    }
}
