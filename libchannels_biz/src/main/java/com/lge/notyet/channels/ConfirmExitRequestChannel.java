package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ConfirmExitRequestChannel extends ClientChannelRegistry {
    private static final String TOPIC = "/controller/%s/confirm_exit";
    private static final String KEY_SLOT_NUMBER = "slot_no";

    private final String controllerPhysicalId;

    public ConfirmExitRequestChannel(INetworkConnection networkConnection, String controllerPhysicalId) {
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

    public static int getSlotNumber(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_SLOT_NUMBER).asInt();
    }

    public static MqttNetworkMessage createRequestMessage(int slotNumber) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_SLOT_NUMBER, slotNumber);
        return new MqttNetworkMessage(requestObject);
    }
}
