package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-09.
 * This class provide INetworkConnection implementation based on  Mqtt.
 */

import com.lge.notyet.lib.comm.*;
import org.eclipse.paho.client.mqttv3.*;
import com.eclipsesource.json.JsonObject;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MqttNetworkConnection extends BaseNetworkConnection {

    private static final String LOG_TAG = "MqttNetworkConnection";

    // Channels
    private final ConcurrentHashMap<String, NetworkChannel> mSubscribers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NetworkChannel> mRequestChannelMap = new ConcurrentHashMap<>();
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
                message = new JsonObject().add(MqttNetworkMessage.MSG_TYPE, NetworkMessage.MESSAGE_TYPE_NOTIFICATION).add("data", payload);
            }

            int messageType = NetworkMessage.MESSAGE_TYPE_UNKNOWN;

            try {
                messageType = message.get(MqttNetworkMessage.MSG_TYPE).asInt();
            } catch (Exception e) {
                // We will throw this exception, now, but can be added some handling later.
            }

            if (topic == null) {
                logv("received message=" + message + " on null topic");
            }

            MqttNetworkMessage networkMsg = MqttNetworkMessage.build(messageType, message);
            networkMsg.removeMessageType();

            logv("received message=" + message + " on topic=" + topic + ", messageType=" + messageType);

            if (messageType == NetworkMessage.MESSAGE_TYPE_REQUEST) {
                try {
                    networkMsg.makeResponseInfo(mMqttAsyncClient, topic);
                    mSubscribers.values().stream().filter(nc -> nc.getChannelDescription().isSuperOf(new MqttUri(topic))).forEach(channel -> {
                        channel.onRequested(channel, new MqttUri(topic), networkMsg);
                    });
                } catch (UnsupportedOperationException uoe) {
                    log(uoe.toString());
                    throw uoe;
                }
            }
            else if (messageType == NetworkMessage.MESSAGE_TYPE_RESPONSE) {
                if (topic != null && mRequestChannelMap.containsKey(topic)) {
                    NetworkChannel requestedNc = mRequestChannelMap.get(topic);

                    if (requestedNc != null) {
                        requestedNc.onResponse(requestedNc, new MqttUri(topic), networkMsg);
                    }

                    mRequestChannelMap.remove(topic);
                    mMqttAsyncClient.unsubscribe(topic);
                }
            }
            else if (messageType == NetworkMessage.MESSAGE_TYPE_NOTIFICATION) {
                try {

                    mSubscribers.values().stream().filter(nc -> nc.getChannelDescription().isSuperOf(new MqttUri(topic))).forEach(channel -> {
                        channel.onNotified(channel, new MqttUri(topic), networkMsg);
                    });
                } catch (UnsupportedOperationException uoe) {
                    log(uoe.toString());
                    throw uoe;
                }
            }
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

    public MqttNetworkConnection(String name) {
        super(name);
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
            if (connOptions == null) {
                mMqttAsyncClient.connect(null, mMqttConnectListener);
            } else {
                mMqttAsyncClient.connect(connOptions, null, mMqttConnectListener);
            }
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
    public void subscribe(NetworkChannel netChannel) {
        try {
            mSubscribers.put(netChannel.getHashKey(), netChannel);
            mMqttAsyncClient.subscribe(netChannel.getChannelDescription().getPath(), DEFAULT_MQTT_QOS);

            logv("subscribed for topic=" + netChannel.getChannelDescription().getPath() + " NetworkChannel=" + netChannel.getHashKey());
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(NetworkChannel netChannel) {
        try {
            mSubscribers.remove(netChannel.getHashKey());
            mMqttAsyncClient.unsubscribe(netChannel.getChannelDescription().getPath());

            logv("unsubscribe for topic=" + netChannel.getChannelDescription().getPath() + " NetworkChannel=" + netChannel.getHashKey());
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void send(NetworkChannel netChannel, NetworkMessage message) {

        MqttNetworkMessage mqttNetworkMessage = (MqttNetworkMessage) message;
        mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_NOTIFICATION);

        try {
            mMqttAsyncClient.publish(netChannel.getChannelDescription().getPath(), new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }

    @Override
    public void request(NetworkChannel netChannel, NetworkMessage message) {

        int sequenceNumber = sRequestSequenceNumber.addAndGet(1);
        try {
            mRequestChannelMap.put(netChannel.getChannelDescription().getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, netChannel);
            mMqttAsyncClient.subscribe(netChannel.getChannelDescription().getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber, DEFAULT_MQTT_QOS);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }

        MqttNetworkMessage mqttNetworkMessage = (MqttNetworkMessage) message;
        mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_REQUEST);

        try {
            mMqttAsyncClient.publish(netChannel.getChannelDescription().getPath() + MqttNetworkMessage.REQUEST_TOPIC + sequenceNumber, new MqttMessage(mqttNetworkMessage.getBytes()));
            scheduleNetworkTimeout(netChannel, message, netChannel.getChannelDescription().getPath() + MqttNetworkMessage.RESPONSE_TOPIC + sequenceNumber);
        } catch (MqttException e) {
            // TODO: Add Exception Handler
            e.printStackTrace();
        }
    }


    private static final int PENDED_REQUEST_MESSAGE_MAX = 50;
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(PENDED_REQUEST_MESSAGE_MAX);

    private class RequestTimeoutCheckThread implements Runnable {

        private final NetworkChannel mNetChannel;
        private final NetworkMessage mMessage;
        private final String mResponseTopic;

        RequestTimeoutCheckThread(NetworkChannel netChannel, NetworkMessage message, String responseTopic) {
            mNetChannel = netChannel;
            mMessage = message;
            mResponseTopic = responseTopic;
        }

        public void run() {

            if (mRequestChannelMap.containsKey(mResponseTopic)) {
                NetworkChannel netChannel = mRequestChannelMap.get(mResponseTopic);
                if (netChannel != null) {
                    netChannel.onTimeout(netChannel, mMessage);
                }
                mRequestChannelMap.remove(mResponseTopic);

                try {
                    mMqttAsyncClient.unsubscribe(mResponseTopic);
                } catch (MqttException e) {
                    // TODO: Add Exception Handler
                    e.printStackTrace();
                }
            }
        }
    }

    private void scheduleNetworkTimeout(NetworkChannel netChannel, NetworkMessage message, String responseTopic) {
        // TODO: Need to check Maximum Pended Requests?
        mScheduler.schedule(new RequestTimeoutCheckThread(netChannel, message, responseTopic), REQUEST_MESSAGE_PENDING_TIME, SECONDS);
    }

    // Log functions
    private  void log (String log) {
        com.lge.notyet.lib.comm.util.Log.logd(LOG_TAG + "-" + mName, log);
    }
    private  void logv(String log) {
        com.lge.notyet.lib.comm.util.Log.logv(LOG_TAG + "-" + mName, log);
    }
}
