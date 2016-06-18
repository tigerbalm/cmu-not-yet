package com.lge.notyet.channels;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.PublishChannel;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

import java.util.ArrayList;
import java.util.List;

public class UpdateControllerStatusPublishChannel extends PublishChannel {
    private static final String TOPIC = "/controller/%s";
    private static final String KEY_AVAILABLE = "available";
    private static final String KEY_SLOTS = "slots";

    private final String controllerPhysicalId;

    public UpdateControllerStatusPublishChannel(INetworkConnection networkConnection, String controllerPhysicalId) {
        super(networkConnection);
        this.controllerPhysicalId = controllerPhysicalId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, controllerPhysicalId).getValue());
    }

    public static String getControllerPhysicalId(Uri uri) {
        return (String) uri.getPathSegments().get(2);
    }

    public static boolean isAvailable(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_AVAILABLE).asInt() == 1;
    }

    public static List<JsonObject> getSlots(NetworkMessage networkMessage) {
        JsonArray slotArray = ((JsonObject) networkMessage.getMessage()).get(KEY_SLOTS).asArray();
        List<JsonObject> slotList = new ArrayList<>();
        slotArray.forEach(slotObject -> slotList.add(slotObject.asObject()));
        return slotList;
    }
}
