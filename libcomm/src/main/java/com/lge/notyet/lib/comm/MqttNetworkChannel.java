package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 * This class provide INetworkChannel implementation based on  Mqtt.
 */

import org.eclipse.paho.client.mqttv3.*;
import com.eclipsesource.json.JsonObject;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MqttNetworkChannel extends BaseNetworkChannel {

    private static final String LOG_TAG = "MqttNetworkChannel";

    private class RequestMessageCallback {

        RequestMessageCallback (IMessageCallback messageCallback, IMessageTimeoutCallback messageTimeoutCallback) {
            mIMessageCallback = messageCallback;
            mIMessageTimeoutCallback = messageTimeoutCallback;
        }

        IMessageCallback mIMessageCallback = null;
        IMessageTimeoutCallback mIMessageTimeoutCallback = null;
    }

    private final ConcurrentHashMap<String, RequestMessageCallback> mRequestCbMap = new ConcurrentHashMap<>();
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
            RequestMessageCallback rspCbs = null;

            if (mqttMessage == null) {
                throw new NullPointerException("received null message on topic=" + topic);
            }

            JsonObject message;
            String payload = new String(mqttMessage.getPayload());
            try {
                message = JsonObject.readFrom(payload);
                if (message == null) {
                    throw new NullPointerException("fail to read JsonObject from message=" + mqttMessage.toString() + " on topic=" + topic);
                }
            } catch (Exception ex) {
                message = new JsonObject().add(NetworkMessage.MSG_TYPE, NetworkMessage.MESSAGE_TYPE_NOTIFICATION).add("data", payload);
            }

            int messageType = NetworkMessage.MESSAGE_TYPE_UNKNOWN;

            try {
                messageType = message.get(NetworkMessage.MSG_TYPE).asInt();
            } catch (Exception e) {
                // We will throw this exception, now, but can be added some handling later.
            }

            MqttNetworkMessage networkMsg = MqttNetworkMessage.build(messageType, message);
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
                if (topic != null && mRequestCbMap.containsKey(topic)) {
                    rspCbs = mRequestCbMap.get(topic);
                    if (rspCbs != null && rspCbs.mIMessageCallback != null) {
                        rspCbs.mIMessageCallback.onMessage(topic, networkMsg);
                    }
                    mRequestCbMap.remove(topic);
                    mMqttAsyncClient.unsubscribe(topic);
                }
            }

            if (mMessageCallback != null && (rspCbs == null || rspCbs.mIMessageCallback == null)) mMessageCallback.onMessage(topic, networkMsg);
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

    public MqttNetworkChannel(String name, IMessageCallback msgCb) {
        super(name, msgCb);
        mMessageCallback = msgCb;
    }

    @Override
    public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException {
        connect(ipAddress, networkCb, null);
    }

    void connect(InetAddress ipAddress, INetworkCallback networkCb, MqttConnectOptions connOptions) throws UnsupportedOperationException {

        if (mMqttAsyncClient != null && mMqttAsyncClient.isConnected()) {
            throw new UnsupportedOperationException();
        }

        mNetworkCallback = networkCb;

        try {
            mMqttAsyncClient = new MqttAsyncClient("tcp://" + ipAddress.getHostAddress(), MqttAsyncClient.generateClientId());

            mMqttAsyncClient.setCallback(mMqttCallback);
            mMqttAsyncClient.connect(connOptions, null, mMqttConnectListener);

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
        return mMqttAsyncClient != null && mMqttAsyncClient.isConnected();
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
        request(uri, message, responseCb, null);
    }

    @Override
    public void request(Uri uri, JsonObject message, IMessageCallback responseCb, IMessageTimeoutCallback timeoutCallback) {

        int sequenceNumber = sRequestSequenceNumber.addAndGet(1);
        try {
            mRequestCbMap.put(uri.getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, new RequestMessageCallback(responseCb, timeoutCallback));
            mMqttAsyncClient.subscribe(uri.getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, DEFAULT_MQTT_QOS);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }

        MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.build(NetworkMessage.MESSAGE_TYPE_REQUEST, message);
        mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_REQUEST);

        try {
            mMqttAsyncClient.publish(uri.getPath() + MqttNetworkMessage.REQUEST_TOPIC + sequenceNumber, new MqttMessage(mqttNetworkMessage.getBytes()));
            scheduleNetworkTimeout(uri.getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, uri);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }


    private static final int PENDED_REQUEST_MESSAGE_MAX = 50;
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(PENDED_REQUEST_MESSAGE_MAX);

    private class RequestTimeoutCheckThread implements Runnable {

        private final Uri mRequestedUri;
        private final String mResponseTopic;

        RequestTimeoutCheckThread(String responseTopic, Uri uri) {
            mRequestedUri = uri;
            mResponseTopic = responseTopic;
        }

        public void run() {

            if (mRequestCbMap.containsKey(mResponseTopic)) {
                RequestMessageCallback rspCbs = mRequestCbMap.get(mResponseTopic);
                if (rspCbs != null && rspCbs.mIMessageTimeoutCallback != null) {
                    rspCbs.mIMessageTimeoutCallback.onMessageTimeout(mRequestedUri);
                }
                mRequestCbMap.remove(mResponseTopic);

                try {
                    mMqttAsyncClient.unsubscribe(mResponseTopic);
                } catch (MqttException e) {
                    // TODO: Add Exception Handler
                    e.printStackTrace();
                }
            }
        }
    }

    private void scheduleNetworkTimeout(String topic, Uri requestedUri) {
        // TODO: Need to check Maximum Pended Requests?
        mScheduler.schedule(new RequestTimeoutCheckThread(topic, requestedUri), REQUEST_MESSAGE_PENDING_TIME, SECONDS);
    }

    // Log functions
    private  void log (String log) {
        com.lge.notyet.lib.comm.util.Log.logd(LOG_TAG + "-" + mName, log);
    }
    private  void logv(String log) {
        com.lge.notyet.lib.comm.util.Log.logv(LOG_TAG + "-" + mName, log);
    }
}
