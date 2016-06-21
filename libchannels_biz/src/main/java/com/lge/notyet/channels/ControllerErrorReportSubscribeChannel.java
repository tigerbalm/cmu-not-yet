package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ControllerErrorReportSubscribeChannel extends SubscribeChannelRegistry {

    private static final String TOPIC = "/controller/+/error";  // %d is controller physical id

    public ControllerErrorReportSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }
}
