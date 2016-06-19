package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class UpdateControllerStatusSubscribeChannel extends SubscribeChannelRegistry {
    private static final String TOPIC = "/controller/+";
    private static final String TOPIC_WITH_PHYSICAL_ID = "/controller/%d";

    private final int controllerPhysicalId;

    public UpdateControllerStatusSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
        controllerPhysicalId = -1;
    }

    public UpdateControllerStatusSubscribeChannel(INetworkConnection networkConnection, int controllerId) {
        super(networkConnection);
        controllerPhysicalId = controllerId;
    }

    @Override
    public Uri getChannelDescription() {
        if (controllerPhysicalId != -1) {
            return new MqttUri(StringFormatter.format(TOPIC_WITH_PHYSICAL_ID, controllerPhysicalId).getValue());
        }
        return new MqttUri(TOPIC);
    }
}
