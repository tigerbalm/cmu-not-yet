package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class MakeReservationResponseChannel extends ServerChannelRegistry {
    private final static String mTopic = "/facility/+/make_reservation/#";

    public MakeReservationResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(mTopic);
    }
}
