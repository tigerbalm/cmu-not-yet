package com.lge.notyet.driver.business;

import com.lge.notyet.channels.GetReservationRequestChannel;
import com.lge.notyet.channels.ReservableFacilitiesRequestChannel;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-15.
 */
public class UpdateFacilityListTask implements Callable<Void> {

    private String mSessionKey;
    private ITaskDoneCallback mTaskDoneCallback;

    public UpdateFacilityListTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
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
        reservableFacilitiesRequestChannel.request(reservableFacilitiesRequestChannel.createRequestMessage(mSessionKey));
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mUpdateFacilityListResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mUpdateFacilityListResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mUpdateFacilityListTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new UpdateFacilityListTask(sessionKey, taskDoneCallback));
    }
}
