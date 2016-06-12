package com.lge.notyet.lib.comm.mqtt;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.lib.comm.NetworkMessage;
import org.eclipse.paho.client.mqttv3.*;

public class MqttNetworkMessage extends NetworkMessage <JsonObject> {

    public static final int MESSAGE_TYPE_UNKNOWN = -1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 0;
    public static final int MESSAGE_TYPE_REQUEST = 1;
    public static final int MESSAGE_TYPE_RESPONSE = 2;

    static final String REQUEST_TOPIC = "/request/";
    static final String RESPONSE_TOPIC = "/response/";
    static final String WILL_TOPIC = "/will";

    public static final String MSG_TYPE = "_msg_type_"; // Reserved

    private MqttAsyncClient mResponseNetworkConnection = null;
    private String mResponseTopic = null;

    private JsonObject mMessage = null;

    private MqttNetworkMessage(JsonObject message) {
        super();
        mMessage = message;
    }

    public static MqttNetworkMessage build(JsonObject message) {
        return new MqttNetworkMessage(message);
    }

    void makeResponseInfo(MqttAsyncClient nc, String topic) throws UnsupportedOperationException {

        if (!topic.contains(REQUEST_TOPIC)) {
           throw new UnsupportedOperationException("this request type message has wrong topic, topic=" + topic);
        }

        String responseTopic = new String(topic);
        responseTopic = responseTopic.replace(REQUEST_TOPIC, RESPONSE_TOPIC);

        mResponseNetworkConnection = nc;
        mResponseTopic = responseTopic;
    }

    public byte[] getBytes() {
        return mMessage.toString().getBytes();
    }

    @Override
    protected void response_impl(NetworkMessage message) throws ExceptionInInitializerError {

        if (mResponseNetworkConnection == null) {
            throw new ExceptionInInitializerError("response connection is null");
        }

        if (mResponseTopic == null) {
            throw new ExceptionInInitializerError("response topic connection is null");
        }

        MqttNetworkMessage mqttNetworkMessage = (MqttNetworkMessage) message;
        mqttNetworkMessage.addMessageType(MESSAGE_TYPE_RESPONSE);

        try {
            mResponseNetworkConnection.publish(mResponseTopic, new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
        }
    }

    // Utility Function to support request-response message flow.
    void addMessageType (int messageType) {
        JsonValue typeValue = mMessage.get(MSG_TYPE);
        if (typeValue == null) {
            mMessage.add(MSG_TYPE, messageType);
        }
    }

    void removeMessageType () {
        mMessage.remove(MSG_TYPE);
    }

    public JsonObject getMessage() {
        return mMessage;
    }
}
