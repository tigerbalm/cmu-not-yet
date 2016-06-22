package com.lge.notyet.driver.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.time.Instant;
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

    private final IOnRequest mGetReservationRequestReceived = (networkChannel, uri, message) -> {

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());

        // TODO: We can change this value for test
        boolean isReservedTest = true;
        boolean isParkedTest = false;

        //noinspection ConstantConditions
        if (!isReservedTest) {
            response.getMessage().add("success", 0);
            response.getMessage().add("cause", "NO_RESERVATION_EXIST");
        } else {
            response.getMessage().add("success", 1);
            response.getMessage().add("reservation_ts", 1466561562L);
            response.getMessage().add("confirmation_no", 3333);
            response.getMessage().add("id", 1);
            response.getMessage().add("facility_id", 1);
            response.getMessage().add("controller_physical_id" ,"1");

            if (isParkedTest) {
                response.getMessage().add("begin_ts", 1466561762L);
            }
        }

        Log.logd(LOG_TAG, "UpdateFacilityList Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private final IOnRequest mUpdateFacilityListRequestReceived = (networkChannel, uri, message) -> {

        List<JsonObject> facilityList = new ArrayList<>();
        facilityList.add(new JsonObject().add("id", 1).add("name", "Shadyside Parking Lot"));
        facilityList.add(new JsonObject().add("id", 7).add("name", "CMU Parking Lot"));
        MqttNetworkMessage response = new MqttNetworkMessage(ReservableFacilitiesResponseChannel.createResponseObject(facilityList));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "UpdateFacilityList Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private final IOnRequest mLoginRequestReceived = (networkChannel, uri, message) -> {

        MqttNetworkMessage response = new MqttNetworkMessage(LoginResponseChannel.createResponseObject(1, "1111-2222-3333-4444", "12/16", "12345678"));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "Login Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private final IOnRequest mSignUpRequestReceived = (networkChannel, uri, message) -> {

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mSignUpRequestReceived Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private final IOnRequest mCancelReservationRequestReceived = (networkChannel, uri, message) -> {

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mCancelReservationRequestReceived Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private final IOnRequest mReservationRequestReceived = (networkChannel, uri, message) -> {

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        response.getMessage().add("id", 1);
        response.getMessage().add("confirmation_no", 10);
        response.getMessage().add("controller_physical_id" ,"1");
        response.getMessage().add("reservation_ts", Instant.now().getEpochSecond());
        response.getMessage().add("facility_id", 1);
        Log.logd(LOG_TAG, "Reservation Requested Received=" + message.getMessage());
        message.responseFor(response);
    };

    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mTestSubscribeChannel.listen();
            mMakeReservationResponseChannel.listen();
            mMakeReservationResponseChannel.addObserver(mReservationRequestReceived);
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
    private MakeReservationResponseChannel mMakeReservationResponseChannel = null;
    private LoginResponseChannel mLoginResponseChannel = null;
    private ReservableFacilitiesResponseChannel mReservableFacilitiesResponseChannel = null;
    private GetReservationResponseChannel mGetReservationResponseChannel = null;
    private SignUpResponseChannel mSignUpResponseChannel = null;
    private CancelReservationResponseChannel mCancelReservationResponseChannel = null;

    private TestServer() {

        mNc = new MqttNetworkConnection(null);
        mTestSubscribeChannel = new TestSubscribeChannel(mNc);
        mMakeReservationResponseChannel = new MakeReservationResponseChannel(mNc);
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