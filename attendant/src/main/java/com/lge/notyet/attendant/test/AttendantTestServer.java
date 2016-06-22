package com.lge.notyet.attendant.test;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.lge.notyet.channels.*;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.*;
import com.lge.notyet.lib.comm.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AttendantTestServer {

    private static final String LOG_TAG = "TestServer";
    private final List<JsonObject> mSlotList = new ArrayList<>();

    // Business Logic here, we have no time :(
    private final IOnRequest mConfirmExitReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "mConfirmExitReceived Requested Received=" + message.getMessage() + ", topic=" + uri.getLocation().toString());

        String topic = (String) uri.getLocation();
        StringTokenizer topicTokenizer = new StringTokenizer(topic, "/");

        MqttNetworkMessage notificationMessage = (MqttNetworkMessage)message;

        try {

            topicTokenizer.nextToken(); // skip "controller"
            String physicalId = topicTokenizer.nextToken();

            JsonObject found = null;
            for (JsonObject obj : mSlotList) {
                if (physicalId.equals(obj.get("controller_physical_id").asString())) {
                    if (notificationMessage.getMessage().get("slot_no").asInt() == obj.get("number").asInt()) {
                        found = obj;
                        break;
                    }
                }
            }

            if (found != null){
                found.set("parked", 0).set("parked_ts", JsonValue.NULL).
                        set("reserved", 0).set("reservation_id", 0).add("email", "").add("reservation_ts", 0L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MqttNetworkMessage response = new MqttNetworkMessage(new JsonObject());
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mConfirmExitReceived send Response=" + response.getMessage());
        message.responseFor(response);
    };

    // Business Logic here, we have no time :(
    private final IOnRequest mGetSlotsRequestReceived = (networkChannel, uri, message) -> {

        // Need to parse
        Log.logd(LOG_TAG, "mGetSlotsRequestReceived Requested Received=" + message.getMessage() + ", topic=" + uri.getLocation().toString());
        MqttNetworkMessage response = new MqttNetworkMessage(GetSlotsResponseChannel.createResponseObject(mSlotList));
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
        MqttNetworkMessage response = new MqttNetworkMessage(LoginResponseChannel.createResponseObject(1, "1111-2222-3333-4444", "12/16", "12345678"));
        response.getMessage().add("success", 1);
        Log.logd(LOG_TAG, "mLoginRequestReceived send Response=" + response.getMessage());
        message.responseFor(response);
        scheduleControllerStatusUpdateThread();
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
            mConfirmExitResponseChannel.listen();
            mConfirmExitResponseChannel.addObserver(mConfirmExitReceived);
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
    private ControllerStatusPublishChannel mControllerStatusPublishChannel = null;
    private ConfirmExitResponseChannel mConfirmExitResponseChannel = null;

    private AttendantTestServer() {

        mNc = new MqttNetworkConnection(null);
        mLoginResponseChannel = new LoginResponseChannel(mNc);
        mGetFacilitiesResponseChannel = new GetFacilitiesResponseChannel(mNc);
        mGetSlotsResponseChannel = new GetSlotsResponseChannel(mNc);
        mControllerStatusPublishChannel = new ControllerStatusPublishChannel(mNc, "3");
        mConfirmExitResponseChannel = new ConfirmExitResponseChannel(mNc);

        mSlotList.add(new JsonObject().add("id", 1).add("number", 1).add("parked", 1).add("parked_ts", 1466021160L).add("available", 1).
                add("reserved", 1).add("controller_id",1).add("controller_physical_id","3").add("reservation_id", 23).add("email", "tony").add("reservation_ts", 1466001160L));
        mSlotList.add(new JsonObject().add("id", 2).add("number", 2).add("parked", 1).add("parked_ts", 1466041160L).add("available", 1).
                add("reserved", 1).add("controller_id",1).add("controller_physical_id","3").add("reservation_id", 123).add("email", "luffy").add("reservation_ts", 1466021160L));
        mSlotList.add(new JsonObject().add("id", 3).add("number", 3).add("parked", 0).add("parked_ts", 1466053160L).add("available", 1).
                add("reserved", 1).add("controller_id",1).add("controller_physical_id","3").add("reservation_id", 231).add("email", "allie").add("reservation_ts", 1466061160L));
        mSlotList.add(new JsonObject().add("id", 4).add("number", 4).add("parked", 0).add("parked_ts", JsonValue.NULL).add("available", 1).
                add("reserved", 0).add("controller_id",1).add("controller_physical_id","3").add("reservation_id", 0).add("email", "").add("reservation_ts", 0L));
        mSlotList.add(new JsonObject().add("id", 5).add("number", 1).add("parked", 0).add("parked_ts", JsonValue.NULL).add("available", 1).
                add("reserved", 0).add("controller_id",2).add("controller_physical_id","8").add("reservation_id", 0).add("email", "").add("reservation_ts", 0L));
        mSlotList.add(new JsonObject().add("id", 6).add("number", 2).add("parked", 0).add("parked_ts", JsonValue.NULL).add("available", 1).
                add("reserved", 1).add("controller_id",2).add("controller_physical_id","8").add("reservation_id", 121).add("email", "david").add("reservation_ts", 1466021160L));
        mSlotList.add(new JsonObject().add("id", 7).add("number", 3).add("parked", 0).add("parked_ts", JsonValue.NULL).add("available", 1).
                add("reserved", 1).add("controller_id",2).add("controller_physical_id","8").add("reservation_id", 344).add("email", "reshout").add("reservation_ts", 1466021160L));
        mSlotList.add(new JsonObject().add("id", 8).add("number", 4).add("parked", 1).add("parked_ts", 1466051160L).add("available", 1).
                add("reserved", 1).add("controller_id",2).add("controller_physical_id","8").add("reservation_id", 5655).add("email", "beney").add("reservation_ts", 1466045160L));
        mSlotList.add(new JsonObject().add("id", 9).add("number", 5).add("parked", 1).add("parked_ts", 1466054160L).add("available", 1).
                add("reserved", 1).add("controller_id",2).add("controller_physical_id","8").add("reservation_id", 1233).add("email", "beney2").add("reservation_ts", 1466034160L));

        mNc.connect(InetAddress.getLoopbackAddress(), mNetworkCallback);
    }

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        new AttendantTestServer();
    }

    // Test Module
    private static final int SLOT_STATUS_UPDATE_PERIOD = 10;
    private static final int SLOT_STATUS_UPDATE_MESSAGE_MAX = 3;
    private final ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(SLOT_STATUS_UPDATE_MESSAGE_MAX);

    private class ControllerStatusUpdateThread implements Runnable {

        boolean isAvailable = true;

        public void run() {
            MqttNetworkMessage notifyMsg = new MqttNetworkMessage(new JsonObject());
            notifyMsg.getMessage().add("available", (isAvailable ? 0:1));
            isAvailable = !isAvailable;
            mControllerStatusPublishChannel.notify(notifyMsg);
        }
    }

    private void scheduleControllerStatusUpdateThread() {
        // TODO: Need to check Maximum Pended Requests?
        mScheduler.scheduleAtFixedRate(new ControllerStatusUpdateThread(), SLOT_STATUS_UPDATE_PERIOD, SLOT_STATUS_UPDATE_PERIOD, SECONDS);
    }
}