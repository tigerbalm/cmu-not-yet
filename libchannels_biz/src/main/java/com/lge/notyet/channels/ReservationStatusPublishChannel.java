package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.PublishChannel;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ReservationStatusPublishChannel extends PublishChannel {
    private static final String TOPIC = "/reservation/%d";
    private static final String KEY_EXPIRED = "available";
    private static final String KEY_PAID = "paid";

    private final int reservationId;

    public ReservationStatusPublishChannel(INetworkConnection networkConnection, int reservationId) {
        super(networkConnection);
        this.reservationId = reservationId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, reservationId).getValue());
    }

    public static String getControllerPhysicalId(Uri uri) {
        return (String) uri.getPathSegments().get(2);
    }

    public static boolean isExpired(NetworkMessage networkMessage) {
        JsonObject object = (JsonObject) networkMessage.getMessage();
        return object.get(KEY_EXPIRED) != null;
    }

    public static boolean isPaid(NetworkMessage networkMessage) {
        JsonObject object = (JsonObject) networkMessage.getMessage();
        return object.get(KEY_PAID) != null;
    }

    public static NetworkMessage createExpiredMessage(boolean updated) {
        JsonObject object = new JsonObject();
        object.add(KEY_EXPIRED, updated ? 1: 0);
        return new MqttNetworkMessage(object);
    }

    public static NetworkMessage createPaidMessage(boolean updated) {
        JsonObject object = new JsonObject();
        object.add(KEY_PAID, updated ? 1: 0);
        return new MqttNetworkMessage(object);
    }
}