package com.lge.noyet.lib.comm.test;

/**
 * Created by beney.kim on 2016-06-09.
 */

import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MqttChannelTester implements Runnable {

    private MqttNetworkChannel mNc = null;
    private INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd("MqttChannelTester", "onConnected");
            mNc.subscribe(new Uri("/fac/#"));
        }

        @Override
        public void onConnectFailed() {
            Log.logd("MqttChannelTester", "onConnectFailed");
        }

        @Override
        public void onLost() {
            Log.logd("MqttChannelTester", "onLost");
        }
    };

    private IMessageCallback mMessageCallback = new IMessageCallback() {
        @Override
        public void onMessage(String topic, String msg/*JsonObject notification*/) {

            // TODO: We need better way with Json
            if (topic.contains("/request/")) {

                Log.logd("MqttChannelTester", "onRequested:" + msg);
                String responseTopic = topic.replace("/request/", "/response/");

                // TODO: We need better way like sendReponse() or msg.responseTo();
                mNc.send(new Uri(responseTopic), "RSP to - " + msg);

            } else {
                Log.logd("MqttChannelTester", "onMessage:" + msg);
            }
        }
    };

    private IMessageCallback mResponseCallback = new IMessageCallback() {
        @Override
        public void onMessage(String topic, String msg/*JsonObject notification*/) {
            Log.logd("MqttChannelTester", "mResponseCallback:" + msg);
        }
    };

    public MqttChannelTester() {

        mNc = new MqttNetworkChannel(mNetworkCallback, mMessageCallback);

        try {
            mNc.connect(InetAddress.getByName("1.2.3.4"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        mNc.connect(InetAddress.getLoopbackAddress());
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
                    Log.logd("MqttChannelTester", "sendRequest: REQ_" + i);
                    mNc.request(new Uri("/fac/req_res"), "REQ_" + i, mResponseCallback);
                } else {
                    Log.logd("MqttChannelTester", "sendMessage: TEST_" + i);
                    mNc.send(new Uri("/fac/1"), "TEST_" + i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
