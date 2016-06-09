package com.lge.notyet.lib.comm;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.lib.comm.util.Log;

import org.eclipse.paho.client.mqttv3.*;

/**
 * Created by beney.kim on 2016-06-10.
 */
public class MqttNetworkMessage extends NetworkMessage {

    private MqttAsyncClient mResponseNetworkChannel = null;
    private String mResponseTopic = null;

    private MqttNetworkMessage(int messageType, JsonObject message) {
        super(messageType, message);
    }

    public static MqttNetworkMessage buildMessage(JsonObject message) {
        return new MqttNetworkMessage(MESSAGE_TYPE_NOTIFICATION, message);
    }

    public static MqttNetworkMessage buildRequest(JsonObject message) {
        return new MqttNetworkMessage(MESSAGE_TYPE_REQUEST, message);
    }

    protected MqttNetworkMessage buildResponse(JsonObject message) {
        return new MqttNetworkMessage(MESSAGE_TYPE_RESPONSE, message);
    }

    static MqttNetworkMessage build(int messageType, JsonObject message) {
        return new MqttNetworkMessage(messageType, message);
    }

    public void setResponseTopic(MqttAsyncClient nc, String responseTopic) {
        mResponseNetworkChannel = nc;
        mResponseTopic = responseTopic;
    }

    public byte[] getBytes() {
        return getMessage().toString().getBytes();
    }

    @Override
    protected void response_impl(JsonObject message) {


        if (mResponseNetworkChannel == null) {
            // error
        }

        MqttNetworkMessage mqttNetworkMessage = buildResponse(message);
        mqttNetworkMessage.addMessageType(MESSAGE_TYPE_RESPONSE);

        try {
            mResponseNetworkChannel.publish(mResponseTopic, new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
        }
    }

    // Utility Function to support request-response message flow.
    void addMessageType (int messageType) {
        JsonObject messageObj = getMessage();
        JsonValue typeValue = messageObj.get(MSG_TYPE);
        if (typeValue == null) {
            messageObj.add(MSG_TYPE, messageType);
        }
    }

    void removeMessageType () {
        getMessage().remove(MSG_TYPE);
    }
}
