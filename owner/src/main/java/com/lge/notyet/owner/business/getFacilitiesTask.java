package com.lge.notyet.owner.business;

import com.lge.notyet.channels.GetFacilitiesRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.manager.NetworkConnectionManager;
import com.lge.notyet.owner.manager.SessionManager;
import com.lge.notyet.owner.ui.ITaskDoneCallback;
import com.lge.notyet.owner.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class getFacilitiesTask implements Callable<Void> {

    private static final String LOG_TAG = "getFacilitiesTask";

    private ITaskDoneCallback mTaskDoneCallback;

    public getFacilitiesTask(ITaskDoneCallback taskDoneCallback) {
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        GetFacilitiesRequestChannel lc = ncm.createGetFacilitiesRequestChannel();
        lc.addObserver(mFacilitiesListResult);
        lc.addTimeoutObserver(mFacilitiesListTimeout);

        MqttNetworkMessage requestMsg = lc.createRequestMessage(SessionManager.getInstance().getKey());
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

    public static FutureTask<Void> getTask(ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new getFacilitiesTask(taskDoneCallback));
    }
}
