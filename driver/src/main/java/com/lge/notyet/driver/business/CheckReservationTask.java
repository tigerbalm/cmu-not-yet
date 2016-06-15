package com.lge.notyet.driver.business;

import com.lge.notyet.channels.GetReservationRequestChannel;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CheckReservationTask implements Callable<Void> {

    private String mSessionKey;
    private ITaskDoneCallback mTaskDoneCallback;

    public CheckReservationTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        GetReservationRequestChannel getReservationRequestChannel = ncm.createGetReservationRequestChannel();
        getReservationRequestChannel.addObserver(mReservationCheckResult);
        getReservationRequestChannel.addTimeoutObserver(mReservationTimeout);
        getReservationRequestChannel.request(getReservationRequestChannel.createRequestMessage(mSessionKey));
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mReservationCheckResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mReservationCheckResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new CheckReservationTask(sessionKey, taskDoneCallback));
    }
}
