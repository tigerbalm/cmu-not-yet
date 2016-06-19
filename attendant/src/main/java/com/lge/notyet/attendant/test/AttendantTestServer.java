package com.lge.notyet.attendant.test;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class AttendantTestServer {

    private static final String LOG_TAG = "TestServer";

    // Business Logic here, we have no time :(
    private final IOnRequest mGetSlotsRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "mGetSlotsRequestReceived Requested Received=" + message.getMessage() + ", topic=" + uri.getLocation().toString());

        List<JsonObject> slotList = new ArrayList<>();
        slotList.add(new JsonObject().add("id", 1).add("number", 1).add("occupied", 1).add("occupied_ts", 1466021160L).add("reserved", 1).add("controller_id",1).add("physical_id",3).add("reservation_id", 23).add("email", "tony").add("reservation_ts", 1466001160L));
        slotList.add(new JsonObject().add("id", 2).add("number", 2).add("occupied", 1).add("occupied_ts", 1466041160L).add("reserved", 1).add("controller_id",1).add("physical_id",3).add("reservation_id", 123).add("email", "luffy").add("reservation_ts", 1466021160L));
        slotList.add(new JsonObject().add("id", 3).add("number", 3).add("occupied", 0).add("occupied_ts", 0L).add("reserved", 1).add("controller_id",1).add("physical_id",3).add("reservation_id", 231).add("email", "allie").add("reservation_ts", 1466061160L));
        slotList.add(new JsonObject().add("id", 4).add("number", 4).add("occupied", 0).add("occupied_ts", 0L).add("reserved", 0).add("controller_id",1).add("physical_id",3).add("reservation_id", 0).add("email", "").add("reservation_ts", 0L));
        slotList.add(new JsonObject().add("id", 5).add("number", 1).add("occupied", 0).add("occupied_ts", 0L).add("reserved", 0).add("controller_id",2).add("physical_id",8).add("reservation_id", 0).add("email", "").add("reservation_ts", 0L));
        slotList.add(new JsonObject().add("id", 6).add("number", 2).add("occupied", 0).add("occupied_ts", 0L).add("reserved", 1).add("controller_id",2).add("physical_id",8).add("reservation_id", 121).add("email", "david").add("reservation_ts", 1466021160L));
        slotList.add(new JsonObject().add("id", 7).add("number", 3).add("occupied", 0).add("occupied_ts", 0L).add("reserved", 1).add("controller_id",2).add("physical_id",8).add("reservation_id", 344).add("email", "reshout").add("reservation_ts", 1466021160L));
        slotList.add(new JsonObject().add("id", 8).add("number", 4).add("occupied", 1).add("occupied_ts", 1466051160L).add("reserved", 1).add("controller_id",2).add("physical_id",8).add("reservation_id", 5655).add("email", "beney").add("reservation_ts", 1466045160L));
        MqttNetworkMessage response = new MqttNetworkMessage(GetSlotsResponseChannel.createResponseObject(slotList));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mGetSlotsRequestReceived send Response=" + response.getMessage());
        message.responseFor(response);
    };


    // Business Logic here, we have no time :(
    private final IOnRequest mGetFacilitiesRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "mGetFacilitiesRequestReceived Requested Received=" + message.getMessage());
        List<JsonObject> facilityList = new ArrayList<>();
        facilityList.add(new JsonObject().add("id", 3).add("name", "Shadyside Parking Lot"));
        MqttNetworkMessage response = new MqttNetworkMessage(GetFacilitiesResponseChannel.createResponseObject(facilityList));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mGetFacilitiesRequestReceived send Response=" + response.getMessage());
        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mLoginRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "Login Requested Received=" + message.getMessage());
        MqttNetworkMessage response = new MqttNetworkMessage(LoginResponseChannel.createResponseObject(1, 1, "1111-2222-3333-4444", "12/16", "12345678"));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mLoginRequestReceived send Response=" + response.getMessage());
        message.responseFor(response);
    };

    private INetworkConnection mNc = null;
    private final INetworkCallback mNetworkCallback = new INetworkCallback() {

        @Override
        public void onConnected() {
            Log.logd(LOG_TAG, "onConnected");
            mLoginResponseChannel.listen();
            mLoginResponseChannel.addObserver(mLoginRequestReceived);
            mGetFacilitiesResponseChannel.listen();
            mGetFacilitiesResponseChannel.addObserver(mGetFacilitiesRequestReceived);
            mGetSlotsResponseChannel.listen();
            mGetSlotsResponseChannel.addObserver(mGetSlotsRequestReceived);
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
    private GetFacilitiesResponseChannel mGetFacilitiesResponseChannel = null;
    private GetSlotsResponseChannel mGetSlotsResponseChannel = null;

    private AttendantTestServer() {

        mNc = new MqttNetworkConnection(null);
        mLoginResponseChannel = new LoginResponseChannel(mNc);
        mGetFacilitiesResponseChannel = new GetFacilitiesResponseChannel(mNc);
        mGetSlotsResponseChannel = new GetSlotsResponseChannel(mNc);
        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new AttendantTestServer();
    }
}