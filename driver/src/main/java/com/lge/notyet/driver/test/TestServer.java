package com.lge.notyet.driver.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.driver.business.ReservationResponseMessage;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

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
    private final IOnRequest mGetReservationRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());

        // TODO: We can change this value for test
        boolean isReservedTest = true;

        //noinspection ConstantConditions
        if (!isReservedTest) {
            response.getMessage().add("success", 0);
            response.getMessage().add("cause", "NO_RESERVATION_EXIST");
        } else {

            response.getMessage().add("success", 1);
            response.getMessage().add("reservation_ts", 1466021160L);
            response.getMessage().add("confirmation_no", 3333);
            response.getMessage().add("id", 1);
            response.getMessage().add("facility_id", 1);
        }

        Log.logd(LOG_TAG, "UpdateFacilityList Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mUpdateFacilityListRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse

        List<JsonObject> facilityList = new ArrayList<>();
        facilityList.add(new JsonObject().add("id", 1).add("name", "Shadyside Parking Lot"));
        facilityList.add(new JsonObject().add("id", 7).add("name", "CMU Parking Lot"));
        MqttNetworkMessage response = new MqttNetworkMessage(ReservableFacilitiesResponseChannel.createResponseObject(facilityList));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "UpdateFacilityList Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mLoginRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        MqttNetworkMessage response = new MqttNetworkMessage(LoginResponseChannel.createResponseObject(1, 2, "1111-2222-3333-4444", "12/16", "12345678"));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "Login Requested Received=" + message.getMessage());

        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mSignUpRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mSignUpRequestReceived Requested Received=" + message.getMessage());

        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mCancelReservationRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mCancelReservationRequestReceived Requested Received=" + message.getMessage());

        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mReservationRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "Reservation Requested Received=" + message.getMessage());
        message.responseFor(new ReservationResponseMessage().setResult(1).setConfirmationNumber(10L).setReservationId(1));
    };


    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mTestSubscribeChannel.listen();
            mReservationResponseChannel.listen();
            mReservationResponseChannel.addObserver(mReservationRequestReceived);
            mLoginResponseChannel.listen();
            mLoginResponseChannel.addObserver(mLoginRequestReceived);
            mReservableFacilitiesResponseChannel.listen();
            mReservableFacilitiesResponseChannel.addObserver(mUpdateFacilityListRequestReceived);
            mGetReservationResponseChannel.listen();
            mGetReservationResponseChannel.addObserver(mGetReservationRequestReceived);
            mSignUpResponseChannel.listen();
            mSignUpResponseChannel.addObserver(mSignUpRequestReceived);
            mCancelReservationResponseChannel.listen();
            mCancelReservationResponseChannel.addObserver(mCancelReservationRequestReceived);
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
    private LoginResponseChannel mLoginResponseChannel = null;
    private ReservableFacilitiesResponseChannel mReservableFacilitiesResponseChannel = null;
    private GetReservationResponseChannel mGetReservationResponseChannel = null;
    private SignUpResponseChannel mSignUpResponseChannel = null;
    private CancelReservationResponseChannel mCancelReservationResponseChannel = null;

    private TestServer() {

        mNc = new MqttNetworkConnection(null);
        mTestSubscribeChannel = new TestSubscribeChannel(mNc);
        mReservationResponseChannel = new ReservationResponseChannel(mNc);
        mLoginResponseChannel = new LoginResponseChannel(mNc);
        mReservableFacilitiesResponseChannel = new ReservableFacilitiesResponseChannel(mNc);
        mGetReservationResponseChannel = new GetReservationResponseChannel(mNc);
        mSignUpResponseChannel = new SignUpResponseChannel(mNc);
        mCancelReservationResponseChannel = new CancelReservationResponseChannel(mNc);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new TestServer();
    }
}