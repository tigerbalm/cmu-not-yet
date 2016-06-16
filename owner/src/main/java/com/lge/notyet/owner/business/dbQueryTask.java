package com.lge.notyet.owner.business;

import com.lge.notyet.channels.GetDBQueryRequestChannel;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;
import com.lge.notyet.owner.manager.NetworkConnectionManager;
import com.lge.notyet.owner.manager.SessionManager;
import com.lge.notyet.owner.ui.ITaskDoneCallback;
import com.lge.notyet.owner.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class dbQueryTask implements Callable<Void> {

    private static final String LOG_TAG = "dbQueryTask";

    private String mQueryString;
    private ITaskDoneCallback mTaskDoneCallback;

    public dbQueryTask(String queryString, ITaskDoneCallback taskDoneCallback) {
        mQueryString = queryString;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        GetDBQueryRequestChannel lc = ncm.createGetDBQueryRequestChannel();
        lc.addObserver(mQueryResult);
        lc.addTimeoutObserver(mQueryTimeout);

        MqttNetworkMessage requestMsg = lc.createRequestMessage(SessionManager.getInstance().getKey(), mQueryString);
        Log.log(LOG_TAG, requestMsg.toString());
        lc.request(requestMsg);
        return null;
    }

    // Business Logic here,
    private IOnResponse mQueryResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mQueryResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mQueryTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String queryString, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new dbQueryTask(queryString, taskDoneCallback));
    }
}
