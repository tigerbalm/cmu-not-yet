package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.ServerChannelRegistry;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class LoginResponseChannel extends ServerChannelRegistry {
    private final static String TOPIC = "/login/#";

    public LoginResponseChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static JsonObject createResponseObject(int userId, String cardNumber, String cardExpiration, String sessionKey) {
        JsonObject responseObject = new JsonObject();
        responseObject.add("id", userId);
        if (cardNumber != null) {
            responseObject.add("card_number", cardNumber);
        }
        if (cardExpiration != null) {
            responseObject.add("card_expiration", cardExpiration);
        }
        responseObject.add("session_key", sessionKey);
        return responseObject;
    }
}
