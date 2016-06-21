package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class ReservationRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facility/%d/reservation";

    private int facilityId = 0;

    public ReservationRequestChannel(INetworkConnection networkConnection, int facilityId) {
        super(networkConnection);
        this.facilityId = facilityId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, facilityId).getValue());
    }

    public static int getFacilityId(Uri uri) {
        return Integer.parseInt((String) uri.getPathSegments().get(2));
    }

    public static String getSessionKey(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("session_key").asString();
    }

    public static int getReservationTs(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("reservation_ts").asInt();
    }

    public static MqttNetworkMessage createRequestMessage(String sessionKey, long reservationTimestamp) {
        JsonObject requestObject = new JsonObject();
        requestObject.add("session_key", sessionKey);
        requestObject.add("reservation_ts", reservationTimestamp);
        return new MqttNetworkMessage(requestObject);
    }
}