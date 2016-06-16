package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

public class SignUpRequestChannel extends ClientChannelRegistry {
    private final static String TOPIC = "/user/sign_up";
    private final static String KEY_EMAIL = "email";
    private final static String KEY_PASSWORD = "password";
    private final static String KEY_CARD_NUMBER = "card_number";
    private final static String KEY_CARD_EXPIRATION = "card_expiration";

    public SignUpRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static final String getEmail(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_EMAIL).asString();
    }

    public static final String getPassword(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_PASSWORD).asString();
    }

    public static final String getCardNumber(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_CARD_NUMBER).asString();
    }

    public static final String getCardExpiration(NetworkMessage networkMessage) {
        return ((JsonObject) networkMessage.getMessage()).get(KEY_CARD_EXPIRATION).asString();
    }

    public static MqttNetworkMessage createRequestMessage(String email, String password, String cardNumber, String cardExpiration) {
        JsonObject requestObject = new JsonObject()
                .add(KEY_EMAIL, email).add(KEY_PASSWORD, password)
                .add(KEY_CARD_NUMBER, cardNumber).add(KEY_CARD_EXPIRATION, cardExpiration);
        return new MqttNetworkMessage(requestObject);
    }
}