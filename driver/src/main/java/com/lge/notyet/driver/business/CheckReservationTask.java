package com.lge.notyet.driver.business;

import com.lge.notyet.channels.GetReservationRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class CheckReservationTask implements Callable<Void> {

    private String mSessionKey;
    private ITaskDoneCallback mTaskDoneCallback;

    private CheckReservationTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
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
        getReservationRequestChannel.request(GetReservationRequestChannel.createRequestMessage(mSessionKey));
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mReservationCheckResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                System.out.println("mReservationCheckResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
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
