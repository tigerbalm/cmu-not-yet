package com.lge.notyet.driver.business;

import com.lge.notyet.channels.ReservationMessage;
import com.lge.notyet.channels.ReservationRequestChannel;
import com.lge.notyet.driver.manager.ITaskDoneCallback;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class ReservationTask implements Callable<Void> {

    private int mFacilityId;
    private ITaskDoneCallback mTaskDoneCallback;

    public ReservationTask(int facilityId) {
        mFacilityId = facilityId;
    }
    public ReservationTask(int facilityId, ITaskDoneCallback taskDoneCallback) {
        mFacilityId = facilityId;
        mTaskDoneCallback = taskDoneCallback;
    }

    public void setTaskDoneCallback(ITaskDoneCallback taskDoneCallback) {
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();
        ReservationRequestChannel rc = ncm.createReservationChannel(mFacilityId);
        rc.addObserver(mReservationResult);
        rc.addTimeoutObserver(mReservationTimeout);
        ReservationMessage message = new ReservationMessage().setUserId("beney").setDate("6/1").setDate("11").setCreditCardNumber("1111-2222-3333-4444");
        rc.request(message);
        return null;
    }

    // Business Logic here, we have no time :(
    IOnResponse mReservationResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("Reservation Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    IOnTimeout mReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(int facilityId, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<Void>(new ReservationTask(facilityId, taskDoneCallback));
    }
}
