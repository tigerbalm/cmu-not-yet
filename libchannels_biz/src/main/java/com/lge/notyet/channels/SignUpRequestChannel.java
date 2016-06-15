package com.lge.notyet.channels;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.ClientChannelRegistry;
import com.lge.notyet.lib.comm.INetworkConnection;
import com.lge.notyet.lib.comm.Uri;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.lib.comm.mqtt.MqttUri;

/**
 * Created by beney.kim on 2016-06-15.
 */
public class SignUpRequestChannel extends ClientChannelRegistry {

    private final static String TOPIC = "/signup";

    public SignUpRequestChannel(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    @Override
    public Uri getChannelDescription() {
        return new MqttUri(TOPIC);
    }

    public static MqttNetworkMessage createRequestMessage(String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc) {
        JsonObject requestObject = new JsonObject()
                .add("user_email", userEmailAddress).add("password", passWord)
                .add("card_number", creditCardNumber).add("card_expiration", creditCardExpireDate).add("card_cvc", creditCardCvc);
        return new MqttNetworkMessage(requestObject);
    }
}
