package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class GetFacilitiesRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facilities/get";
    private final static String KEY_SESSION_KEY = "session_key";

    public GetFacilitiesRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
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
