package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.PublishChannel;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class UpdateSlotStatusPublishChannel extends PublishChannel {
    private static final String TOPIC = "/controller/%s/slot/%d";
    private static final String KEY_OCCUPIED = "occupied";

    private final String physicalId;
    private final int slotId;

    public UpdateSlotStatusPublishChannel(INetworkConnection networkConnection, String physicalId, int slotId) {
        super(networkConnection);
        this.physicalId = physicalId;
        this.slotId = slotId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, physicalId, slotId).getValue());
    }

    public static String getControllerPhysicalId(Uri uri) {
        return (String) uri.getPathSegments().get(2);
    }

    public static int getSlotNumber(Uri uri) {
        return Integer.parseInt((String) uri.getPathSegments().get(4));
    }

    public static boolean isOccupied(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_OCCUPIED).asInt() == 1;
    }
}
