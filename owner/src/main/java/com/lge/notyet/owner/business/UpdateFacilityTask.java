package com.lge.notyet.owner.business;

import com.lge.notyet.channels.UpdateFacilityRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.manager.NetworkConnectionManager;
import com.lge.notyet.owner.manager.SessionManager;
import com.lge.notyet.owner.ui.ITaskDoneCallback;
import com.lge.notyet.owner.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class UpdateFacilityTask implements Callable<Void> {

    private static final String LOG_TAG = "UpdateFacilityTask";

    private ITaskDoneCallback mTaskDoneCallback;
    private String facilityID;
    private String name;
    private String fee;
    private String fee_unit;
    private String grace_period;

    public UpdateFacilityTask(ITaskDoneCallback mTaskDoneCallback, String facilityID, String name, String fee, String fee_unit, String grace_period) {
        this.facilityID= facilityID;
        this.mTaskDoneCallback = mTaskDoneCallback;
        this.name = name;
        this.fee = fee;
        this.fee_unit = fee_unit;
        this.grace_period = grace_period;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        UpdateFacilityRequestChannel lc = ncm.createUpdateFacilityRequestChannel(facilityID);
        lc.addObserver(mFacilitiesListResult);
        lc.addTimeoutObserver(mFacilitiesListTimeout);

        MqttNetworkMessage requestMsg = lc.createRequestMessage(SessionManager.getInstance().getKey(), name, fee, fee_unit, grace_period);
        Log.log(LOG_TAG, requestMsg.toString());
        lc.request(requestMsg);
        return null;
    }

    // Business Logic here,
    private IOnResponse mFacilitiesListResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            System.out.println("mFacilitiesListResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mFacilitiesListTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(ITaskDoneCallback taskDoneCallback, String facilityID, String name, String fee, String fee_unit, String grace_period) {
        return new FutureTask<>(new UpdateFacilityTask(taskDoneCallback, facilityID, name, fee, fee_unit, grace_period));
    }
}
