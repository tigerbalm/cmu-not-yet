package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class ReservableFacilitiesRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/facility/reservable_list";
    private final static String KEY_SESSION_KEY = "session_key";

    public ReservableFacilitiesRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static MqttNetworkMessage createRequestMessage(String sessionKey) {
        JsonObject requestObject = new JsonObject();
        requestObject.add(KEY_SESSION_KEY, sessionKey);
        return new MqttNetworkMessage(requestObject);
    }
}