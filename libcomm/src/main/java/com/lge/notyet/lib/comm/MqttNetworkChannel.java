package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.lge.notyet.lib.comm.util.Log;
import com.eclipsesource.json.JsonObject;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.*;

public class MqttNetworkChannel implements INetworkChannel {

    private static final String LOG_TAG = "MqttNetworkChannel";

    private final HashMap<String, IMessageCallback> mRequestCbMap = new HashMap<String, IMessageCallback>();
    private static final AtomicInteger sRequestSequenceNumber = new AtomicInteger(0);

    // MQTT Variables
    private static final int DEFAULT_MQTT_QOS = 2;
    private MqttAsyncClient mMqttAsyncClient = null;
    private final MqttCallback mMqttCallback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable throwable) {
            logv("connectionLost");
            if (mNetworkCallback != null) mNetworkCallback.onLost();
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            IMessageCallback rspCb = null;
            MqttNetworkMessage networkMsg = null;

            if (mqttMessage == null) {
                throw new NullPointerException("received null message on topic=" + topic);
            }

            JsonObject message = JsonObject.readFrom(new String(mqttMessage.getPayload()));
            if (message == null) {
                throw new NullPointerException("fail to read JsonObject from message=" + mqttMessage.toString() + " on topic=" + topic);
            }

            int messageType = NetworkMessage.MESSAGE_TYPE_UNKNOWN;

            try {
                messageType = message.get(NetworkMessage.MSG_TYPE).asInt();
            } catch (UnsupportedOperationException uoe) {
                // We will throw this exception, now, but can be added some handling later.
                throw uoe;
            }

            networkMsg = MqttNetworkMessage.build(messageType, message);
            networkMsg.removeMessageType();

            if (topic == null) {
                log("received message=" + message + " on null topic");
            }

            logv("received message=" + message + " on topic=" + topic);

            if (messageType == NetworkMessage.MESSAGE_TYPE_REQUEST) {
                try {
                    networkMsg.makeResponseInfo(mMqttAsyncClient, topic);
                } catch (UnsupportedOperationException uoe) {
                    log(uoe.toString());
                    throw uoe;
                }
            }

            if (messageType == NetworkMessage.MESSAGE_TYPE_RESPONSE) {
                if (mRequestCbMap.containsKey(topic)) {
                    rspCb = mRequestCbMap.get(topic);
                    if (rspCb != null) {
                        rspCb.onMessage(topic, networkMsg);
                    }
                    mRequestCbMap.remove(topic);
                    mMqttAsyncClient.unsubscribe(topic);
                }
            }

            if (mMessageCallback != null && rspCb == null) mMessageCallback.onMessage(topic, networkMsg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            logv("deliveryComplete");

            // TODO: Should we add notification?
        }
    };

    private final IMqttActionListener mMqttConnectListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            logv("connected to server");
            if (mNetworkCallback != null) mNetworkCallback.onConnected();
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            logv("failed to connect to server");
            if (mNetworkCallback != null) mNetworkCallback.onConnectFailed();
        }
    };

    // Internal Variables
    private String mName = null;
    private INetworkCallback mNetworkCallback = null;
    private IMessageCallback mMessageCallback = null;

    public MqttNetworkChannel(IMessageCallback msgCb) {
        mMessageCallback = msgCb;
    }

    public MqttNetworkChannel(String name, IMessageCallback msgCb) {
        mName = name;
        mMessageCallback = msgCb;
    }

    @Override
    public void connect(InetAddress ipAddress, INetworkCallback networkCb) {

        mNetworkCallback = networkCb;

        try {
            mMqttAsyncClient = new MqttAsyncClient("tcp://" + ipAddress.getHostAddress(), MqttAsyncClient.generateClientId());

            mMqttAsyncClient.setCallback(mMqttCallback);
            mMqttAsyncClient.connect(null, mMqttConnectListener);

        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {

        try {
            mMqttAsyncClient.disconnect();

            // TODO: Should we call onLost() or make one more function like onDisconnected()?
            if (mNetworkCallback != null) mNetworkCallback.onLost();

        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return mMqttAsyncClient.isConnected();
    }

    @Override
    public void subscribe(Uri uri) {
        try {
            mMqttAsyncClient.subscribe(uri.getPath(), DEFAULT_MQTT_QOS);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(Uri uri) {
        try {
            mMqttAsyncClient.unsubscribe(uri.getPath());
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void send(Uri uri, JsonObject message) {

        MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.build(NetworkMessage.MESSAGE_TYPE_NOTIFICATION, message);
        mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_NOTIFICATION);

        try {
            mMqttAsyncClient.publish(uri.getPath(), new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void request(Uri uri, JsonObject message, IMessageCallback responseCb) {

        int sequenceNumber = sRequestSequenceNumber.addAndGet(1);
        try {
            mRequestCbMap.put(uri.getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, responseCb);
            mMqttAsyncClient.subscribe(uri.getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, DEFAULT_MQTT_QOS);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }

        MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.build(NetworkMessage.MESSAGE_TYPE_REQUEST, message);
        mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_REQUEST);

        try {
            mMqttAsyncClient.publish(uri.getPath() + MqttNetworkMessage.REQUEST_TOPIC + sequenceNumber, new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    // Log functions
    private  void log (String log) {
        Log.logd(LOG_TAG + "-" + mName, log);
    }

    private  void logv(String log) {
        Log.logv(LOG_TAG + "-" + mName, log);
    }
}
