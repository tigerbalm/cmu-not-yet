package com.lge.notyet.lib.comm.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;

public class MqttPassiveRedundancyServerChannelTester {

    private static final String LOG_TAG = "MqttPassiveRedundancyServerChannelTester";

    private final class TestSubscribeChannel extends SubscribeChannel {

        private static final String TEST_SERVER_NOTIFICATION_TOPIC = "/server/notification";

        protected TestSubscribeChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_SERVER_NOTIFICATION_TOPIC);
        }

        @Override
        public void onNotified(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestSubscribeChannel.onMessage:" + message.getMessage() + " on channel=" + getChannelDescription());
        }
    }

    private final class TestResponseChannel extends ResponseChannel {

        private static final String TEST_SERVER_REQUEST_TOPIC = "/server/req-res/#";

        protected TestResponseChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_SERVER_REQUEST_TOPIC);
        }

        @Override
        public void onRequested(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestRequestChannel.onRequested():" + message.getMessage() + " on channel=" + uri.getPath());

            JsonObject resp_msg = new JsonObject().add("type", "response").add("received message", message.getMessage().toString());
            MqttNetworkMessage msg = MqttNetworkMessage.build(NetworkMessage.MESSAGE_TYPE_NOTIFICATION, resp_msg);
            message.response(msg);
        }
    }


    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mTestSubscribeChannel.listen();
            mTestResponseChannel.listen();
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

    TestSubscribeChannel mTestSubscribeChannel = null;
    TestResponseChannel mTestResponseChannel = null;

    private MqttPassiveRedundancyServerChannelTester() {

        mNc = new MqttPassiveRedundancyNetworkConnection("server", new MqttNetworkConnection(null));
        mTestSubscribeChannel = new TestSubscribeChannel(mNc);
        mTestResponseChannel = new TestResponseChannel(mNc);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new MqttPassiveRedundancyServerChannelTester();
    }
}