package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class LoginRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/login";

    public LoginRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static final String getEmail(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("email").asString();
    }

    public static final String getPassword(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("password").asString();
    }
}
