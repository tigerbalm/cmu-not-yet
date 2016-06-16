package com.lge.notyet.lib.comm.mqtt;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.lib.comm.NetworkMessage;
import org.eclipse.paho.client.mqttv3.*;

public class MqttNetworkMessage extends NetworkMessage <JsonObject> {

    private MqttAsyncClient mResponseNetworkConnection = null;
    private String mResponseTopic = null;

    public MqttNetworkMessage(JsonObject message) {
        super(message);
    }

    public static MqttNetworkMessage build(JsonObject message) {
        return new MqttNetworkMessage(message);
    }

    void makeResponseInfo(MqttAsyncClient nc, String topic) throws UnsupportedOperationException {

        if (!topic.contains(MqttConstants.REQUEST_MESSAGE_TOPIC)) {
           // throw new UnsupportedOperationException("this request type message has wrong topic, topic=" + topic);
        }

        String responseTopic = new String(topic);
        responseTopic = responseTopic.replace(MqttConstants.REQUEST_MESSAGE_TOPIC, MqttConstants.RESPONSE_MESSAGE_TOPIC);

        mResponseNetworkConnection = nc;
        mResponseTopic = responseTopic;
    }

    @Override
    public void responseFor(NetworkMessage message) {

        if (mResponseNetworkConnection == null) {
            throw new ExceptionInInitializerError("response connection is null");
        }

        if (mResponseTopic == null) {
            throw new ExceptionInInitializerError("response topic connection is null");
        }

        MqttNetworkMessage mqttNetworkMessage = (MqttNetworkMessage) message;
        mqttNetworkMessage.addMessageType(MESSAGE_TYPE_RESPONSE);

        try {
            mResponseNetworkConnection.publish(mResponseTopic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
        }
    }

    // Internal functions to support request-response message flow, because we use JsonObject which includes "MESSAGE_TYPE" in the message, too.
    void addMessageType (int messageType) {
        JsonValue typeValue = mMessage.get(MqttConstants.MSG_TYPE_KEY);
        if (typeValue == null) {
            mMessage.add(MqttConstants.MSG_TYPE_KEY, messageType);
        }
    }

    void removeMessageType () {
        mMessage.remove(MqttConstants.MSG_TYPE_KEY);
    }
}
