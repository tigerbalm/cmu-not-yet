package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

/**
 * Created by beney.kim on 2016-06-17.
 */
public class ModifyAccountResponseChannel extends ServerChannelRegistry {

    private final static String TOPIC = "/modifyAccount/#";

    public ModifyAccountResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }
}
