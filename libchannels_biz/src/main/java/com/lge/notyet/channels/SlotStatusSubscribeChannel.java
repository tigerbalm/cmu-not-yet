package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class SlotStatusSubscribeChannel extends SubscribeChannelRegistry {
    private static final String TOPIC = "/controller/+/slot/+";
    private static final String TOPIC_WITH_PHYSICAL_ID = "/controller/%d/slot/#";

    private final int controllerPhysicalId;

    public SlotStatusSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
        controllerPhysicalId = -1;
    }

    public SlotStatusSubscribeChannel(INetworkConnection networkConnection, int controllerPhysicalId) {
        super(networkConnection);
        this.controllerPhysicalId = controllerPhysicalId;
    }

    @Override
    public Uri getChannelDescription() {
        if (controllerPhysicalId != -1) {
            return new MqttUri(StringFormatter.format(TOPIC_WITH_PHYSICAL_ID, controllerPhysicalId).getValue());
        }
        return new MqttUri(TOPIC);
    }
}
