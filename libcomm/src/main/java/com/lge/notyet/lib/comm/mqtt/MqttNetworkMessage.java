package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-10.
 */

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.lib.comm.NetworkMessage;
import org.eclipse.paho.client.mqttv3.*;

class MqttNetworkMessage extends NetworkMessage {

    static final String REQUEST_TOPIC = "/request/";
    static final String RESPONSE_TOPIC = "/response/";
    static final String WILL_TOPIC = "/will";

    private MqttAsyncClient mResponseNetworkChannel = null;
    private String mResponseTopic = null;

    private MqttNetworkMessage(int messageType, JsonObject message) {
        super(messageType, message);
    }

    static MqttNetworkMessage build(int messageType, JsonObject message) {
        return new MqttNetworkMessage(messageType, message);
    }

    void makeResponseInfo(MqttAsyncClient nc, String topic) throws UnsupportedOperationException {

        if (topic.contains(REQUEST_TOPIC) == false) {
           throw new UnsupportedOperationException("this request type message has wrong topic, topic=" + topic);
        }

        String responseTopic = new String(topic);
        responseTopic = responseTopic.replace(REQUEST_TOPIC, RESPONSE_TOPIC);

        mResponseNetworkChannel = nc;
        mResponseTopic = responseTopic;
    }

    byte[] getBytes() {
        return getMessage().toString().getBytes();
    }

    @Override
    protected void response_impl(JsonObject message) throws ExceptionInInitializerError {

        if (mResponseNetworkChannel == null) {
            throw new ExceptionInInitializerError("response channel is null");
        }

        if (mResponseTopic == null) {
            throw new ExceptionInInitializerError("response topic channel is null");
        }

        MqttNetworkMessage mqttNetworkMessage = new MqttNetworkMessage(MESSAGE_TYPE_RESPONSE, message);
        mqttNetworkMessage.addMessageType(MESSAGE_TYPE_RESPONSE);

        try {
            mResponseNetworkChannel.publish(mResponseTopic, new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
        }
    }

    // Utility Function to support request-response message flow.
    void addMessageType (int messageType) {
        JsonObject messageObj = getMessage();
        JsonValue typeValue = messageObj.get(MSG_TYPE);
        if (typeValue == null) {
            messageObj.add(MSG_TYPE, messageType);
        } else {
            // TODO: Add Exception Handler
        }
    }

    void removeMessageType () {
        JsonObject messageObj = getMessage();
        messageObj.remove(MSG_TYPE);
    }
}
