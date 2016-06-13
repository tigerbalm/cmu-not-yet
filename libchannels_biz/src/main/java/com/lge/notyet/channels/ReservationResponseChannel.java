package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.*;

public class ReservationResponseChannel extends ServerChannelRegistry {

    protected ReservationResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return null;
    }
}
