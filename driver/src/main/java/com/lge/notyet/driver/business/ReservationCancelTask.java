package com.lge.notyet.driver.business;

import com.lge.notyet.channels.CancelReservationRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.ui.ITaskDoneCallback;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.mqtt.MqttNetworkMessage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-16.
 */

public class ReservationCancelTask implements Callable<Void> {

    private static final String LOG_TAG = "ReservationCancelTask";

    private String mSessionKey;
    private int mReservationId;
    private ITaskDoneCallback mTaskDoneCallback;

    public ReservationCancelTask(String sessionKey, int reservationId, ITaskDoneCallback taskDoneCallback) {
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

        MqttNetworkMessage requestMsg = cancelReservationRequestChannel.createRequestMessage(mSessionKey);
        Log.log(LOG_TAG, requestMsg.toString());

        cancelReservationRequestChannel.request(requestMsg);
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mCancelReservationResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mCancelReservationResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mCancelReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, int reservationId, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new ReservationCancelTask(sessionKey, reservationId, taskDoneCallback));
    }
}
