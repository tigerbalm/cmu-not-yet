package com.lge.notyet.lib.comm.test;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.Random;

public class MqttPassiveRedundancyTester {

    private static final String LOG_TAG = "MqttPassiveRedundancyTester";

    private static final String TEST_SERVER_TOPIC = "/server/#";

    private INetworkChannel mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mNc.subscribe(new Uri(TEST_SERVER_TOPIC));
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

    final IMessageCallback mMessageCallback = new IMessageCallback() {

        @Override
        public void onMessage(Uri uri, NetworkMessage msg) {

            // TODO: We need better way with Json
            if (msg.isRequest()) {
                Log.logd(LOG_TAG, "onRequested:" + msg.getMessage() + " on topic=" + uri);
                JsonObject resp_msg = new JsonObject().add("type", "response").add("received message", msg.getMessage());
                msg.response(resp_msg);
            } else {
                Log.logd(LOG_TAG, "onNotified:" + msg.getMessage() + " on topic=" + uri);
            }
        }
    };

    public MqttPassiveRedundancyTester() {

        mNc = new MqttPassiveRedundancyNetworkChannel("server", new MqttNetworkChannel(null, mMessageCallback));
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new MqttPassiveRedundancyTester();
    }
}
