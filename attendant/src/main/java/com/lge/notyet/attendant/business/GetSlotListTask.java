package com.lge.notyet.attendant.business;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.ui.ITaskDoneCallback;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.GetFacilitiesRequestChannel;
import com.lge.notyet.channels.GetSlotsRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class GetSlotListTask implements Callable<Void> {

    private static final String LOG_TAG = "GetSlotListTask";

    private String mSessionKey;
    private int mFacilityId;
    private ITaskDoneCallback mTaskDoneCallback;

    public GetSlotListTask(String sessionKey, int facilityId, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mFacilityId = facilityId;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        GetSlotsRequestChannel sc = ncm.createGetSlotsRequestChannel(mFacilityId);
        sc.addObserver(mGetSlotsResult);
        sc.addTimeoutObserver(mGetSlotsTimeout);

        MqttNetworkMessage requestMsg = sc.createRequestMessage(mSessionKey);
        Log.log(LOG_TAG, requestMsg.toString());
        sc.request(requestMsg);
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mGetSlotsResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            System.out.println("mLoginResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mGetSlotsTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, int facilityId, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new GetSlotListTask(sessionKey, facilityId, taskDoneCallback));
    }
}
