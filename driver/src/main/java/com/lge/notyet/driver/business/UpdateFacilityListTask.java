package com.lge.notyet.driver.business;

import com.lge.notyet.channels.ReservableFacilitiesRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class UpdateFacilityListTask implements Callable<Void> {

    private static final String LOG_TAG = "UpdateFacilityListTask";

    private final String mSessionKey;
    private final ITaskDoneCallback mTaskDoneCallback;

    private UpdateFacilityListTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        ReservableFacilitiesRequestChannel reservableFacilitiesRequestChannel = ncm.createReservableFacilitiesRequestChannel();
        reservableFacilitiesRequestChannel.addObserver(mUpdateFacilityListResult);
        reservableFacilitiesRequestChannel.addTimeoutObserver(mUpdateFacilityListTimeout);
        reservableFacilitiesRequestChannel.request(ReservableFacilitiesRequestChannel.createRequestMessage(mSessionKey));
        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mUpdateFacilityListResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logd(LOG_TAG, "mUpdateFacilityListResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mUpdateFacilityListTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new UpdateFacilityListTask(sessionKey, taskDoneCallback));
    }
}
