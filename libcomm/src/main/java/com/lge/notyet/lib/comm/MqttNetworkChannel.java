package com.lge.notyet.lib.comm;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.lge.notyet.lib.comm.util.Log;
import org.eclipse.paho.client.mqttv3.*;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.eclipsesource.json.JsonObject;

public class MqttNetworkChannel implements INetworkChannel {

    private static final String LOG_TAG = "MqttNetworkChannel";

    private static final String REQUEST_TOPIC = "/request/";
    private static final String RESPONSE_TOPIC = "/response/";

    private final HashMap<String, IMessageCallback> mRequestCbMap = new HashMap<String, IMessageCallback>();
    private AtomicInteger mRequestSequnceNumber = new AtomicInteger(0);

    // MQTT Variables
    private MqttAsyncClient mMqttAsyncClient = null;
    private MqttCallback mMqttCallback = new MqttCallback() {

        @Override
        public void connectionLost(Throwable throwable) {
            log("connectionLost");
            if (mNetworkCallback != null) mNetworkCallback.onLost();
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            IMessageCallback rspCb = null;
            MqttNetworkMessage networkMsg = null;

            JsonObject message = JsonObject.readFrom(new String(mqttMessage.getPayload()));
            int messageType = message.get(NetworkMessage.MSG_TYPE).asInt();

            networkMsg = MqttNetworkMessage.buildRequest(message);

            //log("messageArrived: topic=" + topic + ", message=" + message);
            if (messageType == NetworkMessage.MESSAGE_TYPE_REQUEST) {

                String responseTopic = new String(topic);
                responseTopic = responseTopic.replace("/request/", "/response/");
                networkMsg.setResponseTopic(mMqttAsyncClient, responseTopic);
            }

            if (messageType == NetworkMessage.MESSAGE_TYPE_RESPONSE) {
                if (mRequestCbMap.containsKey(topic)) {

                    rspCb = mRequestCbMap.get(topic);
                    if (rspCb != null) {
                        rspCb.onMessage(topic, networkMsg);
                    }
                    mMqttAsyncClient.unsubscribe(topic);
                    mRequestCbMap.remove(topic);
                }
            }

            if (mMessageCallback != null && rspCb == null) mMessageCallback.onMessage(topic, networkMsg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            logv("deliveryComplete");
            //TODO: Do something later
            if (mNetworkCallback != null) ;
        }
    };
    private IMqttActionListener mMqttConnectListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            log("connected to server");
            mNetworkCallback.onConnected();
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            mNetworkCallback.onConnectFailed();
        }
    };

    // Internal Variables
    INetworkCallback mNetworkCallback = null;
    IMessageCallback mMessageCallback = null;

    public MqttNetworkChannel(INetworkCallback networkCb, IMessageCallback msgCb) {
        mNetworkCallback = networkCb;
        mMessageCallback = msgCb;
    }

    @Override
    public void connect(InetAddress ipAddress) {
        try {
            mMqttAsyncClient = new MqttAsyncClient("tcp://" + ipAddress.getHostAddress(), MqttAsyncClient.generateClientId());

            mMqttAsyncClient.setCallback(mMqttCallback);
            mMqttAsyncClient.connect(null, mMqttConnectListener);

        } catch (MqttException e) {

        }
    }

    @Override
    public void subscribe(Uri uri) {
        try {
            mMqttAsyncClient.subscribe(uri.getPath(), 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Uri uri, JsonObject message) {

        MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.buildMessage(message);

        try {
            mMqttAsyncClient.publish(uri.getPath(), new MqttMessage(mqttNetworkMessage.getBytes()));
        } catch (MqttException e) {
        }
    }

    @Override
    public void request(Uri uri, JsonObject message, IMessageCallback responseCb) {

        // TODO: Will be fixed later, just for fast experimental.
        int sequnceNumer = mRequestSequnceNumber.addAndGet(1);
        mRequestCbMap.put(uri.getPath() + RESPONSE_TOPIC + sequnceNumer, responseCb);

        MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.buildRequest(message);

        try {
            mMqttAsyncClient.subscribe(uri.getPath() + RESPONSE_TOPIC + sequnceNumer, 2);
            mMqttAsyncClient.publish(uri.getPath() + REQUEST_TOPIC + sequnceNumer, new MqttMessage(mqttNetworkMessage.getBytes()));

        } catch (MqttException e) {
        }
    }

    public void log (String log) {
        Log.logd(LOG_TAG, log);
    }

    public void logv(String log) {
        Log.logv(LOG_TAG, log);
    }
}
