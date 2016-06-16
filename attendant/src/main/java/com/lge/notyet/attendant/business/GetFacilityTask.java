package com.lge.notyet.attendant.business;

import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.ui.ITaskDoneCallback;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.channels.GetFacilitiesRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-16.
 */

public class GetFacilityTask implements Callable<Void> {


    private static final String LOG_TAG = "GetFacilityTask";

    private String mSessionKey;
    private ITaskDoneCallback mTaskDoneCallback;

    public GetFacilityTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        GetFacilitiesRequestChannel fc = ncm.createGetFacilitiesRequestChannel();
        fc.addObserver(mGetFacilityResult);
        fc.addTimeoutObserver(mGetFacilityTimeout);

        MqttNetworkMessage requestMsg = fc.createRequestMessage(mSessionKey);
        Log.log(LOG_TAG, requestMsg.toString());
        fc.request(requestMsg);
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mGetFacilityResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            System.out.println("mLoginResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mGetFacilityTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new GetFacilityTask(sessionKey, taskDoneCallback));
    }

}
