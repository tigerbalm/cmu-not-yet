package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ConfirmReservationRequestChannel extends ClientChannelRegistry {
    private static final String TOPIC = "/controller/%s/confirm_reservation";
    private static final String KEY_CONFIRMATION_NUMBER = "confirmation_no";

    private final String controllerPhysicalId;

    public ConfirmReservationRequestChannel(INetworkConnection networkConnection, String controllerPhysicalId) {
        super(networkConnection);
        this.controllerPhysicalId = controllerPhysicalId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, controllerPhysicalId).getValue());
    }

    public static String getControllerPhysicalId(Uri uri) {
        return (String) uri.getPathSegments().get(1);
    }

    public static int getConfirmationNumber(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_CONFIRMATION_NUMBER).asInt();
    }

    public static MqttNetworkMessage createRequestMessage(int confirmationNumber) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_CONFIRMATION_NUMBER, confirmationNumber);
        return new MqttNetworkMessage(requestObject);
    }
}
