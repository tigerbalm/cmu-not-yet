package com.lge.notyet.driver.test;

import com.lge.notyet.driver.business.ReservationRequestMessage;
import com.lge.notyet.channels.ReservationResponseChannel;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;

public class TestServer {

    private static final String LOG_TAG = "TestServer";

    private final class TestSubscribeChannel extends SubscribeChannel {

        private static final String TEST_SERVER_NOTIFICATION_TOPIC = "/server/notification";

        TestSubscribeChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(TEST_SERVER_NOTIFICATION_TOPIC);
        }

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd(LOG_TAG, "TestSubscribeChannel.onNotify():" + message.getMessage() + " on channel=" + getChannelDescription());
        }
    }

    // Business Logic here, we have no time :(
    private IOnRequest mServervationRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            ReservationRequestMessage reqMsg = new ReservationRequestMessage((MqttNetworkMessage)message);
            System.out.println("Reservation Requested Received=" + message.getMessage());
            message.responseFor(new ReservationResponseMessage().setResult(1).setConfirmationNumber(10L));
        }
    };


    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mTestSubscribeChannel.listen();
            mReservationResponseChannel.listen();
            mReservationResponseChannel.addObserver(mServervationRequestReceived);
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

    private TestSubscribeChannel mTestSubscribeChannel = null;
    private ReservationResponseChannel mReservationResponseChannel = null;

    private TestServer() {

        mNc = new MqttNetworkConnection(null);
        mTestSubscribeChannel = new TestSubscribeChannel(mNc);
        mReservationResponseChannel = new ReservationResponseChannel(mNc);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new TestServer();
    }
}