package com.lge.notyet.attendant.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TestServer {

    private static final String LOG_TAG = "TestServer";

    // Business Logic here, we have no time :(
    private IOnRequest mGetReservationRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse

            MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());

            boolean isReservedTest = true;

            if (isReservedTest == false) {
                response.getMessage().add("success", 0);
                response.getMessage().add("cause", "NO_RESERVATION_EXIST");
            } else {

                response.getMessage().add("success", 1);
                response.getMessage().add("reservation_ts", 1466021160L);
                response.getMessage().add("confirmation_no", 3333);
                response.getMessage().add("id", 1);
                response.getMessage().add("facility_id", 1);
            }

            System.out.println("UpdateFacilityList Requested Received=" + message.getMessage());
            message.responseFor(response);
        }
    };

    // Business Logic here, we have no time :(
    private IOnRequest mUpdateFacilityListRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse

            List<JsonObject> facilityList = new ArrayList<>();
            facilityList.add(new JsonObject().add("id", 1).add("name", "Shadyside Parking Lot"));
            facilityList.add(new JsonObject().add("id", 7).add("name", "CMU Parking Lot"));
            MqttNetworkMessage response = new MqttNetworkMessage(mReservableFacilitiesResponseChannel.createResponseObject(facilityList));
            response.getMessage().add("success", 1);
            System.out.println("UpdateFacilityList Requested Received=" + message.getMessage());
            message.responseFor(response);
        }
    };

    // Business Logic here, we have no time :(
    private IOnRequest mLoginRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            MqttNetworkMessage response = new MqttNetworkMessage(mLoginResponseChannel.createResponseObject(1, 2, "1111-2222-3333-4444", "12/16", "12345678"));
            response.getMessage().add("success", 1);
            System.out.println("Login Requested Received=" + message.getMessage());

            message.responseFor(response);
        }
    };

    // Business Logic here, we have no time :(
    private IOnRequest mSignUpRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
            response.getMessage().add("success", 1);
            System.out.println("mSignUpRequestReceived Requested Received=" + message.getMessage());

            message.responseFor(response);
        }
    };

    // Business Logic here, we have no time :(
    private IOnRequest mCancelReservationRequestReceived = new IOnRequest() {

        @Override
        public void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
            response.getMessage().add("success", 1);
            System.out.println("mCancelReservationRequestReceived Requested Received=" + message.getMessage());

            message.responseFor(response);
        }
    };


    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
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

    private LoginResponseChannel mLoginResponseChannel = null;
    private ReservableFacilitiesResponseChannel mReservableFacilitiesResponseChannel = null;
    private GetReservationResponseChannel mGetReservationResponseChannel = null;
    private SignUpResponseChannel mSignUpResponseChannel = null;
    private CancelReservationResponseChannel mCancelReservationResponseChannel = null;

    private TestServer() {

        mNc = new MqttNetworkConnection(null);
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