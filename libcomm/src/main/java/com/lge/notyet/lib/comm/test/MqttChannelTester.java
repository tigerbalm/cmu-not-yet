package com.lge.notyet.lib.comm.test;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.Random;

public class MqttChannelTester implements Runnable {

    private static final String LOG_TAG = "MqttChannelTester-1";

    private INetworkChannel mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mNc.subscribe(new Uri("/fac/1/#"));
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
        public void onMessage(String topic, NetworkMessage msg) {

            // TODO: We need better way with Json
            if (msg.isRequest()) {

                Log.logd(LOG_TAG, "onRequested:" + msg.getMessage());

                JsonObject resp_msg = new JsonObject();
                resp_msg.add("type", "response");
                resp_msg.add("received message", msg.getMessage());
                msg.response(resp_msg);

            } else {
                Log.logd(LOG_TAG, "onMessage:" + msg.getMessage());
            }
        }
    };

    private final IMessageCallback mResponseCallback = new IMessageCallback() {

        @Override
        public void onMessage(String topic, NetworkMessage msg) {
            Log.logd(LOG_TAG, "onResponse:" + msg.getMessage());
        }
    };

    private final IMessageTimeoutCallback mRequestTimeoutCallback = new IMessageTimeoutCallback() {

        @Override
        public void onMessageTimeout(Uri uri) {
            Log.logd(LOG_TAG, "onMessageTimeout on " + uri.getPath());
        }
    };

    public MqttChannelTester() {

        mNc = new MqttPassiveRedundancyNetworkChannel("server", new MqttNetworkChannel("beney", mMessageCallback));
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        new Thread(new MqttChannelTester()).start();
    }

    @Override
    public void run() {
        int i = 0;
        while(true) {
            try {
                Thread.sleep(1000);
                i++;
                if (i % 5 == 0) {
                    JsonObject req_msg = new JsonObject();
                    req_msg.add("type", "request");
                    req_msg.add("number", i);
                    if (mNc.isConnected())  {
                        Log.logd(LOG_TAG, "sendRequest: REQ_" + i);
                        mNc.request(new Uri("/fac/1/req_res"), req_msg, mResponseCallback, mRequestTimeoutCallback);
                    }
                } else {
                    JsonObject noti_msg = new JsonObject();
                    noti_msg.add("type", "notify");
                    noti_msg.add("number", i);
                    if (mNc.isConnected()) {
                        Log.logd(LOG_TAG, "sendMessage: TEST_" + i);
                        mNc.send(new Uri("/fac/1/noti"), noti_msg);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
