package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;
import com.sun.javafx.binding.StringFormatter;

public class GetSlotsRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facility/%d/slots/get";
    private final static String KEY_SESSION_KEY = "session_key";

    private int facilityId;

    public GetSlotsRequestChannel(INetworkConnection networkConnection, int facilityId) {
        super(networkConnection);
        this.facilityId = facilityId;
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(StringFormatter.format(TOPIC, facilityId).getValue());
    }

    public static final int getFacilityId(Uri uri) {
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
