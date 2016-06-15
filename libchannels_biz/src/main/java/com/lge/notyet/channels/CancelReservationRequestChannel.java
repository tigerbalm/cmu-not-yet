package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class CancelReservationRequestChannel extends ClientChannelRegistry {
    private static final String TOPIC = "/reservation/%d/cancel";
    private static final String KEY_SESSION_KEY = "session_key";

    private final int reservationId;

    public CancelReservationRequestChannel(INetworkConnection networkConnection, int reservationId) {
        super(networkConnection);
        this.reservationId = reservationId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, reservationId).getValue());
    }

    public static final int getReservationId(Uri uri) {
        return Integer.parseInt((String) uri.getPathSegments().get(2));
    }

    public static String getSessionKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_SESSION_KEY).asString();
    }

    public static MqttNetworkMessage createRequestMessage(String sessionKey) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_SESSION_KEY, sessionKey);
        return new MqttNetworkMessage(requestObject);
    }
}
