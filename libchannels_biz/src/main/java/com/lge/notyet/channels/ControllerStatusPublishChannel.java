package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.PublishChannel;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ControllerStatusPublishChannel extends PublishChannel {
    private static final String TOPIC = "/controller/%s";
    private static final String KEY_AVAILABLE = "available";
    private static final String KEY_UPDATED = "updated";

    private final String controllerPhysicalId;

    public ControllerStatusPublishChannel(INetworkConnection networkConnection, String controllerPhysicalId) {
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

    public static boolean isUpdated(NetworkMessage networkMessage) {
        JsonObject object = (JsonObject) networkMessage.getMessage();
        return object.get(KEY_UPDATED) != null && (object.get(KEY_UPDATED).asInt() == 1);
    }

    public static boolean isAvailable(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_AVAILABLE).asInt() == 1;
    }

    public static NetworkMessage createUpdatedMessage(boolean updated) {
        JsonObject object = new JsonObject();
        object.add(KEY_UPDATED, updated ? 1: 0);
        return new MqttNetworkMessage(object);
    }
}
