package com.lge.notyet.lib.comm.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;

public class MqttClientChannelTester implements Runnable {

    private static final String LOG_TAG = "MqttClientChannelTester";

    private final class TestLoopbackNotificationChannel extends NotificationChannel {

        private static final String TEST_LISTEN_TOPIC = "/test";

        TestLoopbackNotificationChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_LISTEN_TOPIC);
        }

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestLoopbackNotificationChannel.onNotify():" + message.getMessage() + " on channel=" + uri.getPath());
        }
    }

    private final class TestNotificationChannel extends PublishChannel {

        private static final String TEST_SERVER_NOTIFICATION_TOPIC = "/server/notification";

        TestNotificationChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_SERVER_NOTIFICATION_TOPIC);
        }
    }

    private final class TestRequestChannel extends RequestChannel {

        private static final String TEST_SERVER_REQUEST_TOPIC = "/server/req-res";

        TestRequestChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_SERVER_REQUEST_TOPIC);
        }

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestRequestChannel.onResponse():" + message.getMessage() + " on channel=" + uri.getPath());
        }

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestRequestChannel.onTimeout():" + message.getMessage() + " on requested channel=" + getChannelDescription());
        }
    }

    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mTestLoopbackNotificationChannel.listen();
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

    private TestNotificationChannel mTestNotificationChannel = null;
    private TestLoopbackNotificationChannel mTestLoopbackNotificationChannel = null;
    private TestRequestChannel mTestRequestChannel = null;

    private MqttClientChannelTester() {

        mNc = new MqttNetworkConnection(null);

        mTestNotificationChannel = new TestNotificationChannel(mNc);
        mTestLoopbackNotificationChannel = new TestLoopbackNotificationChannel(mNc);
        mTestRequestChannel = new TestRequestChannel(mNc);

        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new Thread(new MqttClientChannelTester()).start();
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(1000);
                if(!mNc.isConnected()) continue;
                i++;
                if (i % 3 == 0) {

                    JsonObject req_msg = new JsonObject().add("type", "request").add("number", i);
                    Log.logd(LOG_TAG, "client send request msg=" + req_msg);
                    MqttNetworkMessage msg = MqttNetworkMessage.build(req_msg);
                    mTestRequestChannel.request(msg);

                } else if (i % 3 == 1) {

                    JsonObject notification_msg = new JsonObject().add("type", "notify").add("number", i);
                    Log.logd(LOG_TAG, "client send notification msg=" + notification_msg);
                    MqttNetworkMessage msg = MqttNetworkMessage.build(notification_msg);
                    mTestNotificationChannel.notify(msg);

                } else {
                    JsonObject loopback_msg = new JsonObject().add("type", "loopback").add("number", i);
                    Log.logd(LOG_TAG, "client send loopback notification msg=" + loopback_msg);
                    MqttNetworkMessage msg = MqttNetworkMessage.build(loopback_msg);
                    mTestLoopbackNotificationChannel.notify(msg);
                }

                if (i> 100) break;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mNc.disconnect();
    }
}
