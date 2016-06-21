package com.lge.notyet.driver.business;

import com.lge.notyet.channels.CancelReservationRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ReservationCancelTask implements Callable<Void> {

    private static final String LOG_TAG = "ReservationCancelTask";

    private final String mSessionKey;
    private final int mReservationId;
    private final ITaskDoneCallback mTaskDoneCallback;

    private ReservationCancelTask(String sessionKey, int reservationId, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mReservationId = reservationId;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        CancelReservationRequestChannel cancelReservationRequestChannel = ncm.createCancelReservationRequestChannel(mReservationId);
        cancelReservationRequestChannel.addObserver(mCancelReservationResult);
        cancelReservationRequestChannel.addTimeoutObserver(mCancelReservationTimeout);

        boolean ret = cancelReservationRequestChannel.request(CancelReservationRequestChannel.createRequestMessage(mSessionKey));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mCancelReservationResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mCancelReservationResult Result=" + message.getMessage());
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mCancelReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, int reservationId, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new ReservationCancelTask(sessionKey, reservationId, taskDoneCallback));
    }
}
