package com.lge.notyet.lib.comm.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;

public class MqttClientTester implements Runnable {

    private static final String LOG_TAG = "MqttClientTester";

    private static final String TEST_LISTEN_TOPIC = "/test";
    private static final String TEST_SERVER_NOTIFICATION_TOPIC = "/server/notification";
    private static final String TEST_SERVER_REQUEST_TOPIC = "/server/req-res";

    private INetworkChannel mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mNc.subscribe(new MqttUri(TEST_LISTEN_TOPIC + "/#"));
        }

        @Override
        public void onConnectFailed() {
            Log.logd(LOG_TAG, "onConnectFailed");
        }

        @Override
        public void onLost() {
            Log.logd(LOG_TAG, "onLost");
            mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
        }
    };

    private final IMessageCallback mMessageCallback = (uri, msg) -> {

        // TODO: We need better way with Json
        if (msg.isRequest()) {

            Log.logd(LOG_TAG, "onRequested:" + msg.getMessage());

            JsonObject resp_msg = new JsonObject();
            resp_msg.add("type", "response");
            resp_msg.add("received message", msg.getMessage());
            msg.response(resp_msg);

        } else {
            Log.logd(LOG_TAG, "onMessage:" + msg.getMessage() + " on topic=" + uri);
        }
    };

    private final IMessageCallback mResponseCallback = (uri, msg) -> Log.logd(LOG_TAG, "onResponseMessage:" + msg.getMessage());

    private final IMessageTimeoutCallback mRequestTimeoutCallback = uri -> Log.logd(LOG_TAG, "onMessageTimeout on topic=" + uri);

    private MqttClientTester() {

        mNc = new MqttNetworkChannel(null, mMessageCallback);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new Thread(new MqttClientTester()).start();
    }

    @Override
    public void run() {
        int i = 0;
        while(true) {
            try {
                Thread.sleep(2000);
                if(!mNc.isConnected()) continue;
                i++;
                if (i % 3 == 0) {
                    JsonObject req_msg = new JsonObject().add("type", "request").add("number", i);
                    Log.logd(LOG_TAG, "client send request msg=" + req_msg);
                    mNc.request(new MqttUri(TEST_SERVER_REQUEST_TOPIC), req_msg, mResponseCallback, mRequestTimeoutCallback);
                } else if (i % 3 == 1) {
                    JsonObject notification_msg = new JsonObject().add("type", "notify").add("number", i);
                    Log.logd(LOG_TAG, "client send notification msg=" + notification_msg);
                    mNc.send(new MqttUri(TEST_SERVER_NOTIFICATION_TOPIC), notification_msg);
                } else {
                    JsonObject loopback_msg = new JsonObject().add("type", "loopback").add("number", i);
                    Log.logd(LOG_TAG, "client loopback test msg=" + loopback_msg);
                    mNc.send(new MqttUri(TEST_LISTEN_TOPIC), loopback_msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
