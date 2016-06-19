package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ModifyAccountResponseChannel extends ServerChannelRegistry {

    private final static String TOPIC = "/user/modify_account/#";

    public ModifyAccountResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }
}
