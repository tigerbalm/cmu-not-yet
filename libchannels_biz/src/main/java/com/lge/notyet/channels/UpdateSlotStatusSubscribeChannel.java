package com.lge.notyet.channels;

import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.SubscribeChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class UpdateSlotStatusSubscribeChannel extends SubscribeChannelRegistry {
    private static final String TOPIC = "/controller/+/slot/+";
    private static final String TOPIC_WITH_PHYSICAL_ID = "/controller/%d/slot/+";

    private final int mControllerId;

    public UpdateSlotStatusSubscribeChannel(INetworkConnection networkConnection) {
        super(networkConnection);
        mControllerId = -1;
    }

    public UpdateSlotStatusSubscribeChannel(INetworkConnection networkConnection, int controllerId) {
        super(networkConnection);
        mControllerId = controllerId;
    }

    @Override
    public Uri getChannelDescription() {

        if (mControllerId != -1) {
            return new MqttUri(StringFormatter.format(TOPIC_WITH_PHYSICAL_ID, mControllerId).getValue());
        }
        return new MqttUri(TOPIC);
    }
}
