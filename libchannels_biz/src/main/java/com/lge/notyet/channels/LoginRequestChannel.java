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
    private final static String KEY_EMAIL = "email";
    private final static String KEY_PASSWORD = "password";

    public LoginRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static String getEmail(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("email").asString();
    }

    public static String getPassword(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get("password").asString();
    }

    public static MqttNetworkMessage createRequestMessage(String email, String password) {
        JsonObject requestObject = new JsonObject().add(KEY_EMAIL, email).add(KEY_PASSWORD, password);
        return new MqttNetworkMessage(requestObject);
    }
}
