package com.lge.notyet.verticle;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LibTester implements Runnable {

    private static final String LOG_TAG = "LibTester";

    private MqttNetworkChannel mNc = null;
    private INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mNc.subscribe(new Uri("/fac/#"));
        }

        @Override
        public void onConnectFailed() {
            Log.logd(LOG_TAG, "onConnectFailed");
        }

        @Override
        public void onLost() {
            Log.logd(LOG_TAG, "onLost");
        }
    };

    private IMessageCallback mMessageCallback = new IMessageCallback() {

        @Override
        public void onMessage(String topic, NetworkMessage msg) {

            // TODO: We need better way with Json
            if (msg.isRequest()) {

                Log.logd(LOG_TAG, "onRequested:" + msg.getMessage());

                JsonObject resp_msg = new JsonObject();
                resp_msg.add("RESPONSE: ", msg.getMessage());
                msg.response(resp_msg);

            } else {
                Log.logd(LOG_TAG, "onMessage:" + msg.getMessage());
            }
        }
    };

    private IMessageCallback mResponseCallback = new IMessageCallback() {

        @Override
        public void onMessage(String topic, NetworkMessage msg) {
            Log.logd(LOG_TAG, "mResponseCallback:" + msg.getMessage());
        }
    };

    public LibTester() {

        mNc = new MqttNetworkChannel(mNetworkCallback, mMessageCallback);
        mNc.connect(InetAddress.getLoopbackAddress());
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        new Thread(new com.lge.notyet.lib.comm.test.MqttChannelTester()).start();
    }

    @Override
    public void run() {
        int i = 0;
        while(true) {
            try {
                Thread.sleep(1000);
                i++;
                if (i % 5 == 0) {
                    Log.logd(LOG_TAG, "sendRequest: REQ_" + i);
                    JsonObject req_msg = new JsonObject();
                    req_msg.add("REQ_NUM", i);
                    mNc.request(new Uri("/fac/req_res"), req_msg, mResponseCallback);
                } else {
                    Log.logd(LOG_TAG, "sendMessage: TEST_" + i);
                    JsonObject noti_msg = new JsonObject();
                    noti_msg.add("NOTIFY", i);
                    mNc.send(new Uri("/fac/1"), noti_msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
